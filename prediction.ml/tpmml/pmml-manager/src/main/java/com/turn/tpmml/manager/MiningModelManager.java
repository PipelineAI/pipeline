/*
 * Copyright (c) 2012 University of Tartu
 */
package com.turn.tpmml.manager;

import com.turn.tpmml.MiningFunctionType;
import com.turn.tpmml.MiningModel;
import com.turn.tpmml.MiningSchema;
import com.turn.tpmml.Model;
import com.turn.tpmml.MultipleModelMethodType;
import com.turn.tpmml.PMML;
import com.turn.tpmml.Predicate;
import com.turn.tpmml.Segment;
import com.turn.tpmml.Segmentation;
import com.turn.tpmml.True;

import java.util.List;

public class MiningModelManager extends ModelManager<MiningModel> {

	private static final long serialVersionUID = 1L;
	private MiningModel miningModel = null;

	public MiningModelManager() {
	}

	public MiningModelManager(PMML pmml) {
		this(pmml, find(pmml.getContent(), MiningModel.class));
	}

	public MiningModelManager(PMML pmml, MiningModel miningModel) {
		super(pmml);

		this.miningModel = miningModel;
	}

	@Override
	public MiningModel getModel() throws ModelManagerException {
		ensureNotNull(this.miningModel);

		return this.miningModel;
	}

	public String getSummary() {
		return "MiningModel";
	}

	/**
	 * @throws ModelManagerException If the Model already exists
	 * 
	 * @see #getModel()
	 */
	public MiningModel createModel(MiningFunctionType miningFunction) throws ModelManagerException {
		ensureNull(this.miningModel);

		this.miningModel = new MiningModel(new MiningSchema(), miningFunction);

		getModels().add(this.miningModel);

		return this.miningModel;
	}

	/**
	 * @throws ModelManagerException If the Segmentation does not exist
	 */
	public Segmentation getSegmentation() throws ModelManagerException {
		MiningModel miningModel = getModel();

		Segmentation segmentation = miningModel.getSegmentation();
		ensureNotNull(segmentation);

		return segmentation;
	}

	/**
	 * @throws ModelManagerException If the Segmentation already exists
	 */
	public Segmentation createSegmentation(MultipleModelMethodType multipleModelMethod)
			throws ModelManagerException {
		MiningModel miningModel = getModel();

		Segmentation segmentation = miningModel.getSegmentation();
		ensureNull(segmentation);

		segmentation = new Segmentation(multipleModelMethod);
		miningModel.setSegmentation(segmentation);

		return segmentation;
	}

	public Segment addSegment(Model model) throws ModelManagerException {
		return addSegment(new True(), model);
	}

	public Segment addSegment(Predicate predicate, Model model) throws ModelManagerException {
		Segment segment = new Segment();
		segment.setPredicate(predicate);
		segment.setModel(model);

		getSegments().add(segment);

		return segment;
	}

	public List<Segment> getSegments() throws ModelManagerException {
		return getSegmentation().getSegments();
	}

	public MiningFunctionType getFunctionType() {
		return miningModel.getFunctionName();
	}

	public MultipleModelMethodType getMultipleMethodModel() {
		return miningModel.getSegmentation().getMultipleModelMethod();
	}
}
