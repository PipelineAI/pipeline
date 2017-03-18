/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.DerivedField;
import com.turn.tpmml.FieldName;

import java.util.LinkedHashMap;
import java.util.Map;


public abstract class EvaluationContext implements Cloneable {

	private Map<FieldName, ?> parameters = null;

	public EvaluationContext(Map<FieldName, ?> parameters) {
		setParameters(parameters);
	}

	public abstract DerivedField resolve(FieldName name) throws EvaluationException;

	@Override
	public EvaluationContext clone() {
		try {
			EvaluationContext result = (EvaluationContext) super.clone();

			// Deep copy parameters
			Map<FieldName, Object> parameters = new LinkedHashMap<FieldName, Object>(
					getParameters());
			result.setParameters(parameters);

			return result;
		} catch (CloneNotSupportedException cnse) {
			throw new AssertionError(cnse);
		}
	}

	public Object getParameter(FieldName name) {
		Map<FieldName, ?> parameters = getParameters();

		return parameters.get(name);
	}

	@SuppressWarnings({ "unchecked" })
	void putParameter(FieldName name, Object value) {
		// Use cast to remove the implicit "read-only" protection
		Map<FieldName, Object> parameters = (Map<FieldName, Object>) getParameters();

		parameters.put(name, value);
	}

	public Map<FieldName, ?> getParameters() {
		return this.parameters;
	}

	void setParameters(Map<FieldName, ?> parameters) {
		this.parameters = parameters;
	}
}
