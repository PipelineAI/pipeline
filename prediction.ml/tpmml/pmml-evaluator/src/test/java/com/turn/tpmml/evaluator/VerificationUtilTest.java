/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class VerificationUtilTest {

	@Test
	public void acceptable() {
		double precision = 0.001;
		double zeroThreshold = (precision * precision);

		assertTrue(VerificationUtil.acceptable(1.0, 1.0, precision, zeroThreshold));

		assertTrue(VerificationUtil.acceptable(1.0, 0.999, precision, zeroThreshold));
		assertFalse(VerificationUtil.acceptable(1.0, 0.99895, precision, zeroThreshold));

		assertTrue(VerificationUtil.acceptable(1.0, 1.001, precision, zeroThreshold));
		assertFalse(VerificationUtil.acceptable(1.0, 1.00105, precision, zeroThreshold));

		assertTrue(VerificationUtil.acceptable(-1.0, -1.0, precision, zeroThreshold));

		assertTrue(VerificationUtil.acceptable(-1.0, -1.001, precision, zeroThreshold));
		assertFalse(VerificationUtil.acceptable(-1.0, -1.00105, precision, zeroThreshold));

		assertTrue(VerificationUtil.acceptable(-1.0, -0.999, precision, zeroThreshold));
		assertFalse(VerificationUtil.acceptable(-1.0, -0.99895, precision, zeroThreshold));
	}

	@Test
	public void isZero() {
		double zeroThreshold = 0.001;

		assertTrue(VerificationUtil.isZero(0.0005, zeroThreshold));
		assertTrue(VerificationUtil.isZero(0, zeroThreshold));
		assertTrue(VerificationUtil.isZero(-0.0005, zeroThreshold));

		assertFalse(VerificationUtil.isZero(0.0015, zeroThreshold));
		assertFalse(VerificationUtil.isZero(-0.0015, zeroThreshold));
	}
}
