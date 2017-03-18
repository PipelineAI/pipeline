/*
 * Copyright (c) 2009 University of Tartu
 */
package com.turn.tpmml.evaluator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class PredicateUtilTest {

	@Test
	public void binaryAnd() {
		assertEquals(Boolean.TRUE, PredicateUtil.binaryAnd(Boolean.TRUE, Boolean.TRUE));
		assertEquals(Boolean.FALSE, PredicateUtil.binaryAnd(Boolean.TRUE, Boolean.FALSE));
		assertEquals(null, PredicateUtil.binaryAnd(Boolean.TRUE, null));
		assertEquals(Boolean.FALSE, PredicateUtil.binaryAnd(Boolean.FALSE, Boolean.TRUE));
		assertEquals(Boolean.FALSE, PredicateUtil.binaryAnd(Boolean.FALSE, Boolean.FALSE));
		assertEquals(Boolean.FALSE, PredicateUtil.binaryAnd(Boolean.FALSE, null));
		assertEquals(null, PredicateUtil.binaryAnd(null, Boolean.TRUE));
		assertEquals(Boolean.FALSE, PredicateUtil.binaryAnd(null, Boolean.FALSE));
		assertEquals(null, PredicateUtil.binaryAnd(null, null));
	}

	@Test
	public void binaryOr() {
		assertEquals(Boolean.TRUE, PredicateUtil.binaryOr(Boolean.TRUE, Boolean.TRUE));
		assertEquals(Boolean.TRUE, PredicateUtil.binaryOr(Boolean.TRUE, Boolean.FALSE));
		assertEquals(Boolean.TRUE, PredicateUtil.binaryOr(Boolean.TRUE, null));
		assertEquals(Boolean.TRUE, PredicateUtil.binaryOr(Boolean.FALSE, Boolean.TRUE));
		assertEquals(Boolean.FALSE, PredicateUtil.binaryOr(Boolean.FALSE, Boolean.FALSE));
		assertEquals(null, PredicateUtil.binaryOr(Boolean.FALSE, null));
		assertEquals(Boolean.TRUE, PredicateUtil.binaryOr(null, Boolean.TRUE));
		assertEquals(null, PredicateUtil.binaryOr(null, Boolean.FALSE));
		assertEquals(null, PredicateUtil.binaryOr(null, null));
	}

	@Test
	public void binaryXor() {
		assertEquals(Boolean.FALSE, PredicateUtil.binaryXor(Boolean.TRUE, Boolean.TRUE));
		assertEquals(Boolean.TRUE, PredicateUtil.binaryXor(Boolean.TRUE, Boolean.FALSE));
		assertEquals(null, PredicateUtil.binaryXor(Boolean.TRUE, null));
		assertEquals(Boolean.TRUE, PredicateUtil.binaryXor(Boolean.FALSE, Boolean.TRUE));
		assertEquals(Boolean.FALSE, PredicateUtil.binaryXor(Boolean.FALSE, Boolean.FALSE));
		assertEquals(null, PredicateUtil.binaryXor(Boolean.FALSE, null));
		assertEquals(null, PredicateUtil.binaryXor(null, Boolean.TRUE));
		assertEquals(null, PredicateUtil.binaryXor(null, Boolean.FALSE));
		assertEquals(null, PredicateUtil.binaryXor(null, null));
	}
}
