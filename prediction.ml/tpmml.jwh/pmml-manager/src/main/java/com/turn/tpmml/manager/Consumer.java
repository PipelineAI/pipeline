/*
 * Copyright (c) 2012 University of Tartu
 */
package com.turn.tpmml.manager;

import com.turn.tpmml.DataField;
import com.turn.tpmml.FieldName;
import com.turn.tpmml.MiningField;
import com.turn.tpmml.OutputField;

import java.io.Serializable;
import java.util.List;

public interface Consumer extends Serializable {

	/**
	 * Returns a short description of the underlying {@link Model}
	 */
	String getSummary();

	/**
	 * Gets the definition of a field from the {@link DataDictionary}.
	 *
	 * @see PMMLManager#getDataField(FieldName)
	 */
	DataField getDataField(FieldName name);

	/**
	 * Gets the independent (ie. input) fields of a {@link Model} from its {@link MiningSchema}.
	 * @throws ModelManagerException 
	 *
	 * @see ModelManager#getActiveFields()
	 */
	List<FieldName> getActiveFields() throws ModelManagerException;

	/**
	 * Gets the dependent (ie. output) field(s) of a {@link Model} from its {@link MiningSchema}.
	 * @throws ModelManagerException 
	 *
	 * @see ModelManager#getPredictedFields()
	 */
	List<FieldName> getPredictedFields() throws ModelManagerException;

	/**
	 * Gets the definition of a field from the {@link MiningSchema}.
	 * @throws ModelManagerException 
	 *
	 * @see #getActiveFields()
	 * @see #getPredictedFields()
	 *
	 * @see ModelManager#getMiningField(FieldName)
	 */
	MiningField getMiningField(FieldName name) throws ModelManagerException;

	/**
	 * Gets the output fields of a {@link Model} from its {@link Output}.
	 * @throws ModelManagerException 
	 *
	 * @see ModelManager#getOutputFields()
	 */
	List<FieldName> getOutputFields() throws ModelManagerException;

	/**
	 * Gets the definition of a field from the {@link Output}
	 * @throws ModelManagerException 
	 *
	 * @see #getOutputFields()
	 */
	OutputField getOutputField(FieldName name) throws ModelManagerException;
}
