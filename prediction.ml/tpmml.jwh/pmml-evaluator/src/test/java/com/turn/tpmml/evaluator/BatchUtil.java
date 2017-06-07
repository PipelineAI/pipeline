/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.FieldName;
import com.turn.tpmml.IOUtil;
import com.turn.tpmml.PMML;
import com.turn.tpmml.manager.IPMMLResult;
import com.turn.tpmml.manager.PMMLManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



public class BatchUtil {

	private BatchUtil() {
	}

	/**
	 * @return <code>true</code> If all evaluations succeeded, <code>false</code> otherwise.
	 */
	public static boolean evaluate(Batch batch) throws Exception {
		PMML pmml = IOUtil.unmarshal(batch.getModel());

		PMMLManager pmmlManager = new PMMLManager(pmml);

		Evaluator evaluator = (Evaluator) pmmlManager.getModelManager(null);

		List<Map<FieldName, String>> input = CsvUtil.load(batch.getInput());
		List<Map<FieldName, String>> output = CsvUtil.load(batch.getOutput());

		if (input.size() != output.size()) {
			throw new RuntimeException();
		}

		List<FieldName> activeFields = evaluator.getActiveFields();
		List<FieldName> predictedFields = evaluator.getPredictedFields();
		List<FieldName> outputFields = evaluator.getOutputFields();

		boolean success = true;

		for (int i = 0; i < input.size(); i++) {
			Map<FieldName, String> inputRow = input.get(i);
			Map<FieldName, String> outputRow = output.get(i);

			Map<FieldName, Object> parameters = new LinkedHashMap<FieldName, Object>();

			for (FieldName activeField : activeFields) {
				String inputCell = inputRow.get(activeField);

				parameters.put(activeField, evaluator.prepare(activeField, inputCell));
			}

			IPMMLResult result = evaluator.evaluate(parameters);

			for (FieldName predictedField : predictedFields) {
				String outputCell = outputRow.get(predictedField);

				Object predictedValue = EvaluatorUtil.decode(result.getValue(predictedField));

				success &= acceptable(outputCell, predictedValue);
			}

			for (FieldName outputField : outputFields) {
				String outputCell = outputRow.get(outputField);

				// XXX
				if (outputCell == null) {
					continue;
				}

				Object computedValue = result.getValue(outputField);

				success &= acceptable(outputCell, computedValue);
			}
		}

		return success;
	}

	private static boolean acceptable(String expected, Object actual) throws EvaluationException {
		return VerificationUtil.acceptable(
				ParameterUtil.parse(ParameterUtil.getDataType(actual), expected), actual,
				BatchUtil.PRECISION, BatchUtil.ZERO_THRESHOLD);
	}

	// One part per million parts
	private static final double PRECISION = 1d / (1000 * 1000);

	private static final double ZERO_THRESHOLD = PRECISION;
}
