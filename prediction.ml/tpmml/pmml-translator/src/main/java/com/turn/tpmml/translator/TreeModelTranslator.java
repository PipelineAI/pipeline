package com.turn.tpmml.translator;

import com.turn.tpmml.DataField;
import com.turn.tpmml.FieldName;
import com.turn.tpmml.NoTrueChildStrategyType;
import com.turn.tpmml.Node;
import com.turn.tpmml.OpType;
import com.turn.tpmml.PMML;
import com.turn.tpmml.Predicate;
import com.turn.tpmml.TreeModel;
import com.turn.tpmml.manager.ModelManagerException;
import com.turn.tpmml.manager.TPMMLException.TPMMLCause;
import com.turn.tpmml.manager.TreeModelManager;
import com.turn.tpmml.translator.Variable.VariableType;

import java.util.List;

/**
 * Translate tree model into java code
 * 
 * @author asvirsky
 * 
 */
public class TreeModelTranslator extends TreeModelManager implements Translator {

	private static final long serialVersionUID = 1L;

	public TreeModelTranslator(PMML pmml) {
		super(pmml);
	}

	public TreeModelTranslator(PMML pmml, TreeModel treeModel) {
		super(pmml, treeModel);
	}

	public TreeModelTranslator(TreeModelManager parent) throws ModelManagerException {
		this(parent.getPmml(), parent.getModel());
	}

	public String translate(TranslationContext context) throws TranslationException {

		String outputVariableName = null;
		List<FieldName> predictedFields;
		try {
			predictedFields = getPredictedFields();
		} catch (ModelManagerException e) {
			throw new TranslationException(e);
		}
		if (predictedFields != null && !predictedFields.isEmpty()) {
			outputVariableName = predictedFields.get(0).getValue();
		}
		if (outputVariableName == null) {
			throw new TranslationException("Predicted variable is not defined");
		}

		DataField outputField = getDataField(new FieldName(outputVariableName));
		if (outputField == null || outputField.getDataType() == null) {
			throw new TranslationException("Predicted variable [" + outputVariableName +
					"] does not have type defined");
		}

		return translate(context, outputField);
	}

	public String translate(TranslationContext context, DataField outputField)
			throws TranslationException {
		Node rootNode;
		try {
			rootNode = getOrCreateRoot();
		} catch (ModelManagerException e) {
			throw new TranslationException(e);
		}
		StringBuilder sb = new StringBuilder();
		CodeFormatter cf = new StandardCodeFormatter();
		generateCodeForNode(rootNode, context, sb, outputField, cf);

		return sb.toString();
	}

	private Node getChildById(Node node, String id) {
		Node result = null;
		if (id != null) {
			for (Node child : node.getNodes()) {
				if (id.equals(child.getId())) {
					result = child;
					break;
				}
			}
		}
		return result;
	}

	private void generateCodeForNode(Node node, TranslationContext context, StringBuilder code,
			DataField outputVariable, CodeFormatter cf) throws TranslationException {
		context.assignOutputVariable(code, node.getScore(), context, outputVariable);

		if (context.getModelResultTrackingVariable() != null && node.getId() != null) {
			cf.assignVariable(code, context, context.getModelResultTrackingVariable(),
					cf.stringify(node.getId()));
		}

		if (node.getNodes() == null || node.getNodes().isEmpty()) {
			return;
		}

		String succVariable = context.generateLocalVariableName("succ");

		cf.declareVariable(code, context, new Variable(VariableType.BOOLEAN, succVariable));

		for (Node child : node.getNodes()) {

			Predicate predicate = child.getPredicate();
			if (predicate == null) {
				throw new TranslationException("No predicate for node: " + child.getId());
			}

			cf.beginControlFlowStructure(code, context, "if", "!" + succVariable);

			String predicateValue = context.generateLocalVariableName("predicateValue");
			String predicateCode = PredicateTranslationUtil.generateCode(predicate, this, context);

			// evaluate predicate and store value into "predicateValue" variable
			cf.declareVariable(code, context, new Variable(VariableType.INTEGER, predicateValue),
					predicateCode);

			cf.beginControlFlowStructure(code, context, "if", predicateValue + " == " +
					PredicateTranslationUtil.TRUE);

			cf.assignVariable(code, context, succVariable, "true");

			// predicate is true - insert code for nested nodes
			generateCodeForNode(child, context, code, outputVariable, cf);

			cf.endControlFlowStructure(code, context);

			cf.beginControlFlowStructure(code, context, "else if", predicateValue + " == " +
					PredicateTranslationUtil.UNKNOWN);
			// predicate is unknown

			try {
				switch (this.getModel().getMissingValueStrategy()) {
				case NONE:
					// same as FALSE for current predicate
					// do nothing
					break;

				case LAST_PREDICTION:
					// assume this node evaluated to true, but ignore its value
					// take last prediction instead
					cf.assignVariable(code, context, succVariable, "true");
					break;

				case NULL_PREDICTION:
					// same as above, but reset prediction to null
					cf.assignVariable(code, context, succVariable, "true");

					cf.assignVariableToNullValue(
							code,
							context,
							new Variable(VariableType.OBJECT, "Double", context
									.formatOutputVariable(outputVariable.getName().getValue())));
					break;

				case DEFAULT_CHILD:
					// use default node
					// can't generate code if default child is undefined
					// (this seems to be expensive option in terms of amount of code generated...)
					Node defaultNode = getChildById(child, child.getDefaultChild());
					if (defaultNode == null) {
						throw new TranslationException("No default child defined for nodeId: " +
								child.getId());
					}
					generateCodeForNode(defaultNode, context, code, outputVariable, cf);
					break;
				default:
					throw new TranslationException(TPMMLCause.UNSUPPORTED_OPERATION,
							getModel().getMissingValueStrategy().name());
				}
			} catch (ModelManagerException e) {
				throw new TranslationException(e);
			}

			cf.endControlFlowStructure(code, context);
			cf.endControlFlowStructure(code, context);
		}

		try {
			if (getModel().getNoTrueChildStrategy() ==
					NoTrueChildStrategyType.RETURN_NULL_PREDICTION) {
				cf.beginControlFlowStructure(code, context, "if", "!" + succVariable);

				cf.assignVariableToNullValue(code, context,
						new Variable(VariableType.OBJECT, "Double",
						context.formatOutputVariable(outputVariable.getName().getValue())));

				cf.assignVariable(code, context, context.getModelResultTrackingVariable(),
						context.getNullValueForVariable(OpType.CATEGORICAL));

				cf.endControlFlowStructure(code, context);
			}
		} catch (ModelManagerException e) {
			throw new TranslationException(e);
		}
	}

}
