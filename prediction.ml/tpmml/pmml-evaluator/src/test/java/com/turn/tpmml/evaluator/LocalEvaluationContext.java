/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;


import com.turn.tpmml.DerivedField;
import com.turn.tpmml.FieldName;

import java.util.Collections;
import java.util.Map;

public class LocalEvaluationContext extends EvaluationContext {

	public LocalEvaluationContext() {
		super(Collections.<FieldName, Object>emptyMap());
	}

	public LocalEvaluationContext(FieldName name, Object value) {
		super(Collections.<FieldName, Object>singletonMap(name, value));
	}

	public LocalEvaluationContext(Map<FieldName, ?> parameters) {
		super(parameters);
	}

	@Override
	public DerivedField resolve(FieldName name) {
		return null;
	}
}
