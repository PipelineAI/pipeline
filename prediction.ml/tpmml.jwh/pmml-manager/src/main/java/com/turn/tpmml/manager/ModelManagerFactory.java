/*
 * Copyright (c) 2010 University of Tartu
 */
package com.turn.tpmml.manager;

import com.turn.tpmml.MiningModel;
import com.turn.tpmml.Model;
import com.turn.tpmml.NeuralNetwork;
import com.turn.tpmml.PMML;
import com.turn.tpmml.RegressionModel;
import com.turn.tpmml.Scorecard;
import com.turn.tpmml.TreeModel;

/**
 * Allow to get a manager for a model.
 */
public class ModelManagerFactory {

	protected boolean alternateImplementation = true;

	protected ModelManagerFactory() {
	}

	public ModelManager<? extends Model> getModelManager(PMML pmml, Model model)
			throws ModelManagerException {

		if (model instanceof RegressionModel) {
			return new RegressionModelManager(pmml, (RegressionModel) model);
		} else

		if (model instanceof TreeModel) {
			return new TreeModelManager(pmml, (TreeModel) model);
		} else

		if (model instanceof NeuralNetwork) {
			return new NeuralNetworkManager(pmml, (NeuralNetwork) model);
		} else

		if (model instanceof MiningModel) {
			return new MiningModelManager(pmml, (MiningModel) model);
		} else

		if (model instanceof Scorecard) {
			return new ScoreCardModelManager(pmml, (Scorecard) model);
		}

		throw new ModelManagerException(ModelManagerException.TPMMLCause.UNSUPPORTED_OPERATION,
				model.getModelName());
	}

	public static ModelManagerFactory getInstance() {
		return new ModelManagerFactory();
	}

	public void setAlternateImplementation(boolean alternateImplementation) {
		this.alternateImplementation = alternateImplementation;
	}
}
