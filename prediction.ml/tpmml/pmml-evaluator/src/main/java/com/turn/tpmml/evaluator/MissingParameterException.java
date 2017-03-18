/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.DerivedField;
import com.turn.tpmml.FieldName;

public class MissingParameterException extends EvaluationException {

	private static final long serialVersionUID = 1L;

	public MissingParameterException(FieldName name) {
		super(name != null ? name.getValue() : "(empty)");
	}

	public MissingParameterException(DerivedField derivedField) {
		this(derivedField.getName());
	}
}
