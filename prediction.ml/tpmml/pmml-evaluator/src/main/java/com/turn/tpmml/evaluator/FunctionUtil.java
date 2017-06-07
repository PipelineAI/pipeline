/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.DataType;
import com.turn.tpmml.manager.TPMMLException.TPMMLCause;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.apache.commons.math3.stat.descriptive.summary.Sum;

public class FunctionUtil {

	private FunctionUtil() {
	}

	public static Object evaluate(String name, List<?> values) throws EvaluationException {
		Function function = getFunction(name);
		if (function == null) {
			throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION, name.toString());
		}

		return function.evaluate(values);
	}

	public static Function getFunction(String name) {
		return FunctionUtil.FUNCTIONS.get(name);
	}

	public static void putFunction(String name, Function function) {
		FunctionUtil.FUNCTIONS.put(name, function);
	}

	private static Boolean asBoolean(Object value) throws EvaluationException {

		if (value instanceof Boolean) {
			return (Boolean) value;
		}

		throw new EvaluationException(value + " is not a boolean");
	}

	private static Number asNumber(Object value) throws EvaluationException {

		if (value instanceof Number) {
			return (Number) value;
		}

		throw new EvaluationException(value + " is not a number");
	}

	private static Integer asInteger(Object value) throws EvaluationException {

		if (value instanceof Integer) {
			return (Integer) value;
		}

		throw new EvaluationException(value + " is not an integer");
	}

	private static String asString(Object value) throws EvaluationException {

		if (value instanceof String) {
			return (String) value;
		}

		throw new EvaluationException(value + " is not a string");
	}

	private static DataType integerToDouble(DataType dataType) {

		if ((DataType.INTEGER).equals(dataType)) {
			return DataType.DOUBLE;
		}

		return dataType;
	}

	private static final Map<String, Function> FUNCTIONS = new LinkedHashMap<String, Function>();

	public interface Function {

		Object evaluate(List<?> values) throws EvaluationException;
	}

	public abstract static class ArithmeticFunction implements Function {

		public abstract Double evaluate(Number left, Number right);

		public Number cast(DataType dataType, Double result) throws EvaluationException {
			return asNumber(ParameterUtil.cast(dataType, result));
		}

		public Number evaluate(List<?> values) throws EvaluationException {

			if (values.size() != 2) {
				throw new EvaluationException("Wrong number of arguments: expected 2, got " +
						values.size());
			}

			Object left = values.get(0);
			Object right = values.get(1);

			if (left == null || right == null) {
				return null;
			}

			DataType dataType = ParameterUtil.getResultDataType(left, right);

			return cast(dataType, evaluate(asNumber(left), asNumber(right)));
		}
	}

	static {
		putFunction("+", new ArithmeticFunction() {

			@Override
			public Double evaluate(Number left, Number right) {
				return Double.valueOf(left.doubleValue() + right.doubleValue());
			}
		});

		putFunction("-", new ArithmeticFunction() {

			@Override
			public Double evaluate(Number left, Number right) {
				return Double.valueOf(left.doubleValue() - right.doubleValue());
			}
		});

		putFunction("*", new ArithmeticFunction() {

			@Override
			public Double evaluate(Number left, Number right) {
				return Double.valueOf(left.doubleValue() * right.doubleValue());
			}
		});

		putFunction("/", new ArithmeticFunction() {

			@Override
			public Number cast(DataType dataType, Double result) throws EvaluationException {
				return super.cast(integerToDouble(dataType), result);
			}

			@Override
			public Double evaluate(Number left, Number right) {
				return Double.valueOf(left.doubleValue() / right.doubleValue());
			}
		});
	}

	public abstract static class AggregateFunction implements Function {

		public abstract StorelessUnivariateStatistic createStatistic();

		public Number cast(DataType dataType, Double result) throws EvaluationException {
			return asNumber(ParameterUtil.cast(dataType, result));
		}

		public Number evaluate(List<?> values) throws EvaluationException {
			StorelessUnivariateStatistic statistic = createStatistic();

			DataType dataType = null;

			for (Object value : values) {

				if (value == null) {
					continue;
				}

				statistic.increment(asNumber(value).doubleValue());

				if (dataType != null) {
					dataType =
							ParameterUtil.getResultDataType(dataType,
									ParameterUtil.getDataType(value));
				} else {
					dataType = ParameterUtil.getDataType(value);
				}
			}

			if (statistic.getN() == 0) {
				throw new EvaluationException("There is no result in statistic");
			}

			return cast(dataType, statistic.getResult());
		}
	}

	static {
		putFunction("min", new AggregateFunction() {

			@Override
			public Min createStatistic() {
				return new Min();
			}
		});

		putFunction("max", new AggregateFunction() {

			@Override
			public Max createStatistic() {
				return new Max();
			}
		});

		putFunction("avg", new AggregateFunction() {

			@Override
			public Mean createStatistic() {
				return new Mean();
			}

			@Override
			public Number cast(DataType dataType, Double result) throws EvaluationException {
				return super.cast(integerToDouble(dataType), result);
			}
		});

		putFunction("sum", new AggregateFunction() {

			@Override
			public Sum createStatistic() {
				return new Sum();
			}
		});

		putFunction("product", new AggregateFunction() {

			@Override
			public Product createStatistic() {
				return new Product();
			}
		});
	}

	public abstract static class MathFunction implements Function {

		public abstract Double evaluate(Number value);

		public Number cast(DataType dataType, Number result) throws EvaluationException {
			return asNumber(ParameterUtil.cast(dataType, result));
		}

		public Number evaluate(List<?> values) throws EvaluationException {

			if (values.size() != 1) {
				throw new EvaluationException("Wrong number of arguments, expected 1, got " +
						values.size());
			}

			Object value = values.get(0);

			DataType dataType = ParameterUtil.getDataType(value);

			return cast(dataType, evaluate(asNumber(value)));
		}
	}

	public abstract static class FpMathFunction extends MathFunction {

		@Override
		public Number cast(DataType dataType, Number result) throws EvaluationException {
			return super.cast(integerToDouble(dataType), result);
		}
	}

	static {
		putFunction("log10", new FpMathFunction() {

			@Override
			public Double evaluate(Number value) {
				return Math.log10(value.doubleValue());
			}
		});

		putFunction("ln", new FpMathFunction() {

			@Override
			public Double evaluate(Number value) {
				return Math.log(value.doubleValue());
			}
		});

		putFunction("exp", new FpMathFunction() {

			@Override
			public Double evaluate(Number value) {
				return Math.exp(value.doubleValue());
			}
		});

		putFunction("sqrt", new FpMathFunction() {

			@Override
			public Double evaluate(Number value) {
				return Math.sqrt(value.doubleValue());
			}
		});

		putFunction("abs", new MathFunction() {

			@Override
			public Double evaluate(Number value) {
				return Math.abs(value.doubleValue());
			}
		});

		putFunction("pow", new Function() {

			public Number evaluate(List<?> values) throws EvaluationException {

				if (values.size() != 2) {
					throw new EvaluationException("Wrong number of arguments: expected 2, got " +
							values.size());
				}

				Number left = asNumber(values.get(0));
				Number right = asNumber(values.get(1));

				DataType dataType = ParameterUtil.getResultDataType(left, right);

				Double result = Math.pow(left.doubleValue(), right.doubleValue());

				return asNumber(ParameterUtil.cast(dataType, result));
			}
		});

		putFunction("threshold", new Function() {

			public Number evaluate(List<?> values) throws EvaluationException {

				if (values.size() != 2) {
					throw new EvaluationException("Wrong number of arguments: expected 2, got " +
							values.size());
				}

				Number left = asNumber(values.get(0));
				Number right = asNumber(values.get(1));

				DataType dataType = ParameterUtil.getResultDataType(left, right);

				Integer result = (left.doubleValue() > right.doubleValue()) ? 1 : 0;

				return asNumber(ParameterUtil.cast(dataType, result));
			}
		});

		putFunction("floor", new MathFunction() {

			@Override
			public Double evaluate(Number number) {
				return Math.floor(number.doubleValue());
			}
		});

		putFunction("ceil", new MathFunction() {

			@Override
			public Double evaluate(Number number) {
				return Math.ceil(number.doubleValue());
			}
		});

		putFunction("round", new MathFunction() {

			@Override
			public Double evaluate(Number number) {
				return (double) Math.round(number.doubleValue());
			}
		});
	}

	public abstract static class ValueFunction implements Function {

		public abstract Boolean evaluate(Object value);

		public Boolean evaluate(List<?> values) throws EvaluationException {

			if (values.size() != 1) {
				throw new EvaluationException("Wrong number of arguments: expected 1, got " +
						values.size());
			}

			return evaluate(values.get(0));
		}
	}

	static {
		putFunction("isMissing", new ValueFunction() {

			@Override
			public Boolean evaluate(Object value) {
				return Boolean.valueOf(value == null);
			}
		});

		putFunction("isNotMissing", new ValueFunction() {

			@Override
			public Boolean evaluate(Object value) {
				return Boolean.valueOf(value != null);
			}
		});
	}

	public abstract static class ComparisonFunction implements Function {

		public abstract Boolean evaluate(int diff);

		public <C extends Comparable<C>> Boolean evaluate(C left, C right) {
			return evaluate((left).compareTo(right));
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public Boolean evaluate(List<?> values) throws EvaluationException {

			if (values.size() != 2) {
				throw new EvaluationException("Wrong number of arguments: expected 2, got " +
						values.size());
			}

			Object left = values.get(0);
			Object right = values.get(1);

			if (left == null || right == null) {
				throw new EvaluationException("One of the operand is null");
			}

			// Cast operands to common data type before comparison
			if (!(left.getClass()).equals(right.getClass())) {
				DataType dataType = ParameterUtil.getResultDataType(left, right);

				left = ParameterUtil.cast(dataType, left);
				right = ParameterUtil.cast(dataType, right);
			}

			return evaluate((Comparable) left, (Comparable) right);
		}
	}

	static {
		putFunction("equal", new ComparisonFunction() {

			@Override
			public Boolean evaluate(int diff) {
				return Boolean.valueOf(diff == 0);
			}
		});

		putFunction("notEqual", new ComparisonFunction() {

			@Override
			public Boolean evaluate(int diff) {
				return Boolean.valueOf(diff != 0);
			}
		});

		putFunction("lessThan", new ComparisonFunction() {

			@Override
			public Boolean evaluate(int diff) {
				return Boolean.valueOf(diff < 0);
			}
		});

		putFunction("lessOrEqual", new ComparisonFunction() {

			@Override
			public Boolean evaluate(int diff) {
				return Boolean.valueOf(diff <= 0);
			}
		});

		putFunction("greaterThan", new ComparisonFunction() {

			@Override
			public Boolean evaluate(int diff) {
				return Boolean.valueOf(diff > 0);
			}
		});

		putFunction("greaterOrEqual", new ComparisonFunction() {

			@Override
			public Boolean evaluate(int diff) {
				return Boolean.valueOf(diff >= 0);
			}
		});
	}

	public abstract static class BinaryBooleanFunction implements Function {

		public abstract Boolean evaluate(Boolean left, Boolean right);

		@Override
		public Boolean evaluate(List<?> values) throws EvaluationException {

			if (values.size() < 2) {
				throw new EvaluationException("Wrong number of arguments: expected 2, got " +
						values.size());
			}

			Boolean result = asBoolean(values.get(0));

			for (int i = 1; i < values.size(); i++) {
				result = evaluate(result, asBoolean(values.get(i)));
			}

			return result;
		}
	}

	static {
		putFunction("and", new BinaryBooleanFunction() {

			@Override
			public Boolean evaluate(Boolean left, Boolean right) {
				return Boolean.valueOf(left.booleanValue() & right.booleanValue());
			}
		});

		putFunction("or", new BinaryBooleanFunction() {

			@Override
			public Boolean evaluate(Boolean left, Boolean right) {
				return Boolean.valueOf(left.booleanValue() | right.booleanValue());
			}
		});
	}

	public abstract static class UnaryBooleanFunction implements Function {

		public abstract Boolean evaluate(Boolean value);

		public Boolean evaluate(List<?> values) throws EvaluationException {

			if (values.size() != 1) {
				throw new EvaluationException("Wrong number of arguments: expected 1, got " +
						values.size());
			}

			return evaluate(asBoolean(values.get(0)));
		}
	}

	static {
		putFunction("not", new UnaryBooleanFunction() {

			@Override
			public Boolean evaluate(Boolean value) {
				return Boolean.valueOf(!value.booleanValue());
			}
		});
	}

	public abstract static class ValueListFunction implements Function {

		public abstract Boolean evaluate(Object value, List<?> values);

		public Boolean evaluate(List<?> values) throws EvaluationException {

			if (values.size() < 2) {
				throw new EvaluationException(
						"Wrong number of arguments: expected more than 2, got " +
						values.size());
			}

			return evaluate(values.get(0), values.subList(1, values.size()));
		}
	}

	static {
		putFunction("isIn", new ValueListFunction() {

			@Override
			public Boolean evaluate(Object value, List<?> values) {
				return Boolean.valueOf(values.contains(value));
			}
		});

		putFunction("isNotIn", new ValueListFunction() {

			@Override
			public Boolean evaluate(Object value, List<?> values) {
				return Boolean.valueOf(!values.contains(value));
			}
		});
	}

	static {
		putFunction("if", new Function() {

			public Object evaluate(List<?> values) throws EvaluationException {

				if (values.size() < 2 || values.size() > 3) {
					throw new EvaluationException("Wrong number of arguments:" +
							" expected 2 or 3, got " +
							values.size());
				}

				Boolean flag = asBoolean(values.get(0));

				if (flag.booleanValue()) {
					return values.get(1);
				} else {
					if (values.size() > 2) {
						return values.get(2);
					}

					// XXX
					return null;
				}
			}
		});
	}

	public abstract static class StringFunction implements Function {

		public abstract String evaluate(String value);

		public String evaluate(List<?> values) throws EvaluationException {

			if (values.size() != 1) {
				throw new EvaluationException("Wrong number of arguments: expected 1, got " +
						values.size());
			}

			return evaluate(asString(values.get(0)));
		}
	}

	static {
		putFunction("uppercase", new StringFunction() {

			@Override
			public String evaluate(String value) {
				return value.toUpperCase();
			}
		});

		putFunction("lowercase", new StringFunction() {

			@Override
			public String evaluate(String value) {
				return value.toLowerCase();
			}
		});

		putFunction("substring", new Function() {
			@Override
			public String evaluate(List<?> values) throws EvaluationException {

				if (values.size() != 3) {
					throw new EvaluationException("Wrong number of arguments: expected 3, got " +
							values.size());
				}

				String value = asString(values.get(0));

				int position = asInteger(values.get(1));
				int length = asInteger(values.get(2));

				if (position <= 0 || length < 0) {
					throw new EvaluationException("Invalid position");
				}

				return value.substring(position - 1, (position + length) - 1);
			}
		});

		putFunction("trimBlanks", new StringFunction() {

			@Override
			public String evaluate(String value) {
				return value.trim();
			}
		});
	}
}
