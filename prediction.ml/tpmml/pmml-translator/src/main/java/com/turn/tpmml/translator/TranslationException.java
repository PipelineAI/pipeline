package com.turn.tpmml.translator;

import com.turn.tpmml.manager.ModelManagerException;

/**
 * Generic translation exception
 *
 * @author asvirsky
 *
 */
public class TranslationException extends ModelManagerException {

	private static final long serialVersionUID = 1L;

	public TranslationException(String message) {
		super(TPMMLCause.GENERAL, message);
	}
	
	public TranslationException(TPMMLCause cause, Object... args) {
		super(cause, args);
	}

	public TranslationException(Throwable t) {
		super(t);
	}
}
