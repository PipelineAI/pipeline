/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.Apply;
import com.turn.tpmml.Constant;
import com.turn.tpmml.DataType;
import com.turn.tpmml.DerivedField;
import com.turn.tpmml.Discretize;
import com.turn.tpmml.Expression;
import com.turn.tpmml.FieldColumnPair;
import com.turn.tpmml.FieldName;
import com.turn.tpmml.FieldRef;
import com.turn.tpmml.MapValues;
import com.turn.tpmml.NormContinuous;
import com.turn.tpmml.NormDiscrete;
import com.turn.tpmml.manager.TPMMLException.TPMMLCause;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExpressionUtil {

	private ExpressionUtil() {
	}

	public static Object evaluate(FieldName name, EvaluationContext context)
			throws EvaluationException {
		DerivedField derivedField = context.resolve(name);
		if (derivedField != null) {
			return evaluate(derivedField, context);
		}

		return context.getParameter(name);
	}

	public static Object evaluate(DerivedField derivedField, EvaluationContext context)
			throws EvaluationException {
		Object value = evaluate(derivedField.getExpression(), context);

		DataType dataType = derivedField.getDataType();
		if (dataType != null) {
			value = ParameterUtil.cast(dataType, value);
		}

		return value;
	}

	public static Object evaluate(Expression expression, EvaluationContext context)
			throws EvaluationException {

		if (expression instanceof Constant) {
			return evaluateConstant((Constant) expression, context);
		} else if (expression instanceof FieldRef) {
			return evaluateFieldRef((FieldRef) expression, context);
		} else if (expression instanceof NormContinuous) {
			return evaluateNormContinuous((NormContinuous) expression, context);
		} else if (expression instanceof NormDiscrete) {
			return evaluateNormDiscrete((NormDiscrete) expression, context);
		} else if (expression instanceof Discretize) {
			return evaluateDiscretize((Discretize) expression, context);
		} else if (expression instanceof MapValues) {
			return evaluateMapValues((MapValues) expression, context);
		} else if (expression instanceof Apply) {
			return evaluateApply((Apply) expression, context);
		}

		throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION, expression.toString());
	}

	public static Object evaluateConstant(Constant constant, EvaluationContext context)
			throws EvaluationException {
		String value = constant.getValue();

		DataType dataType = constant.getDataType();
		if (dataType == null) {
			dataType = ParameterUtil.getConstantDataType(value);
		}

		return ParameterUtil.parse(dataType, value);
	}

	public static Object evaluateFieldRef(FieldRef fieldRef, EvaluationContext context)
			throws EvaluationException {
		Object value = evaluate(fieldRef.getField(), context);
		if (value == null) {
			return fieldRef.getMapMissingTo();
		}

		return value;
	}

	public static Object evaluateNormContinuous(NormContinuous normContinuous,
			EvaluationContext context) throws EvaluationException {
		Number value = (Number) evaluate(normContinuous.getField(), context);
		if (value == null) {
			return normContinuous.getMapMissingTo();
		}

		return NormalizationUtil.normalize(normContinuous, value.doubleValue());
	}

	public static Object evaluateNormDiscrete(NormDiscrete normDiscrete, EvaluationContext context)
			throws EvaluationException {
		Object value = evaluate(normDiscrete.getField(), context);
		if (value == null) {
			return normDiscrete.getMapMissingTo();
		}

		boolean equals = ParameterUtil.equals(value, normDiscrete.getValue());

		return Double.valueOf(equals ? 1.0 : 0.0);
	}

	public static Object evaluateDiscretize(Discretize discretize, EvaluationContext context)
			throws EvaluationException {
		DataType dataType = discretize.getDataType();

		Object value = evaluate(discretize.getField(), context);
		if (value == null) {
			return parseSafely(dataType, discretize.getMapMissingTo());
		}

		String result = DiscretizationUtil.discretize(discretize, value);

		return parseSafely(dataType, result);
	}

	public static Object evaluateMapValues(MapValues mapValues, EvaluationContext context)
			throws EvaluationException {
		DataType dataType = mapValues.getDataType();

		Map<String, Object> values = new LinkedHashMap<String, Object>();

		List<FieldColumnPair> fieldColumnPairs = mapValues.getFieldColumnPairs();
		for (FieldColumnPair fieldColumnPair : fieldColumnPairs) {
			Object value = evaluate(fieldColumnPair.getField(), context);
			if (value == null) {
				return parseSafely(dataType, mapValues.getMapMissingTo());
			}

			values.put(fieldColumnPair.getColumn(), value);
		}

		String result = DiscretizationUtil.mapValue(mapValues, values);

		return parseSafely(dataType, result);
	}

	public static Object evaluateApply(Apply apply, EvaluationContext context)
			throws EvaluationException {
		List<Object> values = new ArrayList<Object>();

		List<Expression> arguments = apply.getExpressions();
		for (Expression argument : arguments) {
			Object value = evaluate(argument, context);

			values.add(value);
		}

		Object result = FunctionUtil.evaluate(apply.getFunction(), values);
		if (result == null) {
			return apply.getMapMissingTo();
		}

		return result;
	}

	private static Object parseSafely(DataType dataType, String value) throws EvaluationException {

		if (value != null) {

			if (dataType != null) {
				return ParameterUtil.parse(dataType, value);
			}
		}

		return value;
	}
}
