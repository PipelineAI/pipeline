/*
 * Copyright (c) 2009 University of Tartu
 */
package com.turn.tpmml.manager;

import com.turn.tpmml.DataType;
import com.turn.tpmml.DerivedField;
import com.turn.tpmml.FieldName;
import com.turn.tpmml.FieldUsageType;
import com.turn.tpmml.LocalTransformations;
import com.turn.tpmml.MiningField;
import com.turn.tpmml.MiningSchema;
import com.turn.tpmml.Model;
import com.turn.tpmml.OpType;
import com.turn.tpmml.Output;
import com.turn.tpmml.OutputField;
import com.turn.tpmml.PMML;

import java.util.ArrayList;
import java.util.List;

public abstract class ModelManager<M extends Model> extends PMMLManager implements Consumer {

	private static final long serialVersionUID = 1L;

	private LocalTransformations localTransformations = null;

	private Output output = null;

	public ModelManager() {
	}

	public ModelManager(PMML pmml) {
		super(pmml);
	}

	/**
	 * @throws ModelManagerException If the Model does not exist
	 */
	public abstract M getModel() throws ModelManagerException;

	/**
	 * Convenience method for adding a field declaration to {@link DataDictionary} and
	 * {@link MiningSchema}.
	 * @throws ModelManagerException 
	 * 
	 * @see #addDataField(FieldName, String, OpType, DataType)
	 * @see #addMiningField(FieldName, FieldUsageType)
	 */
	public void addField(FieldName name, String displayName, OpType opType, DataType dataType,
			FieldUsageType fieldUsageType) throws ModelManagerException {
		addDataField(name, displayName, opType, dataType);
		addMiningField(name, fieldUsageType);
	}

	public List<FieldName> getActiveFields() throws ModelManagerException {
		return getMiningFields(FieldUsageType.ACTIVE);
	}

	public FieldName getTarget() throws ModelManagerException {
		List<FieldName> fields = getPredictedFields();

		if (fields.size() != 1) {
			throw new ModelManagerException("Wrong number of fields, expected 1, got " +
					fields.size());
		}

		return fields.get(0);
	}

	public List<FieldName> getPredictedFields() throws ModelManagerException {
		List<FieldName> fieldList = getMiningFields(FieldUsageType.TARGET);
		if (fieldList.size() == 0)
			fieldList = getMiningFields(FieldUsageType.PREDICTED);
		return fieldList;
	}

	public List<FieldName> getMiningFields(FieldUsageType fieldUsageType)
			throws ModelManagerException {
		List<FieldName> result = new ArrayList<FieldName>();

		List<MiningField> miningFields = getMiningSchema().getMiningFields();
		for (MiningField miningField : miningFields) {

			if ((miningField.getUsageType()).equals(fieldUsageType)) {
				result.add(miningField.getName());
			}
		}

		return result;
	}

	public MiningField getMiningField(FieldName name) throws ModelManagerException {
		List<MiningField> miningFields = getMiningSchema().getMiningFields();

		return find(miningFields, name);
	}

	public MiningField addMiningField(FieldName name, FieldUsageType usageType)
			throws ModelManagerException {
		MiningField miningField = new MiningField(name);
		miningField.setUsageType(usageType);

		List<MiningField> miningFields = getMiningSchema().getMiningFields();
		miningFields.add(miningField);

		return miningField;
	}

	public List<FieldName> getOutputFields() throws ModelManagerException {
		List<FieldName> result = new ArrayList<FieldName>();

		Output output = getOrCreateOutput();

		List<OutputField> outputFields = output.getOutputFields();
		for (OutputField outputField : outputFields) {
			result.add(outputField.getName());
		}

		return result;
	}

	public OutputField getOutputField(FieldName name) throws ModelManagerException {
		Output output = getOrCreateOutput();

		List<OutputField> outputFields = output.getOutputFields();

		return find(outputFields, name);
	}

	@Override
	public DerivedField resolve(FieldName name) throws ModelManagerException {
		LocalTransformations localTransformations = getOrCreateLocalTransformations();

		List<DerivedField> derivedFields = localTransformations.getDerivedFields();

		DerivedField derivedField = find(derivedFields, name);
		if (derivedField == null) {
			derivedField = super.resolve(name);
		}

		return derivedField;
	}

	public MiningSchema getMiningSchema() throws ModelManagerException {
		return getModel().getMiningSchema();
	}

	public LocalTransformations getOrCreateLocalTransformations() throws ModelManagerException {

		if (this.localTransformations == null) {
			M model = getModel();

			LocalTransformations localTransformations = model.getLocalTransformations();
			if (localTransformations == null) {
				localTransformations = new LocalTransformations();

				model.setLocalTransformations(localTransformations);
			}

			this.localTransformations = localTransformations;
		}

		return this.localTransformations;
	}

	public Output getOrCreateOutput() throws ModelManagerException {

		if (this.output == null) {
			M model = getModel();

			Output output = model.getOutput();
			if (output == null) {
				output = new Output();

				model.setOutput(output);
			}

			this.output = output;
		}

		return this.output;
	}

	protected static void ensureNull(Object object) throws ModelManagerException {

		if (object != null) {
			throw new ModelManagerException("Object is not null as it should be");
		}
	}

	protected static void ensureNotNull(Object object) throws ModelManagerException {

		if (object == null) {
			throw new ModelManagerException("Object is null and it shouldn't be");
		}
	}
}
