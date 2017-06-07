/*
 * Copyright (c) 2011 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.FieldName;
import com.turn.tpmml.LinearNorm;
import com.turn.tpmml.NormContinuous;
import com.turn.tpmml.OutlierTreatmentMethodType;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NormalizationUtilTest {

	private NormContinuous norm;

	@Before
	public void setUp() {
		norm = new NormContinuous(new FieldName("x"));
		norm.getLinearNorms().add(new LinearNorm(0.01, 0.0));
		norm.getLinearNorms().add(new LinearNorm(3.07897, 0.5));
		norm.getLinearNorms().add(new LinearNorm(11.44, 1.0));
	}

	@Test
	public void testNormalize() throws EvaluationException {
		assertEquals(0.00000, NormalizationUtil.normalize(norm, 0.01), 1e-5);
		assertEquals(0.19583, NormalizationUtil.normalize(norm, 1.212), 1e-5);
		assertEquals(0.50000, NormalizationUtil.normalize(norm, 3.07897), 1e-5);
		assertEquals(0.70458, NormalizationUtil.normalize(norm, 6.5), 1e-5);
		assertEquals(1.00000, NormalizationUtil.normalize(norm, 11.44), 1e-5);
	}

	@Test
	public void testNormalizeOutliers() throws EvaluationException {
		// as is method
		assertEquals(-0.16455, NormalizationUtil.normalize(norm, -1.0), 1e-5);
		assertEquals(1.04544, NormalizationUtil.normalize(norm, 12.2), 1e-5);

		// as missing values method
		norm.setOutliers(OutlierTreatmentMethodType.AS_MISSING_VALUES);
		norm.setMapMissingTo(0.5);
		assertEquals(0.5, NormalizationUtil.normalize(norm, -1.0), 1e-5);
		assertEquals(0.5, NormalizationUtil.normalize(norm, 12.2), 1e-5);

		// as extreme values method
		norm.setOutliers(OutlierTreatmentMethodType.AS_EXTREME_VALUES);
		assertEquals(0.0, NormalizationUtil.normalize(norm, -1.0), 1e-5);
		assertEquals(1.0, NormalizationUtil.normalize(norm, 12.2), 1e-5);
	}

	@Test
	public void testDenormalize() {
		assertEquals(0.010, NormalizationUtil.denormalize(norm, 0.0), 1e-5);
		assertEquals(0.300, NormalizationUtil.denormalize(norm, 0.047247), 1e-5);
		assertEquals(7.123, NormalizationUtil.denormalize(norm, 0.741838), 1e-5);
		assertEquals(11.44, NormalizationUtil.denormalize(norm, 1.0), 1e-5);
	}

}
