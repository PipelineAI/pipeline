/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.DerivedField;
import com.turn.tpmml.FieldName;
import com.turn.tpmml.manager.ModelManager;
import com.turn.tpmml.manager.ModelManagerException;

import java.util.Map;

public class ModelManagerEvaluationContext extends EvaluationContext {

	private ModelManager<?> modelManager = null;

	public ModelManagerEvaluationContext(ModelManager<?> modelManager,
										Map<FieldName, ?> parameters) {
		super(parameters);

		setModelManager(modelManager);
	}

	@Override
	public DerivedField resolve(FieldName name) throws EvaluationException {
		ModelManager<?> modelManager = getModelManager();

		try {
			return modelManager.resolve(name);
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}
	}

	@Override
	public ModelManagerEvaluationContext clone() {
		return (ModelManagerEvaluationContext) super.clone();
	}

	public ModelManager<?> getModelManager() {
		return this.modelManager;
	}

	private void setModelManager(ModelManager<?> modelManager) {
		this.modelManager = modelManager;
	}
}
