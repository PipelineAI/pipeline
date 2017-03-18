package com.turn.tpmml.translator;

import com.turn.tpmml.CategoricalPredictor;
import com.turn.tpmml.DataField;
import com.turn.tpmml.DerivedField;
import com.turn.tpmml.FieldName;
import com.turn.tpmml.NumericPredictor;
import com.turn.tpmml.PMML;
import com.turn.tpmml.RegressionModel;
import com.turn.tpmml.RegressionNormalizationMethodType;
import com.turn.tpmml.RegressionTable;
import com.turn.tpmml.manager.ModelManagerException;
import com.turn.tpmml.manager.RegressionModelManager;
import com.turn.tpmml.manager.TPMMLException.TPMMLCause;
import com.turn.tpmml.translator.CodeFormatter.Operator;
import com.turn.tpmml.translator.Variable.VariableType;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * Translate regression model into java code.
 * 
 * @see RegressionModelManager.
 * 
 * @author tbadie
 * 
 */
public class RegressionModelTranslator extends RegressionModelManager implements Translator {

	private static final long serialVersionUID = 1L;
	private HashMap<RegressionTable, String> regressionTableToId =
			new HashMap<RegressionTable, String>();

	public RegressionModelTranslator(PMML pmml) {
		super(pmml);
	}

	public RegressionModelTranslator(PMML pmml, RegressionModel regressionModel) {
		super(pmml, regressionModel);
	}

	public RegressionModelTranslator(RegressionModelManager parent) throws ModelManagerException {
		this(parent.getPmml(), parent.getModel());
	}

	/**
	 * Return a string that is a java code able to evaluate the model on a set of parameters.
	 * 
	 * @param context
	 *            The translation context.
	 */
	public String translate(TranslationContext context) throws TranslationException {
		String outputVariableName = null;
		List<FieldName> predictedFields;

		try {
			predictedFields = getPredictedFields();
		} catch (ModelManagerException e) {
			throw new TranslationException(e);
		}

		// Get the predicted field. If there is none, it is an error.
		if (predictedFields != null && predictedFields.size() > 0) {
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
		StringBuilder sb = new StringBuilder();

		switch (getFunctionName()) {
		case REGRESSION:
			translateRegression(sb, context, outputField);
			break;
		case CLASSIFICATION:
			translateClassification(sb, context, outputField);
			break;
		default:
			throw new TranslationException(TPMMLCause.UNSUPPORTED_OPERATION,
					getFunctionName().name());
		}

		return sb.toString();
	}

	/**
	 * Translate the regression.
	 * 
	 * @param sb
	 *            The string builder we are working with.
	 * @param context
	 *            The context of the translation.
	 * @param outputField
	 *            The name of the output variable.
	 * @throws TranslationException
	 */

	private void translateRegression(StringBuilder sb, TranslationContext context,
			DataField outputField) throws TranslationException {
		RegressionTable rt;
		try {
			rt = getRegressionTables().get(0);
		} catch (ModelManagerException e) {
			throw new TranslationException(e);
		}
		CodeFormatter cf = context.getFormatter();

		translateRegressionTable(sb, context, outputField.getName().getValue(), rt, cf, true);
		translateNormalizationRegression(sb, context, outputField, cf);
	}

	/**
	 * Translate the classification.
	 * 
	 * @param sb
	 *            The string builder we are working with.
	 * @param context
	 *            The context of the translation.
	 * @param outputField
	 *            The name of the output variable.
	 * @throws TranslationException
	 */
	private void translateClassification(StringBuilder sb, TranslationContext context,
			DataField outputField) throws TranslationException {
		CodeFormatter cf = context.getFormatter();
		String targetCategoryToScoreVariable =
				context.generateLocalVariableName("targetCategoryToScore");
		context.requiredImports.add("import java.util.TreeMap;");
		cf.addLine(sb, context, "TreeMap<String, Double> " + targetCategoryToScoreVariable +
				" = new TreeMap<String, Double>();");

		TreeMap<String, String> categoryNameToVariable = new TreeMap<String, String>();
		try {
			for (RegressionTable rt : getRegressionTables()) {
				categoryNameToVariable.put(
						rt.getTargetCategory(),
						translateRegressionTable(sb, context, targetCategoryToScoreVariable, rt,
								cf, false));
			}
		} catch (ModelManagerException e) {
			throw new TranslationException(e);
		}

		// Apply the normalization:
		String scoreToCategoryVariable = context.generateLocalVariableName("scoreToCategory");
		cf.addLine(sb, context, "TreeMap<Double, String> " + scoreToCategoryVariable +
				" = new TreeMap<Double, String>();");
		switch (getNormalizationMethodType()) {
		case NONE:
			// Pick the category with top score.
			String entryName = context.generateLocalVariableName("entry");
			cf.declareVariable(sb, context, new Variable(VariableType.DOUBLE, entryName));
			try {
				for (RegressionTable rt : getRegressionTables()) {
					cf.assignVariable(sb, context, entryName,
							categoryNameToVariable.get(rt.getTargetCategory()));
					cf.addLine(sb, context, scoreToCategoryVariable + ".put(" + entryName + ", \"" +
							rt.getTargetCategory() + "\");");
				}
			} catch (ModelManagerException e) {
				throw new TranslationException(e);
			}
			break;
		case LOGIT:
			// pick the max of pj = 1 / ( 1 + exp( -yj ) )
			try {
				for (RegressionTable rt : getRegressionTables()) {
					String expression =
							"1.0 / (1.0 + Math.exp(-" +
									categoryNameToVariable.get(rt.getTargetCategory()) + "))";
					cf.addLine(sb, context, scoreToCategoryVariable + ".put(" + expression +
							", \"" + rt.getTargetCategory() + "\");");
				}
			} catch (ModelManagerException e) {
				throw new TranslationException(e);
			}
			break;
		case EXP:
			// pick the max of exp(yj)
			// FIXME: Since this is classification, and since exponential is growing, we may want to
			// only take the max without computing the exp.
			try {
				for (RegressionTable rt : getRegressionTables()) {
					String expression =
							"Math.exp(" + categoryNameToVariable.get(rt.getTargetCategory()) + ")";
					cf.addLine(sb, context, scoreToCategoryVariable + ".put(" + expression +
							", \"" + rt.getTargetCategory() + "\");");
				}
			} catch (ModelManagerException e) {
				throw new TranslationException(e);
			}
			break;
		case SOFTMAX:
			// pj = exp(yj) / (Sum[i = 1 to N](exp(yi) ) )
			String sumName = context.generateLocalVariableName("sum");
			cf.declareVariable(sb, context, new Variable(Variable.VariableType.DOUBLE, sumName));
			try {
				for (RegressionTable rt : getRegressionTables()) {
					cf.assignVariable(sb, context, Operator.PLUS_EQUAL, sumName, "Math.exp(" +
							categoryNameToVariable.get(rt.getTargetCategory()) + ")");
				}

				for (RegressionTable rt : getRegressionTables()) {
					cf.addLine(sb, context, scoreToCategoryVariable + ".put(Math.exp(" +
							categoryNameToVariable.get(rt.getTargetCategory()) + ") / " + sumName +
							", \"" + rt.getTargetCategory() + "\");");
				}
			} catch (ModelManagerException e) {
				throw new TranslationException(e);
			}
			break;
		case CLOGLOG:
			// pick the max of pj = 1 - exp( -exp( yj ) )

			try {
				for (RegressionTable rt : getRegressionTables()) {
					String expression =
							"1.0 - Math.exp(-Math.exp(" +
									categoryNameToVariable.get(rt.getTargetCategory()) + "))";
					cf.addLine(sb, context, scoreToCategoryVariable + ".put(" +
							expression + ", \"" + rt.getTargetCategory() + "\");");
				}
			} catch (ModelManagerException e) {
				throw new TranslationException(e);
			}
			break;
		case LOGLOG:
			// pick the max of pj = exp( -exp( -yj ) )
			try {
				for (RegressionTable rt : getRegressionTables()) {
					String expression =
							"Math.exp(-Math.exp(-" +
									categoryNameToVariable.get(rt.getTargetCategory()) + "))";
					cf.addLine(sb, context, scoreToCategoryVariable + ".put(" +
								expression + ", \"" + rt.getTargetCategory() + "\");");
				}
			} catch (ModelManagerException e) {
				throw new TranslationException(e);
			}
			break;
		default:
			throw new TranslationException(TPMMLCause.UNSUPPORTED_OPERATION,
					getNormalizationMethodType().name());
		}

		cf.assignVariable(sb, context,
				context.formatOutputVariable(outputField.getName().getValue()),
				context.formatOutputVariable(scoreToCategoryVariable + ".lastEntry().getValue()"));
	}

	private String namify(RegressionTable rt, TranslationContext context) {
		if (!regressionTableToId.containsKey(rt)) {
			regressionTableToId.put(
					rt,
					context.generateLocalVariableName("regressionTableNumber" +
							regressionTableToId.size()));
		}

		return regressionTableToId.get(rt);
	}

	/**
	 * Produce a code that evaluates a regressionTable.
	 * 
	 * @param sb
	 *            The string builder we are working with.
	 * @param context
	 *            The context of the translation.
	 * @param variableName
	 *            The name of the variable we want.
	 * @param rt
	 * @param cf
	 * @param storeResultInVariable
	 *            True if we want to affect the result to the output variable. False Otherwise.
	 * @return The name of the variable that contains the evaluation of the table.
	 * @throws TranslationException
	 */
	private String translateRegressionTable(StringBuilder sb, TranslationContext context,
                                          String variableName, RegressionTable rt, CodeFormatter cf,
                                          boolean storeResultInVariable)
			throws TranslationException {

		List<NumericPredictor> lnp = rt.getNumericPredictors();
		List<CategoricalPredictor> lcp = rt.getCategoricalPredictors();

		String categoryVariableName = namify(rt, context);

		cf.declareVariable(sb, context, new Variable(Variable.VariableType.DOUBLE,
				categoryVariableName), new Double(rt.getIntercept()).toString());

		for (NumericPredictor np : lnp) {
			translateNumericPredictor(sb, context, categoryVariableName, np, cf);
		}

		for (CategoricalPredictor cp : lcp) {
			translateCategoricalPredictor(sb, context, categoryVariableName, cp, cf);
		}

		if (storeResultInVariable) {
			cf.assignVariable(sb, context, context.formatOutputVariable(variableName),
					categoryVariableName);
		}

		return categoryVariableName;
	}

	/**
	 * Produce the code for the normalization for the regression.
	 * 
	 * @param code
	 *            The string builder we are working with.
	 * @param context
	 *            The context of the translation.
	 * @param outputVariable
	 *            The variable where we have to put the result.
	 * @param cf
	 *            The code formatter.
	 */
	private void translateNormalizationRegression(StringBuilder code, TranslationContext context,
			DataField outputVariable, CodeFormatter cf) {
		RegressionNormalizationMethodType normalizationMethod = getNormalizationMethodType();
		switch (normalizationMethod) {
		case NONE:
			// Do nothing.
			break;
		case SOFTMAX:
		case LOGIT:
			cf.assignVariable(
					code,
					context,
					context.formatOutputVariable(outputVariable.getName().getValue()),
					"1.0 / (1.0 + Math.exp(-" +
							context.formatOutputVariable(outputVariable.getName().getValue()) +
							"))");
			// result = 1.0 / (1.0 + Math.exp(-result));
			break;
		case EXP:
			cf.assignVariable(code, context,
					context.formatOutputVariable(outputVariable.getName().getValue()), "Math.exp(" +
							context.formatOutputVariable(
									outputVariable.getName().getValue()) + ")");
			// result = Math.exp(result);
			break;
		default:
			// We should never be here.
			assert false;
			break;
		}
	}

	/**
	 * Produce the code for the evaluation of a particular numeric predictor.
	 * 
	 * @param code
	 *            The string builder we are working with.
	 * @param context
	 *            The context of the translation.
	 * @param outputVariable
	 *            The variable where we have to put the result.
	 * @param numericPredictor
	 *            The numeric predictor we translate.
	 * @param cf
	 *            The code formatter.
	 * @throws TranslationException
	 */
	private void translateNumericPredictor(StringBuilder code, TranslationContext context,
			String outputVariableName, NumericPredictor numericPredictor, CodeFormatter cf)
			throws TranslationException {

		/*
    System.out.println("translateNumericPredictor:"+outputVariableName+":"+numericPredictor.getName().getValue());
    System.out.flush();
		*/

		DataField dataField = getDataField(numericPredictor.getName());

		/*
		if (dataField == null) {
			DerivedField derivedField = null;
			try {
				derivedField = resolve(numericPredictor.getName());
			}catch(ModelManagerException e) {
				e.printStackTrace();
			}
			dataField = new DataField(derivedField.getName(), 
																derivedField.getOptype(),
																derivedField.getDataType());
			addDataField(derivedField.getName(), derivedField.getDisplayName(),
									 derivedField.getOptype(), derivedField.getDataType());
		}
		*/
		
    if (dataField != null) {
			/*
        System.out.println("translateNumericPredictor:"+outputVariableName+"::"+dataField.getOptype());
        System.out.flush();
			*/

        cf.beginControlFlowStructure(
                                     code,
                                     context,
                                     "if",
                                     context.formatVariableName(this, numericPredictor.getName()) + " == " +
                                     context.getNullValueForVariable(dataField.getOptype()));
        cf.addLine(
                   code,
                   context,
                   "throw new " + context.getExceptionName() + "(\"Missing parameter " +
                   context.formatVariableName(this, numericPredictor.getName()) + "\");");
        cf.endControlFlowStructure(code, context);
        cf.beginControlFlowStructure(code, context, "else", null);
        cf.assignVariable(
                          code,
                          context,
                          Operator.PLUS_EQUAL,
                          outputVariableName,
                          numericPredictor.getCoefficient() + " * Math.pow(" +
                          context.formatVariableName(this, numericPredictor.getName()) + ", " +
                          new Integer(numericPredictor.getExponent()).doubleValue() + ")");
        cf.endControlFlowStructure(code, context);
    }
  }

	/**
	 * Produce the code for the evaluation of a particular categorical predictor.
	 * 
	 * @param code
	 *            The string builder we are working with.
	 * @param context
	 *            The context of the translation.
	 * @param outputVariable
	 *            The variable where we have to put the result.
	 * @param categoricalPredictor
	 *            The categorical predictor we translate.
	 * @param cf
	 *            The code formatter.
	 * @throws TranslationException
	 */
	private void translateCategoricalPredictor(StringBuilder code, TranslationContext context,
			String outputVariableName, CategoricalPredictor categoricalPredictor, CodeFormatter cf)
			throws TranslationException {

		DataField dataField = getDataField(categoricalPredictor.getName());

		cf.beginControlFlowStructure(
				code,
				context,
				"if",
				context.formatVariableName(this, categoricalPredictor.getName()) + " != " +
						context.getNullValueForVariable(dataField.getOptype()));
		cf.assignVariable(
				code,
				context,
				Operator.PLUS_EQUAL,
				outputVariableName,
				categoricalPredictor.getCoefficient() + " * ((" +
						generateEqualityExpression(categoricalPredictor, context) + ") ? 1 : 0)");
		cf.endControlFlowStructure(code, context);
	}

	/**
	 * Produce the code for an equality expression. The code is different between string and numbers
	 * type.
	 * 
	 * @param categoricalPredictor
	 *            The categorical predictor we translate.
	 * @param context
	 *            The context.
	 * @return The code corresponding to an is equal statement.
	 * @throws TranslationException
	 */
	private String generateEqualityExpression(CategoricalPredictor categoricalPredictor,
			TranslationContext context) throws TranslationException {

		for (DataField df : getDataDictionary().getDataFields()) {
			if (df.getName().getValue().equals(categoricalPredictor.getName().getValue())) {
				switch (df.getDataType()) {
				case STRING:
					return "" + context.formatVariableName(this, categoricalPredictor.getName()) +
							".equals(\"" + categoricalPredictor.getValue() + "\")";
				case FLOAT:
				case DOUBLE:
				case BOOLEAN:
				case INTEGER:
					return "" + context.formatVariableName(this, categoricalPredictor.getName()) +
							" == " + categoricalPredictor.getValue();
				default:
					throw new TranslationException(TPMMLCause.UNSUPPORTED_OPERATION,
							df.getDataType().name());
				}
			}
		}

		return "false";
	}

}
