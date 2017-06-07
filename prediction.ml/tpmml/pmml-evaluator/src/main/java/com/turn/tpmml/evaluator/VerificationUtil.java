/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

public class VerificationUtil {

	private VerificationUtil() {
	}

	public static boolean acceptable(Object expected, Object actual, double precision,
			double zeroThreshold) {

		if (expected == null) {
			return (actual == null);
		} else {
			if (expected instanceof Number && actual instanceof Number) {
				return acceptable((Number) expected, (Number) actual, precision, zeroThreshold);
			}

			return (expected).equals(actual);
		}
	}

	/**
	 * @param precision The acceptable range given <em>in proportion</em> of the expected value,
	 *            including its boundaries.
	 * @param zeroThreshold The threshold for distinguishing between zero and non-zero values.
	 */
	public static boolean acceptable(Number expected, Number actual, double precision,
			double zeroThreshold) {

		if (isZero(expected, zeroThreshold) && isZero(actual, zeroThreshold)) {
			return true;
		}

		double zeroBoundary = expected.doubleValue() * (1d - precision); // Pointed towards zero
		double infinityBoundary = expected.doubleValue() * (1d + precision); // Pointed towards
																				// positive or
																				// negative infinity

		// positive values
		if (expected.doubleValue() >= 0) {
			return (actual.doubleValue() >= zeroBoundary) &&
					(actual.doubleValue() <= infinityBoundary);
		} else {
			// negative values
			return (actual.doubleValue() <= zeroBoundary) &&
					(actual.doubleValue() >= infinityBoundary);
		}
	}

	public static boolean isZero(Number value, double zeroThreshold) {
		return (value.doubleValue() >= -zeroThreshold) && (value.doubleValue() <= zeroThreshold);
	}
}
