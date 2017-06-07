/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.Interval;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DiscretizationUtilTest {

	@Test
	public void contains() throws EvaluationException {
		Double min = Double.valueOf(Integer.MIN_VALUE);
		Double max = Double.valueOf(Integer.MAX_VALUE);

		Interval negative = createInterval(Interval.Closure.OPEN_OPEN, min, 0.0);
		assertTrue(DiscretizationUtil.contains(negative, -1.0));
		assertFalse(DiscretizationUtil.contains(negative, 0.0));

		Interval negativeNull = createInterval(Interval.Closure.OPEN_OPEN, null, 0.0);
		assertTrue(DiscretizationUtil.contains(negativeNull, -1.0));
		assertFalse(DiscretizationUtil.contains(negativeNull, 0.0));

		Interval positive = createInterval(Interval.Closure.OPEN_OPEN, 0.0, max);
		assertFalse(DiscretizationUtil.contains(positive, 0.0));
		assertTrue(DiscretizationUtil.contains(positive, 1.0));

		Interval positiveNull = createInterval(Interval.Closure.OPEN_OPEN, 0.0, null);
		assertFalse(DiscretizationUtil.contains(positiveNull, 0.0));
		assertTrue(DiscretizationUtil.contains(positiveNull, 1.0));

		Interval negativeAndZero = createInterval(Interval.Closure.OPEN_CLOSED, min, 0.0);
		assertTrue(DiscretizationUtil.contains(negativeAndZero, -1.0));
		assertTrue(DiscretizationUtil.contains(negativeAndZero, 0.0));

		Interval zeroAndPositive = createInterval(Interval.Closure.CLOSED_OPEN, 0.0, max);
		assertTrue(DiscretizationUtil.contains(zeroAndPositive, 0.0));
		assertTrue(DiscretizationUtil.contains(zeroAndPositive, 1.0));
	}

	private static Interval createInterval(Interval.Closure closure, Double left, Double right) {
		Interval interval = new Interval(closure);
		interval.setLeftMargin(left);
		interval.setRightMargin(right);

		return interval;
	}
}
