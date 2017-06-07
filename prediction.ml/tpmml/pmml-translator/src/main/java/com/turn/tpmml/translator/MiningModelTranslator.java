package com.turn.tpmml.translator;

import com.turn.tpmml.DataField;
import com.turn.tpmml.DataType;
import com.turn.tpmml.FieldName;
import com.turn.tpmml.MiningModel;
import com.turn.tpmml.MultipleModelMethodType;
import com.turn.tpmml.OpType;
import com.turn.tpmml.PMML;
import com.turn.tpmml.Segment;
import com.turn.tpmml.manager.MiningModelManager;
import com.turn.tpmml.manager.ModelManager;
import com.turn.tpmml.manager.ModelManagerException;
import com.turn.tpmml.manager.TPMMLException.TPMMLCause;
import com.turn.tpmml.translator.CodeFormatter.Operator;
import com.turn.tpmml.translator.Variable.VariableType;

import java.util.HashMap;


/**
 * Generate java code to manage MiningModel.
 * 
 * @author tbadie
 * 
 */
public class MiningModelTranslator extends MiningModelManager implements Translator {

	private static final long serialVersionUID = 1L;
	private HashMap<Segment, String> segmentToId = new HashMap<Segment, String>();

	public MiningModelTranslator(PMML pmml) {
		super(pmml);
	}

	public MiningModelTranslator(PMML pmml, MiningModel model) {
		super(pmml, model);
	}

	public MiningModelTranslator(MiningModelManager parent) throws ModelManagerException {
		this(parent.getPmml(), parent.getModel());
	}

	/**
	 * Return a string that is a java code able to evaluate the model on a set of parameters.
	 * 
	 * @param context
	 *            The translation context.
	 * @throws Exception
	 */
	public String translate(TranslationContext context) throws TranslationException {
		try {
			return translate(context, getOutputField(this));
		} catch (ModelManagerException e) {
			throw new TranslationException(e);
		}
	}

	public String translate(TranslationContext context, DataField outputField)
			throws TranslationException {
		StringBuilder sb = new StringBuilder();
		switch (getFunctionType()) {
		case CLASSIFICATION:
			translateClassification(context, sb, outputField);
			break;
		case REGRESSION:
			translateRegression(context, sb, outputField);
			break;
		default:
			throw new TranslationException(TPMMLCause.UNSUPPORTED_OPERATION,
					getFunctionType().name());
		}

		return sb.toString();
	}

	private String namify(Segment s, TranslationContext context) {
		if (!segmentToId.containsKey(s)) {
			segmentToId.put(s,
					context.generateLocalVariableName("segmentNumber" + segmentToId.size()));
		}

		return segmentToId.get(s);
	}

	private void runModels(TranslationContext context, StringBuilder code, DataField outputField,
			CodeFormatter cf) throws TranslationException {

		ModelTranslatorFactory factory = new ModelTranslatorFactory();

		// Now, here is a subtle hack to avoid evaluate everything where we
		// are only interested in getting the first segment. The idea is to
		// use a "goto end" after the first segment that is evaluated to true.
		// Java does not provide a goto, so we rely on a "do { ... } while (false);"
		// structure. The code inside the brackets is the evaluation of all the segment.
		// And after the first segment evaluated, we just "break".
		if (getMultipleMethodModel() == MultipleModelMethodType.SELECT_FIRST) {
			cf.addLine(code, context, "do {");
		}

		// FIXME: Here we are in trouble because there is two Predicted results.
		try {
			for (Segment s : getSegments()) {
				Translator t = (Translator) factory.getModelManager(getPmml(), s.getModel());
				DataField out = getOutputField((ModelManager<?>) t);
				OpType op = out.getOptype();
				DataType dt = out.getDataType();

				cf.declareVariable(code, context, new Variable(dt, namify(s, context)), "null");
				cf.beginControlFlowStructure(
						code,
						context,
						"if",
						"(" +
								PredicateTranslationUtil.generateCode(s.getPredicate(), this,
										context) + ") == " + PredicateTranslationUtil.TRUE);
				code.append(t.translate(context, new DataField(new FieldName(namify(s, context)),
						op, dt)));

				if (getMultipleMethodModel() == MultipleModelMethodType.SELECT_FIRST) {
					cf.assignVariable(code, context,
							context.formatOutputVariable(outputField.getName().getValue()),
							namify(s, context));
					cf.addLine(code, context, "break;");
				}

				if (getMultipleMethodModel() == MultipleModelMethodType.MODEL_CHAIN) {
					cf.assignVariable(code, context, getOutputField((ModelManager<?>) t).getName()
							.getValue(), namify(s, context));
				}
				cf.endControlFlowStructure(code, context);
			}
		} catch (ModelManagerException e) {
			throw new TranslationException(e);
		}

		if (getMultipleMethodModel() == MultipleModelMethodType.SELECT_FIRST) {
			cf.addLine(code, context, "} while (false);");
		}

	}

	private void translateRegression(TranslationContext context, StringBuilder code,
			DataField outputField) throws TranslationException {
		CodeFormatter cf = context.getFormatter();
		runModels(context, code, outputField, cf);

		switch (getMultipleMethodModel()) {
		case SELECT_FIRST:
			// result already have the right value.
			break;
		case MODEL_CHAIN:
			// This case is to be managed before.
			break;
		case AVERAGE:
		case WEIGHTED_AVERAGE:
			Boolean weighted = getMultipleMethodModel() == MultipleModelMethodType.WEIGHTED_AVERAGE;

			String sumName = context.generateLocalVariableName("sum");
			String sumWeightName = context.generateLocalVariableName("sumWeight");

			cf.declareVariable(code, context, new Variable(VariableType.DOUBLE, sumName));
			if (weighted) {
				cf.declareVariable(code, context, new Variable(VariableType.DOUBLE, sumWeightName));
			}

			String counterName = context.generateLocalVariableName("counter");
			cf.declareVariable(code, context, new Variable(VariableType.INTEGER, counterName));

			try {
				for (Segment s : getSegments()) {
					// This following line is equivalent to add this to the code:
					// 'result += value == null ? value * weight : 0;' Where
					// the '* weight' is only done when we weighted is true.

					cf.beginControlFlowStructure(code, context, "if", namify(s, context) +
							" != null");
					cf.assignVariable(code, context, Operator.PLUS_EQUAL,
							new Variable(outputField),
							namify(s, context) + (weighted ? " * " + s.getWeight() : ""));
					cf.addLine(code, context, "++" + counterName + ";");

					if (weighted) {
						// Little hack to transform the weight into a string without creating
						// (explicitly) a Double, and call
						// ToString on it.
						cf.assignVariable(code, context, Operator.PLUS_EQUAL, sumWeightName,
								"" + s.getWeight());
					}
					cf.endControlFlowStructure(code, context);
				}
			} catch (ModelManagerException e) {
				throw new TranslationException(e);
			}

			cf.beginControlFlowStructure(code, context, "if", (weighted ? sumWeightName :
					counterName) + " != 0.0");
			cf.assignVariable(code, context, Operator.DIV_EQUAL, outputField.getName().getValue(),
					weighted ? sumWeightName : "" + counterName);

			cf.endControlFlowStructure(code, context);
			break;

		case MEDIAN:
			try {
				context.addRequiredImport("java.util.ArrayList");
				context.addRequiredImport("java.util.Collections");
				String listName = context.generateLocalVariableName("list");
				cf.addLine(code, context, "ArrayList<Double>" + listName +
						" = new ArrayList<Double>(" + getSegments().size() + ");");
				for (Segment s : getSegments()) {
					cf.beginControlFlowStructure(code, context, "if", namify(s, context) +
							"!= null");
					cf.addLine(code, context, listName + ".add(" + namify(s, context) + ");");
					cf.endControlFlowStructure(code, context);
				}
				cf.addLine(code, context, "Collections.sort(" + listName + ");");
				cf.assignVariable(code, context, outputField.getName().getValue(), listName +
						".get(" + listName + ".size() / 2);");
			} catch (ModelManagerException e) {
				throw new TranslationException(e);
			}
			break;
		default:
			throw new TranslationException("The method " + getMultipleMethodModel().value() +
					" is not compatible with the regression.");
		}

	}

	private void translateClassification(TranslationContext context, StringBuilder code,
			DataField outputField) throws TranslationException {
		CodeFormatter cf = context.getFormatter();
		runModels(context, code, outputField, cf);

		// Now work on the multiple method.
		switch (getMultipleMethodModel()) {
		case SELECT_FIRST:
			// Already handled
			break;
		case MODEL_CHAIN:
			// This case is to be managed before.
			throw new TranslationException(TPMMLCause.UNSUPPORTED_OPERATION, "MODEL_CHAIN");
		case MAJORITY_VOTE:
		case WEIGHTED_MAJORITY_VOTE:
			context.addRequiredImport("java.util.TreeMap;");
			String nameToVoteName = context.generateLocalVariableName("nameToVote");
			cf.declareVariable(code, context, new Variable(VariableType.OBJECT,
					"TreeMap<String, Double>", nameToVoteName));
			try {
				for (Segment s : getSegments()) {
					String name = namify(s, context);
					Double weight =
							(getMultipleMethodModel() ==
								MultipleModelMethodType.WEIGHTED_MAJORITY_VOTE) ? s
									.getWeight() : 1.0;
					cf.beginControlFlowStructure(code, context, "if", name + " != null");
					// This segment has voted.
					cf.beginControlFlowStructure(code, context, "if", nameToVoteName +
							".containsKey(" + name + ")");
					cf.addLine(code, context, nameToVoteName + ".put(" + name + ", " +
							nameToVoteName + ".get(" + name + ") + " + weight + ");");
					cf.endControlFlowStructure(code, context);
					cf.beginControlFlowStructure(code, context, "else", null);
					cf.addLine(code, context, nameToVoteName + ".put(" + name + ", " + weight +
							");");
					cf.endControlFlowStructure(code, context);
					cf.endControlFlowStructure(code, context);
					cf.addLine(
							code,
							context,
							getBetterKey(context, cf, nameToVoteName, outputField.getName()
									.getValue()));
				}
			} catch (ModelManagerException e) {
				throw new TranslationException(e);
			}
			break;
		case AVERAGE:
		case WEIGHTED_AVERAGE:
		case MEDIAN:
		case MAX:
			throw new TranslationException(TPMMLCause.UNSUPPORTED_OPERATION,
					getMultipleMethodModel().name());
		default:
			throw new TranslationException("The method " + getMultipleMethodModel().value() +
					" is not compatible with the classification.");
		}
	}

	/**
	 * Get an expression that stores the key that has the biggest value into the outputVariableName.
	 * 
	 * @param context
	 *            The context of the translation.
	 * @param cf
	 *            The formatter.
	 * @param mapName
	 *            The name of the variable.
	 * @param outputVariableName
	 *            The variable where we store the result.
	 * @return A valid expression.
	 * @throws TranslationException 
	 */
	private String getBetterKey(TranslationContext context, CodeFormatter cf, String mapName,
			String outputVariableName) throws TranslationException {
		StringBuilder result = new StringBuilder();

		String maxVarName = context.generateLocalVariableName("max");
		String tmpVariableName = context.generateLocalVariableName("tmpVariable");
		cf.declareVariable(result, context, new Variable(VariableType.DOUBLE, maxVarName));
		cf.beginControlFlowStructure(result, context, "for", "Map.Entry<?, Double> " +
				tmpVariableName + " : " + mapName + ".entrySet()");
		cf.beginControlFlowStructure(result, context, "if", tmpVariableName + ".getValue() > " +
				maxVarName);
		cf.assignVariable(result, context, outputVariableName, "(String) " + tmpVariableName +
				".getKey()");
		cf.assignVariable(result, context, maxVarName, tmpVariableName + ".getValue()");
		cf.endControlFlowStructure(result, context);

		cf.endControlFlowStructure(result, context);

		return result.toString();
	}
}
