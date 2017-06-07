/*
 * Copyright (c) 2012 University of Tartu
 */
package com.turn.tpmml.manager;

/**
 * Generic exception when something happens within ModelManager.
 * 
 */
public class ModelManagerException extends TPMMLException {

	private static final long serialVersionUID = 1L;

	public ModelManagerException(String message) {
		super(message);
	}
	
	public ModelManagerException(TPMMLCause c, Object... args) {
		super(c, args);
	}
	
	public ModelManagerException(Throwable e) {
		super(e);
	}
}
