/*
 * Copyright (c) 2009 University of Tartu
 */
package com.turn.tpmml;

import javax.xml.bind.annotation.*;

@SuppressWarnings("restriction")
@XmlTransient
abstract
public class Model extends PMMLObject {

	private static final long serialVersionUID = 1L;

	abstract
	public String getModelName();

	abstract
	public void setModelName(String modelName);

	abstract
	public MiningFunctionType getFunctionName();

	abstract
	public void setFunctionName(MiningFunctionType functionName);

	abstract
	public MiningSchema getMiningSchema();

	abstract
	public void setMiningSchema(MiningSchema miningSchema);

	abstract
	public LocalTransformations getLocalTransformations();

	abstract
	public void setLocalTransformations(LocalTransformations localTransformations);

	abstract
	public Output getOutput();

	abstract
	public void setOutput(Output output);

	abstract
	public boolean isScorable();

	abstract
	public void setScorable(Boolean scorable);

	abstract
	public ModelStats getModelStats();

	abstract
	public void setModelStats(ModelStats modelStats);
}
