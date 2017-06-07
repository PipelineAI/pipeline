/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.Discretize;
import com.turn.tpmml.DiscretizeBin;
import com.turn.tpmml.InlineTable;
import com.turn.tpmml.Interval;
import com.turn.tpmml.MapValues;
import com.turn.tpmml.TableLocator;
import com.turn.tpmml.manager.TPMMLException.TPMMLCause;

import java.util.List;
import java.util.Map;

public class DiscretizationUtil {

	private DiscretizationUtil() {
	}

	public static String discretize(Discretize discretize, Object value)
			throws EvaluationException {
		Double doubleValue = ParameterUtil.toDouble(value);

		List<DiscretizeBin> bins = discretize.getDiscretizeBins();
		for (DiscretizeBin bin : bins) {
			Interval interval = bin.getInterval();

			if (contains(interval, doubleValue)) {
				return bin.getBinValue();
			}
		}

		return discretize.getDefaultValue();
	}

	public static boolean contains(Interval interval, Double value) throws EvaluationException {
		Double left = interval.getLeftMargin();
		Double right = interval.getRightMargin();

		Interval.Closure closure = interval.getClosure();
		switch (closure) {
		case OPEN_CLOSED:
			return greaterThan(left, value) && lessOrEqual(right, value);
		case OPEN_OPEN:
			return greaterThan(left, value) && lessThan(right, value);
		case CLOSED_OPEN:
			return greaterOrEqual(left, value) && lessThan(right, value);
		case CLOSED_CLOSED:
			return greaterOrEqual(left, value) && lessOrEqual(right, value);
		default:
			throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION, closure.name());
		}
	}

	private static boolean lessThan(Double reference, Double value) {
		return (reference != null ? (value).compareTo(reference) < 0 : true);
	}

	private static boolean lessOrEqual(Double reference, Double value) {
		return (reference != null ? (value).compareTo(reference) <= 0 : true);
	}

	private static boolean greaterThan(Double reference, Double value) {
		return (reference != null ? (value).compareTo(reference) > 0 : true);
	}

	private static boolean greaterOrEqual(Double reference, Double value) {
		return (reference != null ? (value).compareTo(reference) >= 0 : true);
	}

	public static String mapValue(MapValues mapValues, Map<String, Object> values)
			throws EvaluationException {
		InlineTable table = mapValues.getInlineTable();

		if (table != null) {
			List<Map<String, String>> rows = TableUtil.parse(table);

			Map<String, String> row = TableUtil.match(rows, values);
			if (row != null) {
				String result = row.get(mapValues.getOutputColumn());
				if (result == null) {
					throw new EvaluationException("There is no result here");
				}

				return result;
			}
		} else {
			TableLocator tableLocator = mapValues.getTableLocator();
			if (tableLocator != null) {
				throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
						tableLocator.toString());
			}
		}

		return mapValues.getDefaultValue();
	}
}
