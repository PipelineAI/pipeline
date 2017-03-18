/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.DataType;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class FunctionUtilTest {

	@Test
	public void evaluateArithmeticFunctions() throws EvaluationException {
		assertEquals(4d, evaluate("+", 1d, 3d));
		assertEquals(-2d, evaluate("-", 1d, 3d));
		assertEquals(3d, evaluate("*", 1d, 3d));
		assertEquals((1d / 3d), evaluate("/", 1d, 3d));

		assertEquals(null, evaluate("+", 1d, null));
		assertEquals(null, evaluate("+", null, 1d));

		assertEquals(DataType.INTEGER, ParameterUtil.getDataType(evaluate("*", 1, 1)));
		assertEquals(DataType.FLOAT, ParameterUtil.getDataType(evaluate("*", 1f, 1f)));
		assertEquals(DataType.DOUBLE, ParameterUtil.getDataType(evaluate("*", 1d, 1d)));

		assertEquals(DataType.DOUBLE, ParameterUtil.getDataType(evaluate("/", 1, 1)));
		assertEquals(DataType.FLOAT, ParameterUtil.getDataType(evaluate("/", 1f, 1f)));
		assertEquals(DataType.DOUBLE, ParameterUtil.getDataType(evaluate("/", 1d, 1d)));
	}

	@Test
	public void evaluateAggregateFunctions() throws EvaluationException {
		List<Integer> values = Arrays.asList(1, 2, 3);

		Object min = evaluate("min", values);
		assertEquals(1, min);
		assertEquals(DataType.INTEGER, ParameterUtil.getDataType(min));

		Object max = evaluate("max", values);
		assertEquals(3, max);
		assertEquals(DataType.INTEGER, ParameterUtil.getDataType(max));

		Object average = evaluate("avg", values);
		assertEquals(2d, average);
		assertEquals(DataType.DOUBLE, ParameterUtil.getDataType(average));

		Object sum = evaluate("sum", values);
		assertEquals(6, sum);
		assertEquals(DataType.INTEGER, ParameterUtil.getDataType(sum));

		Object product = evaluate("product", values);
		assertEquals(6, product);
		assertEquals(DataType.INTEGER, ParameterUtil.getDataType(product));
	}

	@Test
	public void evaluateMathFunctions() throws EvaluationException {
		assertEquals(DataType.DOUBLE, ParameterUtil.getDataType(evaluate("log10", 1)));
		assertEquals(DataType.FLOAT, ParameterUtil.getDataType(evaluate("log10", 1f)));

		assertEquals(DataType.DOUBLE, ParameterUtil.getDataType(evaluate("ln", 1)));
		assertEquals(DataType.FLOAT, ParameterUtil.getDataType(evaluate("ln", 1f)));

		assertEquals(DataType.DOUBLE, ParameterUtil.getDataType(evaluate("exp", 1)));
		assertEquals(DataType.FLOAT, ParameterUtil.getDataType(evaluate("exp", 1f)));

		assertEquals(DataType.DOUBLE, ParameterUtil.getDataType(evaluate("sqrt", 1)));
		assertEquals(DataType.FLOAT, ParameterUtil.getDataType(evaluate("sqrt", 1f)));

		assertEquals(1, evaluate("abs", -1));
		assertEquals(1f, evaluate("abs", -1f));
		assertEquals(1d, evaluate("abs", -1d));

		assertEquals(DataType.INTEGER, ParameterUtil.getDataType(evaluate("pow", 1, 1)));
		assertEquals(DataType.FLOAT, ParameterUtil.getDataType(evaluate("pow", 1f, 1f)));

		assertEquals(0, evaluate("threshold", 2, 3));
		assertEquals(0, evaluate("threshold", 3, 3));
		assertEquals(1, evaluate("threshold", 3, 2));

		assertEquals(0f, evaluate("threshold", 2f, 3f));
		assertEquals(0f, evaluate("threshold", 3f, 3f));
		assertEquals(1f, evaluate("threshold", 3f, 2f));

		assertEquals(1, evaluate("floor", 1));
		assertEquals(1, evaluate("ceil", 1));

		assertEquals(1f, evaluate("floor", 1.99f));
		assertEquals(2f, evaluate("round", 1.99f));

		assertEquals(1f, evaluate("ceil", 0.01f));
		assertEquals(0f, evaluate("round", 0.01f));
	}

	@Test
	public void evaluateValueFunctions() throws EvaluationException {
		assertEquals(Boolean.TRUE, evaluate("isMissing", (String) null));
		assertEquals(Boolean.FALSE, evaluate("isMissing", "value"));

		assertEquals(Boolean.TRUE, evaluate("isNotMissing", "value"));
		assertEquals(Boolean.FALSE, evaluate("isNotMissing", (String) null));
	}

	@Test
	public void evaluateComparisonFunctions() throws EvaluationException {
		assertEquals(Boolean.TRUE, evaluate("equal", 1, 1d));
		assertEquals(Boolean.TRUE, evaluate("equal", 1d, 1d));

		assertEquals(Boolean.TRUE, evaluate("notEqual", 1d, 3d));
		assertEquals(Boolean.TRUE, evaluate("notEqual", 1, 3));

		assertEquals(Boolean.TRUE, evaluate("lessThan", 1d, 3d));
		assertEquals(Boolean.TRUE, evaluate("lessThan", 1, 3d));

		assertEquals(Boolean.TRUE, evaluate("lessOrEqual", 1d, 1d));
		assertEquals(Boolean.TRUE, evaluate("lessOrEqual", 1, 1d));

		assertEquals(Boolean.TRUE, evaluate("greaterThan", 3d, 1d));
		assertEquals(Boolean.TRUE, evaluate("greaterThan", 3, 1d));

		assertEquals(Boolean.TRUE, evaluate("greaterOrEqual", 3d, 3d));
		assertEquals(Boolean.TRUE, evaluate("greaterOrEqual", 3, 3d));
	}

	@Test
	public void evaluateBinaryFunctions() throws EvaluationException {
		assertEquals(Boolean.TRUE, evaluate("and", Boolean.TRUE, Boolean.TRUE));
		assertEquals(Boolean.TRUE, evaluate("and", Boolean.TRUE, Boolean.TRUE, Boolean.TRUE));

		assertEquals(Boolean.FALSE, evaluate("and", Boolean.TRUE, Boolean.FALSE));
		assertEquals(Boolean.FALSE, evaluate("and", Boolean.FALSE, Boolean.TRUE));

		assertEquals(Boolean.TRUE, evaluate("or", Boolean.FALSE, Boolean.TRUE));
		assertEquals(Boolean.TRUE, evaluate("or", Boolean.FALSE, Boolean.FALSE, Boolean.TRUE));

		assertEquals(Boolean.FALSE, evaluate("or", Boolean.FALSE, Boolean.FALSE));
	}

	@Test
	public void evaluateUnaryFunction() throws EvaluationException {
		assertEquals(Boolean.TRUE, evaluate("not", Boolean.FALSE));
		assertEquals(Boolean.FALSE, evaluate("not", Boolean.TRUE));
	}

	@Test
	public void evaluateValueListFunctions() throws EvaluationException {
		assertEquals(Boolean.TRUE, evaluate("isIn", "3", "1", "2", "3"));
		assertEquals(Boolean.TRUE, evaluate("isNotIn", "0", "1", "2", "3"));

		assertEquals(Boolean.TRUE, evaluate("isIn", 3, 1, 2, 3));
		assertEquals(Boolean.TRUE, evaluate("isNotIn", 0, 1, 2, 3));

		assertEquals(Boolean.TRUE, evaluate("isIn", 3d, 1d, 2d, 3d));
		assertEquals(Boolean.TRUE, evaluate("isNotIn", 0d, 1d, 2d, 3d));
	}

	@Test
	public void evaluateIfFunction() throws EvaluationException {
		assertEquals("left", evaluate("if", Boolean.TRUE, "left"));
		assertEquals("left", evaluate("if", Boolean.TRUE, "left", "right"));

		assertEquals(null, evaluate("if", Boolean.FALSE, "left"));
		assertEquals("right", evaluate("if", Boolean.FALSE, "left", "right"));
	}

	@Test
	public void evaluateStringFunctions() throws EvaluationException {
		assertEquals("VALUE", evaluate("uppercase", "Value"));
		assertEquals("value", evaluate("lowercase", "Value"));

		assertEquals("", evaluate("substring", "value", 1, 0));
		assertEquals("value", evaluate("substring", "value", 1, 5));

		assertEquals("alue", evaluate("substring", "value", 2, 4));
		assertEquals("valu", evaluate("substring", "value", 1, 4));

		assertEquals("value", evaluate("trimBlanks", "\tvalue\t"));
	}

	private static Object evaluate(String name, Object... values) throws EvaluationException {
		return evaluate(name, Arrays.asList(values));
	}

	private static Object evaluate(String name, List<?> values) throws EvaluationException {
		return FunctionUtil.evaluate(name, values);
	}
}
