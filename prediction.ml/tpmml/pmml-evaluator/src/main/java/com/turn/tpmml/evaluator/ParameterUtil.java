/*
 * Copyright (c) 2012 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.DataField;
import com.turn.tpmml.DataType;
import com.turn.tpmml.Interval;
import com.turn.tpmml.InvalidValueTreatmentMethodType;
import com.turn.tpmml.MiningField;
import com.turn.tpmml.OpType;
import com.turn.tpmml.OutlierTreatmentMethodType;
import com.turn.tpmml.Value;
import com.turn.tpmml.manager.TPMMLException.TPMMLCause;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParameterUtil {

	private ParameterUtil() {
	}

	@SuppressWarnings("unused")
	public static Object prepare(DataField dataField, MiningField miningField, Object value)
			throws EvaluationException {

		if (dataField == null) {
			throw new EvaluationException("DataField is null");
		}

		outlierTreatment: if (isOutlier(dataField, value)) {

			if (miningField == null) {
				throw new EvaluationException("MiningField is null");
			}

			OutlierTreatmentMethodType outlierTreatmentMethod = miningField.getOutlierTreatment();
			switch (outlierTreatmentMethod) {
			case AS_IS:
				break;
			case AS_MISSING_VALUES:
				value = null;
				break;
			case AS_EXTREME_VALUES:
				Double lowValue = miningField.getLowValue();
				Double highValue = miningField.getHighValue();

				if (lowValue == null || highValue == null) {
					throw new EvaluationException("There are no limits");
				}

				DataType dataType = dataField.getDataType();

				if (compare(dataType, value, lowValue) < 0) {
					value = lowValue;
				} else

				if (compare(dataType, value, highValue) > 0) {
					value = highValue;
				}
				break;
			default:
				throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
						outlierTreatmentMethod.name());
			}
		}

		missingValueTreatment: if (isMissing(dataField, value)) {

			if (miningField == null) {
				return null;
			}

			value = miningField.getMissingValueReplacement();
			if (value != null) {
				break missingValueTreatment;
			}

			return null;
		}

		invalidValueTreatment: if (isInvalid(dataField, value)) {

			if (miningField == null) {
				throw new EvaluationException("MiningField is null");
			}

			InvalidValueTreatmentMethodType invalidValueTreatmentMethod =
					miningField.getInvalidValueTreatment();
			switch (invalidValueTreatmentMethod) {
			case RETURN_INVALID:
				throw new EvaluationException("Invalid evaluation");
			case AS_IS:
				break invalidValueTreatment;
			case AS_MISSING:
				value = miningField.getMissingValueReplacement();
				if (value != null) {
					break invalidValueTreatment;
				}

				return null;
			default:
				throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
						invalidValueTreatmentMethod.name());
			}
		}

		return cast(dataField.getDataType(), value);
	}

	private static boolean isOutlier(DataField dataField, Object value) throws EvaluationException {

		if (value == null) {
			return false;
		}

		OpType opType = dataField.getOptype();
		switch (opType) {
		case CONTINUOUS:
			List<Double> range = new ArrayList<Double>();

			List<Interval> fieldIntervals = dataField.getIntervals();
			for (Interval fieldInterval : fieldIntervals) {
				range.add(fieldInterval.getLeftMargin());
				range.add(fieldInterval.getRightMargin());
			}

			List<Value> fieldValues = dataField.getValues();
			for (Value fieldValue : fieldValues) {
				Value.Property property = fieldValue.getProperty();

				switch (property) {
				case VALID:
					range.add(toDouble(fieldValue.getValue()));
					break;
				default:
					break;
				}
			}

			if (range.isEmpty()) {
				return false;
			}

			Double doubleValue = toDouble(value);

			Double minValue = Collections.min(range);
			if ((doubleValue).compareTo(minValue) < 0) {
				return true;
			}

			Double maxValue = Collections.max(range);
			if ((doubleValue).compareTo(maxValue) > 0) {
				return true;
			}
			break;
		case CATEGORICAL:
		case ORDINAL:
			break;
		default:
			throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION, opType.name());
		}

		return false;
	}

	private static boolean isMissing(DataField dataField, Object value) throws EvaluationException {

		if (value == null) {
			return true;
		}

		List<Value> fieldValues = dataField.getValues();
		for (Value fieldValue : fieldValues) {
			Value.Property property = fieldValue.getProperty();

			switch (property) {
			case MISSING:
				boolean equals = equals(DataType.STRING, value, fieldValue.getValue());
				if (equals) {
					return true;
				}
				break;
			default:
				break;
			}
		}

		return false;
	}

	private static boolean isInvalid(DataField dataField, Object value) throws EvaluationException {
		return !isValid(dataField, value);
	}

	@SuppressWarnings("fallthrough")
	private static boolean isValid(DataField dataField, Object value) throws EvaluationException {
		DataType dataType = dataField.getDataType();

		// Speed up subsequent conversions
		value = cast(dataType, value);

		OpType opType = dataField.getOptype();
		switch (opType) {
		case CONTINUOUS:
			Double doubleValue = toDouble(value);

			int intervalCount = 0;

			List<Interval> fieldIntervals = dataField.getIntervals();
			for (Interval fieldInterval : fieldIntervals) {
				++intervalCount;

				if (DiscretizationUtil.contains(fieldInterval, doubleValue)) {
					return true;
				}
			}

			if (intervalCount > 0) {
				return false;
			}
			// Falls through
		case CATEGORICAL:
		case ORDINAL:
			int validValueCount = 0;

			List<Value> fieldValues = dataField.getValues();
			for (Value fieldValue : fieldValues) {
				Value.Property property = fieldValue.getProperty();

				switch (property) {
				case VALID:
					validValueCount += 1;

					boolean equals = equals(dataType, value, fieldValue.getValue());
					if (equals) {
						return true;
					}
					break;
				case INVALID:
					boolean equals2 = equals(dataType, value, fieldValue.getValue());
					if (equals2) {
						return false;
					}
					break;
				case MISSING:
					break;
				default:
					throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
							property.name());
				}
			}

			if (validValueCount > 0) {
				return false;
			}
			break;
		default:
			throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
					opType.name());
		}

		return true;
	}

	/**
	 * Checks the equality between different value representations.
	 * 
	 * @param value
	 *            The {@link #getDataType(Object) runtime data type representation} of the value.
	 * @param string
	 *            The String representation of the value.
	 * @throws EvaluationException
	 */
	public static boolean equals(Object value, String string) throws EvaluationException {
		return equals(getDataType(value), value, string);
	}

	private static boolean equals(DataType dataType, Object left, Object right)
			throws EvaluationException {
		return (cast(dataType, left)).equals(cast(dataType, right));
	}

	/**
	 * Calculates the order between the value and the reference value.
	 * 
	 * @param value
	 *            The {@link #getDataType(Object) runtime data type representation} of the value.
	 * @param string
	 *            The String representation of the reference value.
	 * @throws EvaluationException
	 * 
	 * @see Comparable#compareTo(Object)
	 */
	public static int compare(Object value, String string) throws EvaluationException {
		return compare(getDataType(value), value, string);
	}

	@SuppressWarnings({ "cast", "rawtypes", "unchecked" })
	private static int compare(DataType dataType, Object left, Object right)
			throws EvaluationException {
		return ((Comparable) cast(dataType, left)).compareTo((Comparable) cast(dataType, right));
	}

	public static Object parse(DataType dataType, String string) throws EvaluationException {

		switch (dataType) {
		case STRING:
			return string;
		case INTEGER:
			return new Integer(string);
		case FLOAT:
			return new Float(string);
		case DOUBLE:
			return new Double(string);
		default:
			break;
		}

		throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
				dataType.name());
	}

	/**
	 * @return The data type of the value.
	 * @throws EvaluationException
	 */
	public static DataType getDataType(Object value) throws EvaluationException {

		if (value instanceof String) {
			return DataType.STRING;
		} else if (value instanceof Integer) {
			return DataType.INTEGER;
		} else if (value instanceof Float) {
			return DataType.FLOAT;
		} else if (value instanceof Double) {
			return DataType.DOUBLE;
		}

		throw new EvaluationException(value + " is not a valid type.");
	}

	/**
	 * @return The least restrictive data type of the data types of two values
	 * @throws EvaluationException
	 * 
	 * @see #getResultDataType(DataType, DataType)
	 */
	public static DataType getResultDataType(Object left, Object right) throws EvaluationException {
		return getResultDataType(getDataType(left), getDataType(right));
	}

	public static DataType getResultDataType(DataType left, DataType right)
			throws EvaluationException {

		if ((left).equals(right)) {
			return left;
		} // End if

		DataType[] dataTypes = ParameterUtil.PRECEDENCE_SEQUENCE;
		for (DataType dataType : dataTypes) {

			if ((dataType).equals(left) || (dataType).equals(right)) {
				return dataType;
			}
		}

		throw new EvaluationException("Unable to find a result data type");
	}

	public static Object cast(DataType dataType, Object value) throws EvaluationException {

		switch (dataType) {
		case STRING:
			return toString(value);
		case INTEGER:
			return toInteger(value);
		case FLOAT:
			return toFloat(value);
		case DOUBLE:
			return toDouble(value);
		default:
			break;
		}

		throw new EvaluationException("Can't cast [" + value + "] to " + dataType.name());
	}

	/**
	 * Converts the specified value to String data type.
	 * @throws EvaluationException 
	 * 
	 * @see DataType#STRING
	 */
	public static String toString(Object value) throws EvaluationException {

		if (value instanceof String) {
			return (String) value;
		} else
		if (value instanceof Number) {
			Number number = (Number) value;

			return number.toString();
		}

		throw new EvaluationException("Can't transform [" + value + "] into a string");
	}

	/**
	 * Converts the specified value to Integer data type.
	 * @throws EvaluationException 
	 * 
	 * @see DataType#INTEGER
	 */
	public static Integer toInteger(Object value) throws EvaluationException {

		if (value instanceof String) {
			String string = (String) value;

			return Integer.valueOf(string);
		} else

		if (value instanceof Integer) {
			return (Integer) value;
		} else

		if (value instanceof Number) {
			Number number = (Number) value;

			return Integer.valueOf(number.intValue());
		}

		throw new EvaluationException("Can't cast [" + value + "] to an integer");
	}

	/**
	 * Converts the specified value to Float data type.
	 * @throws EvaluationException 
	 * 
	 * @see DataType#FLOAT
	 */
	public static Float toFloat(Object value) throws EvaluationException {

		if (value instanceof String) {
			String string = (String) value;

			return Float.valueOf(string);
		} else

		if (value instanceof Float) {
			return (Float) value;
		} else

		if (value instanceof Number) {
			Number number = (Number) value;

			return Float.valueOf(number.floatValue());
		}

		throw new EvaluationException("Can't cast [" + value + "] to a float");
	}

	/**
	 * Converts the specified value to Double data type.
	 * @throws EvaluationException 
	 * 
	 * @see DataType#DOUBLE
	 */
	public static Double toDouble(Object value) throws EvaluationException {

		if (value instanceof String) {
			String string = (String) value;

			return Double.valueOf(string);
		} else if (value instanceof Double) {
			return (Double) value;
		} else if (value instanceof Number) {
			Number number = (Number) value;

			return Double.valueOf(number.doubleValue());
		}

		throw new EvaluationException("Can't cast [" + value + "] to a double");
	}

	public static DataType getConstantDataType(String string) {

		try {
			if (string.indexOf('.') > -1) {
				Float.parseFloat(string);

				return DataType.FLOAT;
			} else {
				Integer.parseInt(string);

				return DataType.INTEGER;
			}
		} catch (NumberFormatException nfe) {
			return DataType.STRING;
		}
	}

	private static final DataType[] PRECEDENCE_SEQUENCE = { DataType.STRING, DataType.DOUBLE,
			DataType.FLOAT, DataType.INTEGER };
}
