/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

public interface Computable<V> {

	V getResult() throws EvaluationException;
}
