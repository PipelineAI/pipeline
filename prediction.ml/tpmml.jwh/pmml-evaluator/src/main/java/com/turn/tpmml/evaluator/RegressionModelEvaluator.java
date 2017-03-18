/*
 * Copyright (c) 2012 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.CategoricalPredictor;
import com.turn.tpmml.DataField;
import com.turn.tpmml.FieldName;
import com.turn.tpmml.MiningFunctionType;
import com.turn.tpmml.NumericPredictor;
import com.turn.tpmml.OpType;
import com.turn.tpmml.PMML;
import com.turn.tpmml.PredictorTerm;
import com.turn.tpmml.RegressionModel;
import com.turn.tpmml.RegressionNormalizationMethodType;
import com.turn.tpmml.RegressionTable;
import com.turn.tpmml.manager.IPMMLResult;
import com.turn.tpmml.manager.ModelManagerException;
import com.turn.tpmml.manager.PMMLResult;
import com.turn.tpmml.manager.RegressionModelManager;
import com.turn.tpmml.manager.TPMMLException.TPMMLCause;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class evaluates the variables on the model. It reads the pmml object to return a result. For
 * information about the regression model, see {@link RegressionModelManager}.
 * 
 * @author tbadie
 * 
 */
public class RegressionModelEvaluator extends RegressionModelManager implements Evaluator {

	private static final long serialVersionUID = 1L;

	public RegressionModelEvaluator(PMML pmml) {
		super(pmml);
	}

	public RegressionModelEvaluator(PMML pmml, RegressionModel regressionModel) {
		super(pmml, regressionModel);
	}

	public RegressionModelEvaluator(RegressionModelManager parent) throws ModelManagerException {
		this(parent.getPmml(), parent.getModel());
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
	@Override
	public IPMMLResult evaluate(Map<FieldName, ?> parameters) throws EvaluationException {
		RegressionModel regressionModel;
		try {
			regressionModel = getModel();
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}

		Map<FieldName, ?> predictions;

		ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(this, parameters);

		MiningFunctionType miningFunction = regressionModel.getFunctionName();
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

		if (predictions == null) {
			return null;
		}
		PMMLResult res = new PMMLResult();
		res = OutputUtil.evaluate(predictions, context);
		// FIXME: Dirty hack: Remove all the content of the result, and keep only the real result.
		// ( { foo => 1.0, bar => 0.5, baz => 0.0 } becomes foo ).
		try {
			if (res.getValue(getTarget()) instanceof ClassificationMap) {
				res.put(getTarget(), ((ClassificationMap) res.getValue(getTarget())).getResult());
			}
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}
		return res;
	}

	public Map<FieldName, Double> evaluateRegression(EvaluationContext context)
			throws EvaluationException {
		RegressionModel regressionModel;
		try {
			regressionModel = getModel();
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}

		List<RegressionTable> regressionTables;
		try {
			regressionTables = getRegressionTables();
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}

		if (regressionTables.size() != 1) {
			throw new EvaluationException("There are too many tables for a regression.");
		}

		RegressionTable regressionTable = regressionTables.get(0);

		Double value = evaluateRegressionTable(regressionTable, context);

		if (value == null) {
			return null;
		}

		FieldName name;
		try {
			name = getTarget();
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}

		RegressionNormalizationMethodType regressionNormalizationMethod =
				regressionModel.getNormalizationMethod();

		value = normalizeRegressionResult(regressionNormalizationMethod, value);

		return Collections.singletonMap(name, value);
	}

	public Map<FieldName, ClassificationMap> evaluateClassification(EvaluationContext context)
			throws EvaluationException {
		RegressionModel regressionModel;
		try {
			regressionModel = getModel();
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}

		List<RegressionTable> regressionTables;
		try {
			regressionTables = getRegressionTables();
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}

		if (regressionTables.size() < 1) {
			throw new EvaluationException("There is no regression table in this model");
		}

		double sumExp = 0d;

		ClassificationMap values = new ClassificationMap();

		for (RegressionTable regressionTable : regressionTables) {
			Double value = evaluateRegressionTable(regressionTable, context);

			if (value == null) {
				throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
						"Target");
			}

			sumExp += Math.exp(value.doubleValue());
			values.put(regressionTable.getTargetCategory(), value);
		}

		FieldName name;
		try {
			name = getTarget();
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}

		DataField dataField = getDataField(name);

		OpType opType = dataField.getOptype();
		switch (opType) {
		case CATEGORICAL:
			break;
		default:
			throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
					opType.name());
		}

		RegressionNormalizationMethodType regressionNormalizationMethod =
				regressionModel.getNormalizationMethod();

		Collection<Map.Entry<String, Double>> entries = values.entrySet();
		for (Map.Entry<String, Double> entry : entries) {
			if (entry.getValue() != null) {
				entry.setValue(normalizeClassificationResult(regressionNormalizationMethod,
						entry.getValue(), sumExp));
			} else {
				entry.setValue(null);
			}
		}

		return Collections.singletonMap(name, values);
	}

	private static Double evaluateRegressionTable(RegressionTable regressionTable,
			EvaluationContext context) throws EvaluationException {
		double result = 0D;

		result += regressionTable.getIntercept();

		List<NumericPredictor> numericPredictors = regressionTable.getNumericPredictors();

		for (NumericPredictor numericPredictor : numericPredictors) {
			Object value = ExpressionUtil.evaluate(numericPredictor.getName(), context);

			// "if the input value is missing then the result evaluates to a missing value"
			if (value == null) {
				return null;
			}

			result +=
					numericPredictor.getCoefficient() *
							Math.pow(((Number) value).doubleValue(),
									numericPredictor.getExponent());
		}

		List<CategoricalPredictor> categoricalPredictors =
				regressionTable.getCategoricalPredictors();
		for (CategoricalPredictor categoricalPredictor : categoricalPredictors) {
			Object value = ExpressionUtil.evaluate(categoricalPredictor.getName(), context);

			// "if the input value is missing then the product is ignored"
			if (value == null) {
				continue;
			}

			boolean equals = ParameterUtil.equals(value, categoricalPredictor.getValue());

			result += categoricalPredictor.getCoefficient() * (equals ? 1d : 0d);
		}

		List<PredictorTerm> predictorTerms = regressionTable.getPredictorTerms();
		for (PredictorTerm predictorTerm : predictorTerms) {
			throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
					predictorTerm.toString());
		}

		return result;
	}

	private static Double normalizeRegressionResult(
			RegressionNormalizationMethodType regressionNormalizationMethod, Double value)
			throws EvaluationException {
		switch (regressionNormalizationMethod) {
		case NONE:
			return value;
		case SOFTMAX:
		case LOGIT:
			return 1d / (1d + Math.exp(-value));
		case EXP:
			return Math.exp(value);
		default:
			throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
					regressionNormalizationMethod.name());
		}
	}

	private static Double normalizeClassificationResult(
			RegressionNormalizationMethodType regressionNormalizationMethod, Double value,
			Double sumExp) throws EvaluationException {

		switch (regressionNormalizationMethod) {
		case NONE:
			return value;
		case SOFTMAX:
			return Math.exp(value) / sumExp;
		case LOGIT:
			return 1d / (1d + Math.exp(-value));
		case CLOGLOG:
			return 1d - Math.exp(-Math.exp(value));
		case LOGLOG:
			return Math.exp(-Math.exp(-value));
		default:
			throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
					regressionNormalizationMethod.name());
		}
	}
}
