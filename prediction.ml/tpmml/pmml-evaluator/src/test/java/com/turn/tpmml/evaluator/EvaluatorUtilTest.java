/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class EvaluatorUtilTest {

	@Test
	public void decode() throws EvaluationException {
		Computable<String> value = new Computable<String>() {

			public String getResult() {
				return "value";
			}
		};

		assertEquals("value", EvaluatorUtil.decode(value));
	}
}
