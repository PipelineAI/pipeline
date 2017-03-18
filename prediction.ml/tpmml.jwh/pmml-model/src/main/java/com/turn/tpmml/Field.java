/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml;

import javax.xml.bind.annotation.*;

@SuppressWarnings("restriction")
@XmlTransient
abstract
public class Field extends PMMLObject implements HasName {

	private static final long serialVersionUID = 1L;

	abstract
	public FieldName getName();

	abstract
	public void setName(FieldName name);

	abstract
	public OpType getOptype();

	abstract
	public void setOptype(OpType opType);
}
