/*
 * Copyright (c) 2010 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.manager.ModelManagerException;
import com.turn.tpmml.manager.TPMMLException;

public class EvaluationException extends ModelManagerException {

	private static final long serialVersionUID = 1L;


	public EvaluationException(String message) {
		super(TPMMLCause.GENERAL, message);
	}

	public EvaluationException(TPMMLCause cause, Object... args) {
		super(cause, args);
	}

	public EvaluationException(TPMMLException e) {
		super(e);
	}
}
