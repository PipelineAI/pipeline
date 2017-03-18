/*
 * Copyright (c) 2012 University of Tartu
 *
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.FieldName;
import com.turn.tpmml.MiningFunctionType;
import com.turn.tpmml.MiningModel;
import com.turn.tpmml.Model;
import com.turn.tpmml.MultipleModelMethodType;
import com.turn.tpmml.PMML;
import com.turn.tpmml.Predicate;
import com.turn.tpmml.Segment;
import com.turn.tpmml.Segmentation;
import com.turn.tpmml.manager.IPMMLResult;
import com.turn.tpmml.manager.MiningModelManager;
import com.turn.tpmml.manager.ModelManagerException;
import com.turn.tpmml.manager.PMMLResult;
import com.turn.tpmml.manager.TPMMLException.TPMMLCause;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MiningModelEvaluator2 extends MiningModelManager implements Evaluator {

	private static final long serialVersionUID = 1L;

	public MiningModelEvaluator2(PMML pmml) {
		super(pmml);
	}

	public MiningModelEvaluator2(PMML pmml, MiningModel miningModel) {
		super(pmml, miningModel);
	}

	public Object prepare(FieldName name, Object value) throws EvaluationException {
		try {
			return ParameterUtil.prepare(getDataField(name), getMiningField(name), value);
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}
	}

	/**
	 * @throws EvaluationException
	 * @see #evaluateRegression(EvaluationContext)
	 * @see #evaluateClassification(EvaluationContext)
	 */
	public IPMMLResult evaluate(Map<FieldName, ?> parameters) throws EvaluationException {
		try {
			MiningModel model = getModel();

			IPMMLResult predictions;

			ModelManagerEvaluationContext context =
					new ModelManagerEvaluationContext(this, parameters);

			MiningFunctionType miningFunction = model.getFunctionName();
			switch (miningFunction) {
			case REGRESSION:
				predictions = evaluateRegression(context);
				break;
			case CLASSIFICATION:
				predictions = evaluateClassification(context);
				break;
			default:
				throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
						miningFunction.name());
			}

			PMMLResult res = new PMMLResult();
			res = OutputUtil.evaluate((PMMLResult) predictions, context);
			return res;
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}
	}

	public IPMMLResult evaluateRegression(EvaluationContext context) throws EvaluationException {
		List<SegmentResult> segmentResults = evaluate(context);
		try {
			Segmentation segmentation = getSegmentation();

			MultipleModelMethodType multipleModelMethod = segmentation.getMultipleModelMethod();
			switch (multipleModelMethod) {
			case SELECT_FIRST:
			case MODEL_CHAIN:
				return dispatchSingleResult(segmentResults);
			case SELECT_ALL:
				throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
						multipleModelMethod.name());
			default:
				break;
			}

			Double result;

			double sum = 0d;
			double weightedSum = 0d;

			for (SegmentResult segmentResult : segmentResults) {
				Object predictedValue = EvaluatorUtil.decode(segmentResult.getPrediction());

				Double value = ParameterUtil.toDouble(predictedValue);

				sum += value.doubleValue();
				weightedSum += ((segmentResult.getSegment()).getWeight() * value.doubleValue());
			}

			int count = segmentResults.size();

			switch (multipleModelMethod) {
			case SUM:
				result = sum;
				break;
			case AVERAGE:
				result = (sum / count);
				break;
			case WEIGHTED_AVERAGE:
				result = (weightedSum / count);
				break;
			default:
				throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
						multipleModelMethod.name());
			}

			PMMLResult res = new PMMLResult();
			res.put(getTarget(), result);

			return res;
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}
	}

	public IPMMLResult evaluateClassification(EvaluationContext context)
			throws EvaluationException {
		List<SegmentResult> segmentResults = evaluate(context);

		Segmentation segmentation;

		try {
			segmentation = getSegmentation();
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}

		MultipleModelMethodType multipleModelMethod = segmentation.getMultipleModelMethod();
		switch (multipleModelMethod) {
		case SELECT_FIRST:
		case MODEL_CHAIN:
			return dispatchSingleResult(segmentResults);
		case SELECT_ALL:
			throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
					multipleModelMethod.name());
		default:
			break;
		}

		ClassificationMap result = new ClassificationMap();

		for (SegmentResult segmentResult : segmentResults) {
			Object predictedValue;
			try {
				predictedValue = EvaluatorUtil.decode(segmentResult.getPrediction());
			} catch (ModelManagerException e) {
				throw new EvaluationException(e);
			}

			String value = ParameterUtil.toString(predictedValue);

			Double vote = result.get(value);
			if (vote == null) {
				vote = 0d;
			}

			switch (multipleModelMethod) {
			case MAJORITY_VOTE:
				vote += 1d;
				break;
			case WEIGHTED_MAJORITY_VOTE:
				vote += ((segmentResult.getSegment()).getWeight() * 1d);
				break;
			default:
				throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
						multipleModelMethod.name());
			}

			result.put(value, vote);
		}

		result.normalizeProbabilities();

		PMMLResult res = new PMMLResult();
		try {
			res.put(getTarget(), result);
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}

		return res;

		// return Collections.singletonMap(getTarget(), result);
	}

	private IPMMLResult dispatchSingleResult(List<SegmentResult> results)
			throws EvaluationException {

		if (results.size() != 1) {
			throw new EvaluationException(results.size() + " results are returned," +
					" but only one is expected");
		}

		SegmentResult result = results.get(0);

		return result.getResult();
	}

	@SuppressWarnings("fallthrough")
	private List<SegmentResult> evaluate(EvaluationContext context) throws EvaluationException {
		List<SegmentResult> results = new ArrayList<SegmentResult>();

		Segmentation segmentation;
		try {
			segmentation = getSegmentation();
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}

		MultipleModelMethodType multipleModelMethod = segmentation.getMultipleModelMethod();

		List<Segment> segments = segmentation.getSegments();
		for (Segment segment : segments) {
			Predicate predicate = segment.getPredicate();

			Boolean selectable = PredicateUtil.evaluate(predicate, context);
			if (selectable == null) {
				throw new EvaluationException("Predicate doesn't evaluate to false or true");
			} // End if

			if (!selectable.booleanValue()) {
				continue;
			}

			Model model = segment.getModel();

			Evaluator evaluator;

			try {
				evaluator = (Evaluator) EVALUATOR_FACTORY.getModelManager(getPmml(), model);
			} catch (ModelManagerException e1) {
				throw new EvaluationException(e1);
			}

			FieldName target;
			try {
				target = evaluator.getTarget();
			} catch (ModelManagerException e) {
				throw new EvaluationException(e);
			}

			IPMMLResult result = evaluator.evaluate(context.getParameters());

			switch (multipleModelMethod) {
			case SELECT_FIRST:
				return Collections.singletonList(new SegmentResult(segment, target, result));
			case MODEL_CHAIN:
				List<FieldName> outputFields;

				try {
					outputFields = evaluator.getOutputFields();
				} catch (ModelManagerException e) {
					throw new EvaluationException(e);
				}

				for (FieldName outputField : outputFields) {
					Object outputValue;
					try {
						outputValue = result.getValue(outputField);
					} catch (ModelManagerException e) {
						throw new EvaluationException(e);
					}
					if (outputValue == null) {
						throw new EvaluationException("Output value is null");
					}

					outputValue = EvaluatorUtil.decode(outputValue);

					context.putParameter(outputField, outputValue);
				}

				results.clear();
				// Falls through
			default:
				results.add(new SegmentResult(segment, target, result));
				break;
			}
		}

		return results;
	}

	private static final ModelEvaluatorFactory EVALUATOR_FACTORY = ModelEvaluatorFactory
			.getInstance();

	private static class SegmentResult {

		private Segment segment = null;

		private FieldName predictedField = null;

		private IPMMLResult result = null;

		public SegmentResult(Segment segment, FieldName predictedField, IPMMLResult result) {
			setSegment(segment);
			setPredictedField(predictedField);
			setResult(result);
		}

		public Object getPrediction() throws ModelManagerException {
			return getResult().getValue(getPredictedField());
		}

		public Segment getSegment() {
			return this.segment;
		}

		private void setSegment(Segment segment) {
			this.segment = segment;
		}

		public FieldName getPredictedField() {
			return this.predictedField;
		}

		private void setPredictedField(FieldName predictedField) {
			this.predictedField = predictedField;
		}

		public IPMMLResult getResult() {
			return result;
		}

		public void setResult(IPMMLResult result) {
			this.result = result;
		}

	}
}
