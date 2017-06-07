/*
 * Copyright (c) 2011 University of Tartu
 */
package com.turn.tpmml.manager;

import com.turn.tpmml.ActivationFunctionType;
import com.turn.tpmml.Connection;
import com.turn.tpmml.DataType;
import com.turn.tpmml.DerivedField;
import com.turn.tpmml.MiningFunctionType;
import com.turn.tpmml.MiningSchema;
import com.turn.tpmml.NeuralInput;
import com.turn.tpmml.NeuralInputs;
import com.turn.tpmml.NeuralLayer;
import com.turn.tpmml.NeuralNetwork;
import com.turn.tpmml.NeuralOutput;
import com.turn.tpmml.NeuralOutputs;
import com.turn.tpmml.Neuron;
import com.turn.tpmml.NormContinuous;
import com.turn.tpmml.OpType;
import com.turn.tpmml.PMML;

import java.util.List;

public class NeuralNetworkManager extends ModelManager<NeuralNetwork> {

	private static final long serialVersionUID = 1L;

	private NeuralNetwork neuralNetwork = null;

	private int neuronCount = 0;

	public NeuralNetworkManager() {
	}

	public NeuralNetworkManager(PMML pmml) throws ModelManagerException {
		this(pmml, find(pmml.getContent(), NeuralNetwork.class));
	}

	public NeuralNetworkManager(PMML pmml, NeuralNetwork neuralNetwork)
			throws ModelManagerException {
		super(pmml);

		this.neuralNetwork = neuralNetwork;

		if (this.neuralNetwork != null) {
			this.neuronCount = getNeuronCount();
		}
	}

	public String getSummary() {
		return "Neural network";
	}

	@Override
	public NeuralNetwork getModel() throws ModelManagerException {
		ensureNotNull(this.neuralNetwork);

		return this.neuralNetwork;
	}

	/**
	 * @throws ModelManagerException If the Model already exists
	 * 
	 * @see #getModel()
	 */
	public NeuralNetwork createModel(MiningFunctionType miningFunction,
			ActivationFunctionType activationFunction) throws ModelManagerException {
		ensureNull(this.neuralNetwork);

		this.neuralNetwork = new NeuralNetwork(new MiningSchema(), new NeuralInputs(),
				miningFunction, activationFunction);

		getModels().add(this.neuralNetwork);

		return this.neuralNetwork;
	}

	public List<NeuralInput> getNeuralInputs() throws ModelManagerException {
		NeuralNetwork neuralNetwork = getModel();

		return neuralNetwork.getNeuralInputs().getNeuralInputs();
	}

	public NeuralInput addNeuralInput(NormContinuous normContinuous) throws ModelManagerException {
		DerivedField derivedField = new DerivedField(OpType.CONTINUOUS, DataType.DOUBLE);
		derivedField.setExpression(normContinuous);

		NeuralInput neuralInput = new NeuralInput(derivedField, nextId());

		getNeuralInputs().add(neuralInput);

		return neuralInput;
	}

	public List<NeuralLayer> getNeuralLayers() throws ModelManagerException {
		NeuralNetwork neuralNetwork = getModel();

		return neuralNetwork.getNeuralLayers();
	}

	public NeuralLayer addNeuralLayer() throws ModelManagerException {
		NeuralLayer neuralLayer = new NeuralLayer();

		getNeuralLayers().add(neuralLayer);

		return neuralLayer;
	}

	public int getNeuronCount() throws ModelManagerException {
		int count = 0;

		count += (getNeuralInputs()).size();

		List<NeuralLayer> neuralLayers = getNeuralLayers();
		for (NeuralLayer neuralLayer : neuralLayers) {
			count += (neuralLayer.getNeurons()).size();
		}

		return count;
	}

	public Neuron addNeuron(NeuralLayer neuralLayer, Double bias) {
		Neuron neuron = new Neuron(nextId());
		neuron.setBias(bias);

		neuralLayer.getNeurons().add(neuron);

		return neuron;
	}

	public static void addConnection(NeuralInput from, Neuron to, double weight) {
		Connection connection = new Connection(from.getId(), weight);

		(to.getConnections()).add(connection);
	}

	public static void addConnection(Neuron from, Neuron to, double weight) {
		Connection connection = new Connection(from.getId(), weight);

		(to.getConnections()).add(connection);
	}

	public List<NeuralOutput> getOrCreateNeuralOutputs() throws ModelManagerException {
		NeuralNetwork neuralNetwork = getModel();

		NeuralOutputs neuralOutputs = neuralNetwork.getNeuralOutputs();
		if (neuralOutputs == null) {
			neuralOutputs = new NeuralOutputs();

			neuralNetwork.setNeuralOutputs(neuralOutputs);
		}

		return neuralOutputs.getNeuralOutputs();
	}

	public NeuralOutput addNeuralOutput(Neuron neuron, NormContinuous normCountinuous)
			throws ModelManagerException {
		DerivedField derivedField = new DerivedField(OpType.CONTINUOUS, DataType.DOUBLE);
		derivedField.setExpression(normCountinuous);

		NeuralOutput output = new NeuralOutput(derivedField, neuron.getId());

		getOrCreateNeuralOutputs().add(output);

		return output;
	}

	private String nextId() {
		return String.valueOf(this.neuronCount++);
	}
}
