/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.DataType;
import com.turn.tpmml.Expression;
import com.turn.tpmml.FieldName;
import com.turn.tpmml.Output;
import com.turn.tpmml.OutputField;
import com.turn.tpmml.ResultFeatureType;
import com.turn.tpmml.manager.ModelManager;
import com.turn.tpmml.manager.ModelManagerException;
import com.turn.tpmml.manager.PMMLResult;
import com.turn.tpmml.manager.TPMMLException.TPMMLCause;

import java.util.List;
import java.util.Map;

public class OutputUtil {

	private OutputUtil() {
	}

	public static PMMLResult evaluate(Map<FieldName, ?> predictions,
			ModelManagerEvaluationContext context) throws EvaluationException {
		PMMLResult res = new PMMLResult();
		for (Map.Entry<FieldName, ?> e : predictions.entrySet()) {
			res.put(e.getKey(), e.getValue());
		}

		return evaluate(res, context);
	}

	/**
	 * Evaluates the {@link Output} element.
	 * 
	 * @param predictions Map of {@link Evaluator#getPredictedFields() predicted field} values.
	 * 
	 * @return Map of {@link Evaluator#getPredictedFields() predicted field} values together with
	 *         {@link Evaluator#getOutputFields() output field} values.
	 * @throws EvaluationException 
	 */
	public static PMMLResult evaluate(PMMLResult predictions,
							ModelManagerEvaluationContext context) throws EvaluationException {
		PMMLResult result = new PMMLResult(predictions);

		// Create a modifiable context instance
		context = context.clone();

		ModelManager<?> modelManager = context.getModelManager();

		Output output;
		try {
			output = modelManager.getOrCreateOutput();
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}

		List<OutputField> outputFields = output.getOutputFields();
		for (OutputField outputField : outputFields) {
			ResultFeatureType resultFeature = outputField.getFeature();

			Object value;

			switch (resultFeature) {
			case PREDICTED_VALUE:
				FieldName target = getTarget(modelManager, outputField);

				if (!predictions.containsKey(target)) {
					throw new EvaluationException("There is no target");
				}

				// Prediction results may be either simple or complex values
				try {
					value = EvaluatorUtil.decode(predictions.getValue(target));
				} catch (ModelManagerException e) {
					throw new EvaluationException(e);
				}
				break;
			case TRANSFORMED_VALUE:
				Expression expression = outputField.getExpression();
				if (expression == null) {
					throw new EvaluationException("There is no expression");
				}

				value = ExpressionUtil.evaluate(expression, context);
				break;
			case PROBABILITY:
				FieldName target2 = getTarget(modelManager, outputField);

				if (!predictions.containsKey(target2)) {
					throw new EvaluationException("There is no expression");
				}

				try {
					value = getProbability(predictions.getValue(target2), outputField.getValue());
				} catch (ModelManagerException e) {
					throw new EvaluationException(e);
				}
				break;
			default:
				throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
						resultFeature.name());
			}

			FieldName name = outputField.getName();

			DataType dataType = outputField.getDataType();
			if (dataType != null) {
				value = ParameterUtil.cast(dataType, value);
			}

			result.put(name, value);

			// The result of one output field becomes available to other output fields
			context.putParameter(name, value);
		}

		return result;
	}

	private static FieldName getTarget(ModelManager<?> modelManager,
										OutputField outputField) throws EvaluationException {
		FieldName result = outputField.getTargetField();
		if (result == null) {
			try {
				result = modelManager.getTarget();
			} catch (ModelManagerException e) {
				throw new EvaluationException(e);
			}
		}

		return result;
	}

	private static Double getProbability(Object result, String value) throws EvaluationException {

		if (!(result instanceof Classification)) {
			throw new EvaluationException("Wrong result type");
		}

		Classification classification = (Classification) result;

		return classification.getProbability(value);
	}

}
