/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml;

import java.util.List;
import javax.xml.bind.annotation.XmlTransient;


@SuppressWarnings("restriction")
@XmlTransient
abstract public class ArrayBase extends PMMLObject {

	private static final long serialVersionUID = 1L;

	@XmlTransient
	private List<String> content = null;

	/**
	 * Gets the raw value.
	 */
	public abstract String getValue();

	/**
	 * Sets the raw value.
	 */
	public abstract void setValue(String value);

	/**
	 * Gets the parsed sequence of values.
	 * 
	 * @see #getValue()
	 */
	public List<String> getContent() {
		return this.content;
	}

	/**
	 * Sets the parsed sequence of values.
	 * 
	 * It is the responsibility of application developer
	 * to maintain the consistency between the raw
	 * value and the parsed sequence of values.
	 * 
	 * @see #setValue(String)
	 */
	public void setContent(List<String> content) {
		this.content = content;
	}
}
