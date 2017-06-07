package com.turn.tpmml.translator;


import com.turn.tpmml.Attribute;
import com.turn.tpmml.Characteristic;
import com.turn.tpmml.DataField;
import com.turn.tpmml.FieldName;
import com.turn.tpmml.PMML;
import com.turn.tpmml.Predicate;
import com.turn.tpmml.Scorecard;
import com.turn.tpmml.manager.ModelManagerException;
import com.turn.tpmml.manager.ScoreCardModelManager;
import com.turn.tpmml.translator.CodeFormatter.Operator;
import com.turn.tpmml.translator.Variable.VariableType;

import java.util.List;

/**
 * Translate score card model into java code.
 * 
 * @see ScoreCardModelManager.
 * 
 * @author tbadie
 * 
 */
public class ScorecardTranslator extends ScoreCardModelManager implements Translator {

	private static final long serialVersionUID = 1L;

	public ScorecardTranslator(PMML pmml) {
		super(pmml);
	}

	public ScorecardTranslator(PMML pmml, Scorecard scorecard) {
		super(pmml, scorecard);
	}

	public ScorecardTranslator(ScoreCardModelManager parent) throws ModelManagerException {
		this(parent.getPmml(), parent.getModel());
	}

	/**
	 * Produce a code that evaluates the scorecard.
	 * 
	 * @param context The translation context.
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

		List<Characteristic> cl = scorecard.getCharacteristics().getCharacteristics();

		String diffToReasonCodeVariable = context.generateLocalVariableName("diffToReasonCode");

		context.requiredImports.add("import java.util.TreeMap;");
		context.getFormatter().declareVariable(
				sb,
				context,
				new Variable(VariableType.OBJECT, "TreeMap<Double, String>",
						diffToReasonCodeVariable));

		// Analyze each characteristic and print the corresponding code.
		for (Characteristic c : cl) {
			translateCharacteristic(c, context, sb, outputField, diffToReasonCodeVariable);
		}

		// Store the result of the reason code. It is currently stored in
		// diffToReasonCode.lastEntry().getValue().
		if (context.getModelResultTrackingVariable() != null) {
			context.getFormatter().assignVariable(sb, context, Operator.EQUAL,
					context.getModelResultTrackingVariable(),
					diffToReasonCodeVariable + ".lastEntry().getValue()");
		}
		return sb.toString();
	}

	// Method that takes a characteristics, and update the code.
	/**
	 * Generate a code that evaluates a characteristic.
	 * 
	 * @param c The characteristic.
	 * @param context The translation context.
	 * @param code The string builder we are working with.
	 * @param outputVariable The variable where the result will be written.
	 * @param diffToReasonCodeVariable The variable that represents the difference to the reason
	 *            code.
	 * @throws TranslationException If an attribute is missing.
	 */
	private void translateCharacteristic(Characteristic c, TranslationContext context,
			StringBuilder code, DataField outputVariable, String diffToReasonCodeVariable)
			throws TranslationException {
		// Run through each characteristic.
		// first is useful to know if we are the first case, or not. Depending on that, we add an
		// 'else' before the if.
		Boolean first = true;
		CodeFormatter cf = context.getFormatter();
		// Put some space between the different characteristics.
		code.append("\n");

		for (Attribute a : c.getAttributes()) {
			Predicate p = a.getPredicate();
			if (p == null) {
				throw new TranslationException("Missing attribute predicate for characteristic: " +
						c.getName());
			}

			String predicateCode = PredicateTranslationUtil.generateCode(p, this, context);

			// Evaluate the predicate. If it is true, update the score. If it is not the first 'if'
			// of the list,
			// Change it in an else if.
			cf.beginControlFlowStructure(code, context, (first ? "" : "else ") + "if", "(" +
					predicateCode + ") == " + PredicateTranslationUtil.TRUE);

			// Update the outputVariable with the corresponding partial score.
			cf.assignVariable(code, context, Operator.PLUS_EQUAL,
					context.formatOutputVariable(outputVariable.getName().getValue()),
					context.formatConstant(this, null, a.getPartialScore().toString()));

			// Compute the diff, include the result in the generated code.
			double diff = 0;
			switch (reasonCodeAlgorithm) {
			case POINTS_BELOW:
				diff = c.getBaselineScore() - a.getPartialScore();
				break;
			case POINTS_ABOVE:
				diff = a.getPartialScore() - c.getBaselineScore();
				break;
			default:
				// We should never be there.
				assert false;
				break;
			}

			// If there is a reason code at the attribute level, use it. Otherwise use the one in
			// the
			// characteristic level.
			cf.addLine(
					code,
					context,
					diffToReasonCodeVariable +
							".put(" +
							diff +
							", \"" +
							(a.getReasonCode() != null && !a.getReasonCode().isEmpty() ? a
									.getReasonCode() : c.getReasonCode()) + "\");");

			cf.endControlFlowStructure(code, context);
			first = false;
		}
	}
}
