/*
 * Copyright (c) 2012 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.ActivationFunctionType;
import com.turn.tpmml.Connection;
import com.turn.tpmml.DerivedField;
import com.turn.tpmml.Expression;
import com.turn.tpmml.FieldName;
import com.turn.tpmml.FieldRef;
import com.turn.tpmml.MiningFunctionType;
import com.turn.tpmml.NeuralInput;
import com.turn.tpmml.NeuralLayer;
import com.turn.tpmml.NeuralNetwork;
import com.turn.tpmml.NeuralOutput;
import com.turn.tpmml.Neuron;
import com.turn.tpmml.NnNormalizationMethodType;
import com.turn.tpmml.NormContinuous;
import com.turn.tpmml.NormDiscrete;
import com.turn.tpmml.PMML;
import com.turn.tpmml.manager.IPMMLResult;
import com.turn.tpmml.manager.ModelManagerException;
import com.turn.tpmml.manager.NeuralNetworkManager;
import com.turn.tpmml.manager.PMMLResult;
import com.turn.tpmml.manager.TPMMLException.TPMMLCause;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NeuralNetworkEvaluator extends NeuralNetworkManager implements Evaluator {

	private static final long serialVersionUID = 1L;

	public NeuralNetworkEvaluator(PMML pmml) throws ModelManagerException {
		super(pmml);
	}

	public NeuralNetworkEvaluator(PMML pmml, NeuralNetwork neuralNetwork)
			throws ModelManagerException {
		super(pmml, neuralNetwork);
	}

	public NeuralNetworkEvaluator(NeuralNetworkManager parent) throws ModelManagerException {
		this(parent.getPmml(), parent.getModel());
	}

	public Object prepare(FieldName name, Object value) throws EvaluationException {
		try {
			return ParameterUtil.prepare(getDataField(name), getMiningField(name), value);
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}
	}

	/**
	 * @throws EvaluationException
	 * @see #evaluateRegression(EvaluationContext)
	 * @see #evaluateClassification(EvaluationContext)
	 */
	@Override
	public IPMMLResult evaluate(Map<FieldName, ?> parameters) throws EvaluationException {
		NeuralNetwork neuralNetwork;
		try {
			neuralNetwork = getModel();
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}

		Map<FieldName, ?> predictions;

		ModelManagerEvaluationContext context = new ModelManagerEvaluationContext(this, parameters);

		MiningFunctionType miningFunction = neuralNetwork.getFunctionName();
		switch (miningFunction) {
		case REGRESSION:
			predictions = evaluateRegression(context);
			break;
		case CLASSIFICATION:
			predictions = evaluateClassification(context);
			break;
		default:
			throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION, miningFunction.name());
		}

		PMMLResult result = new PMMLResult();
		result = OutputUtil.evaluate(predictions, context);

		return result;
	}

	public Map<FieldName, Double> evaluateRegression(EvaluationContext context)
			throws EvaluationException {
		Map<FieldName, Double> result = new LinkedHashMap<FieldName, Double>();

		Map<String, Double> neuronOutputs = evaluateRaw(context);

		List<NeuralOutput> neuralOutputs;
		try {
			neuralOutputs = getOrCreateNeuralOutputs();
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}
		for (NeuralOutput neuralOutput : neuralOutputs) {
			String id = neuralOutput.getOutputNeuron();

			Expression expression = getExpression(neuralOutput.getDerivedField());
			if (expression instanceof FieldRef) {
				FieldRef fieldRef = (FieldRef) expression;

				FieldName field = fieldRef.getField();

				result.put(field, neuronOutputs.get(id));
			} else

			if (expression instanceof NormContinuous) {
				NormContinuous normContinuous = (NormContinuous) expression;

				FieldName field = normContinuous.getField();

				Double value = NormalizationUtil.denormalize(normContinuous, neuronOutputs.get(id));

				result.put(field, value);
			} else {
				throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
						expression.toString());
			}
		}

		return result;
	}

	public Map<FieldName, ClassificationMap> evaluateClassification(EvaluationContext context)
			throws EvaluationException {
		Map<FieldName, ClassificationMap> result =
				new LinkedHashMap<FieldName, ClassificationMap>();

		Map<String, Double> neuronOutputs = evaluateRaw(context);

		List<NeuralOutput> neuralOutputs;
		try {
			neuralOutputs = getOrCreateNeuralOutputs();
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}
		for (NeuralOutput neuralOutput : neuralOutputs) {
			String id = neuralOutput.getOutputNeuron();

			Expression expression = getExpression(neuralOutput.getDerivedField());
			if (expression instanceof NormDiscrete) {
				NormDiscrete normDiscrete = (NormDiscrete) expression;

				FieldName field = normDiscrete.getField();

				ClassificationMap values = result.get(field);
				if (values == null) {
					values = new ClassificationMap();

					result.put(field, values);
				}

				Double value = neuronOutputs.get(id);

				values.put(normDiscrete.getValue(), value);
			} else {
				throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
						expression.toString());
			}
		}

		return result;
	}

	private Expression getExpression(DerivedField derivedField) throws EvaluationException {
		Expression expression = derivedField.getExpression();

		if (expression instanceof FieldRef) {
			FieldRef fieldRef = (FieldRef) expression;

			try {
				derivedField = resolve(fieldRef.getField());
			} catch (ModelManagerException e) {
				throw new EvaluationException(e);
			}
			if (derivedField != null) {
				return getExpression(derivedField);
			}

			return fieldRef;
		}

		return expression;
	}

	/**
	 * Evaluates neural network.
	 * 
	 * @return Mapping between Neuron identifiers and their outputs
	 * @throws EvaluationException
	 * 
	 * @see NeuralInput#getId()
	 * @see Neuron#getId()
	 */
	public Map<String, Double> evaluateRaw(EvaluationContext context) throws EvaluationException {
		Map<String, Double> result = new LinkedHashMap<String, Double>();

		List<NeuralInput> neuralInputs;
		try {
			neuralInputs = getNeuralInputs();
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}
		for (NeuralInput neuralInput : neuralInputs) {
			Double value = (Double) ExpressionUtil.evaluate(neuralInput.getDerivedField(), context);
			if (value == null) {
				throw new EvaluationException(TPMMLCause.MISSING_PARAMETER, 
						neuralInput.getDerivedField().getName());
			}

			result.put(neuralInput.getId(), value);
		}

		List<NeuralLayer> neuralLayers;
		try {
			neuralLayers = getNeuralLayers();
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}
		for (NeuralLayer neuralLayer : neuralLayers) {
			List<Neuron> neurons = neuralLayer.getNeurons();

			for (Neuron neuron : neurons) {
				double z = neuron.getBias();

				List<Connection> connections = neuron.getConnections();
				for (Connection connection : connections) {
					double input = result.get(connection.getFrom());

					z += input * connection.getWeight();
				}

				double output = activation(z, neuralLayer);

				result.put(neuron.getId(), output);
			}

			normalizeNeuronOutputs(neuralLayer, result);
		}

		return result;
	}

	private void normalizeNeuronOutputs(NeuralLayer neuralLayer,
			Map<String, Double> neuronOutputs) throws EvaluationException {
		NeuralNetwork model;
		try {
			model = getModel();
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}

		NnNormalizationMethodType normalizationMethod = neuralLayer.getNormalizationMethod();
		if (normalizationMethod == null) {
			normalizationMethod = model.getNormalizationMethod();
		} // End if

		if (normalizationMethod == NnNormalizationMethodType.NONE) {
			return;
		} else

		if (normalizationMethod == NnNormalizationMethodType.SOFTMAX) {
			List<Neuron> neurons = neuralLayer.getNeurons();

			double sum = 0.0;

			for (Neuron neuron : neurons) {
				double output = neuronOutputs.get(neuron.getId());

				sum += Math.exp(output);
			}

			for (Neuron neuron : neurons) {
				double output = neuronOutputs.get(neuron.getId());

				neuronOutputs.put(neuron.getId(), Math.exp(output) / sum);
			}
		} else {
			throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
					normalizationMethod.name());
		}
	}

	private double activation(double z, NeuralLayer layer) throws EvaluationException {
		NeuralNetwork model;
		try {
			model = getModel();
		} catch (ModelManagerException e) {
			throw new EvaluationException(e);
		}

		ActivationFunctionType activationFunction = layer.getActivationFunction();
		if (activationFunction == null) {
			activationFunction = model.getActivationFunction();
		}

		switch (activationFunction) {
		case THRESHOLD:
			Double threshold = layer.getThreshold();
			if (threshold == null) {
				threshold = Double.valueOf(model.getThreshold());
			}
			return z > threshold.doubleValue() ? 1.0 : 0.0;
		case LOGISTIC:
			return 1.0 / (1.0 + Math.exp(-z));
		case TANH:
			return (1.0 - Math.exp(-2.0 * z)) / (1.0 + Math.exp(-2.0 * z));
		case IDENTITY:
			return z;
		case EXPONENTIAL:
			return Math.exp(z);
		case RECIPROCAL:
			return 1.0 / z;
		case SQUARE:
			return z * z;
		case GAUSS:
			return Math.exp(-(z * z));
		case SINE:
			return Math.sin(z);
		case COSINE:
			return Math.cos(z);
		case ELLIOTT:
			return z / (1.0 + Math.abs(z));
		case ARCTAN:
			return Math.atan(z);
		default:
			throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
					activationFunction.name());
		}
	}
}
