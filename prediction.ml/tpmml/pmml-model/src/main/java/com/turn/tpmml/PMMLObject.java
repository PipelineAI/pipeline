/*
 * Copyright (c) 2009 University of Tartu
 */
package com.turn.tpmml;

import java.io.*;

import javax.xml.bind.annotation.*;

import org.jvnet.jaxb2_commons.lang.*;
import org.jvnet.jaxb2_commons.locator.*;

@SuppressWarnings("restriction")
@XmlTransient
abstract
public class PMMLObject implements Equals, HashCode, ToString, Serializable {

	private static final long serialVersionUID = 1L;

	public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object that, EqualsStrategy equalsStrategy){
		return true;
	}

	public int hashCode(ObjectLocator locator, HashCodeStrategy hashCodeStrategy){
		return 1;
	}

	public StringBuilder append(ObjectLocator locator, StringBuilder builder, ToStringStrategy toStringStrategy){
		return builder;
	}

	public StringBuilder appendFields(ObjectLocator locator, StringBuilder builder, ToStringStrategy toStringStrategy) {
		return builder;
	}
}
