/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

public interface Classification extends Computable<String> {

	Double getProbability(String value);
}
