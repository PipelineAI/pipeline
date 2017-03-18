/*
 * Copyright (c) 2012 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.MiningModel;
import com.turn.tpmml.Model;
import com.turn.tpmml.NeuralNetwork;
import com.turn.tpmml.PMML;
import com.turn.tpmml.RegressionModel;
import com.turn.tpmml.Scorecard;
import com.turn.tpmml.TreeModel;
import com.turn.tpmml.manager.ModelManager;
import com.turn.tpmml.manager.ModelManagerException;
import com.turn.tpmml.manager.ModelManagerFactory;
import com.turn.tpmml.manager.TPMMLException.TPMMLCause;


public class ModelEvaluatorFactory extends ModelManagerFactory {

	protected ModelEvaluatorFactory() {
	}

	@Override
	public ModelManager<? extends Model> getModelManager(PMML pmml, Model model)
			throws ModelManagerException {

		if (model instanceof RegressionModel) {
			return new RegressionModelEvaluator(pmml, (RegressionModel) model);
		} else

		if (model instanceof TreeModel) {
			if (alternateImplementation) {
				return new TreeModelEvaluator(pmml, (TreeModel) model);
			} else {
				return new TreeModelEvaluator2(pmml, (TreeModel) model);
			}
		} else

		if (model instanceof NeuralNetwork) {
			return new NeuralNetworkEvaluator(pmml, (NeuralNetwork) model);
		} else

		if (model instanceof MiningModel) {
			if (alternateImplementation) {
				return new MiningModelEvaluator(pmml, (MiningModel) model);
			} else {
				return new MiningModelEvaluator2(pmml, (MiningModel) model);
			}
		}
		if (model instanceof Scorecard) {
			return new ScorecardEvaluator(pmml, (Scorecard) model);
		}

		throw new ModelManagerException(TPMMLCause.UNSUPPORTED_OPERATION,
				model.getModelName());
	}

	public static ModelEvaluatorFactory getInstance() {
		return new ModelEvaluatorFactory();
	}
}
