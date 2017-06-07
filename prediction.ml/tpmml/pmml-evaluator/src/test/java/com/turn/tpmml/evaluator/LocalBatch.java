/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

import java.io.InputStream;

public abstract class LocalBatch implements Batch {

	private String name = null;

	private String dataset = null;

	public LocalBatch(String name, String dataset) {
		setName(name);
		setDataset(dataset);
	}

	public abstract InputStream open(String path);

	public InputStream getModel() {
		return open("/pmml/" + (getName() + getDataset()) + ".pmml");
	}

	public InputStream getInput() {
		return open("/csv/" + getDataset() + ".csv");
	}

	public InputStream getOutput() {
		return open("/csv/" + (getName() + getDataset()) + ".csv");
	}

	public String getName() {
		return this.name;
	}

	private void setName(String name) {
		this.name = name;
	}

	public String getDataset() {
		return this.dataset;
	}

	private void setDataset(String dataset) {
		this.dataset = dataset;
	}
}
