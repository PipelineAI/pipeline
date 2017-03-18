/*
 * Copyright (c) 2012 University of Tartu
 */
package com.turn.tpmml;

import java.util.List;

import javax.xml.bind.annotation.XmlTransient;


@SuppressWarnings("restriction")
@XmlTransient
abstract public class SparseArray extends PMMLObject {

	private static final long serialVersionUID = 1L;

	public abstract List<Integer> getIndices();

	public abstract Integer getN();

	public abstract void setN(Integer n);
}
