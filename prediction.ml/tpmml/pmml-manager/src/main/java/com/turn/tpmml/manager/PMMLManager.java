/*
 * Copyright (c) 2009 University of Tartu
 */
package com.turn.tpmml.manager;

import com.turn.tpmml.DataDictionary;
import com.turn.tpmml.DataField;
import com.turn.tpmml.DataType;
import com.turn.tpmml.DerivedField;
import com.turn.tpmml.FieldName;
import com.turn.tpmml.HasName;
import com.turn.tpmml.Header;
import com.turn.tpmml.Model;
import com.turn.tpmml.OpType;
import com.turn.tpmml.PMML;
import com.turn.tpmml.PMMLObject;
import com.turn.tpmml.TransformationDictionary;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * PMMLManager is the mother class of the project. It allows to work with the PMML at the lowest
 * level.
 * 
 * Naming conventions for getter methods:
 * <ul>
 * <li><code>getXXX()</code> - Required schema elements. For example {@link #getDataDictionary()}
 * <li><code>getOrCreateXXX()</code> - Optional schema elements. When <code>null</code> then a new
 * element instance is created. For example {@link #getOrCreateTransformationDictionary()}
 * </ul>
 */
public class PMMLManager implements Serializable {

	private static final long serialVersionUID = 1L;

	private PMML pmml = null;

	private TransformationDictionary transformationDictionary = null;

	/**
	 * Create a manager for an empty PMML that belongs to the version 4.1.
	 */
	public PMMLManager() {
		this(new PMML(new Header(), new DataDictionary(), "4.1"));
	}

	/**
	 * Create a manager that works on the given PMML.
	 * 
	 * @param pmml The PMML to manage.
	 */
	public PMMLManager(PMML pmml) {
		setPmml(pmml);
	}

	/**
	 * Return the DataField corresponding to the name given in argument.
	 * 
	 * @param name The name to look for.
	 * @return The DataField or null if it is not found.
	 */
	public DataField getDataField(FieldName name) {
		List<DataField> dataFields = getDataDictionary().getDataFields();

		return find(dataFields, name);
	}

	/**
	 * Add a new Field to the model.
	 * 
	 * @param name Name of the Field.
	 * @param displayName The display name of the field.
	 * @param opType The optype of the field.
	 * @param dataType Its type.
	 * @return The dataField created.
	 */
	public DataField addDataField(FieldName name, String displayName, OpType opType,
			DataType dataType) {
		DataField dataField = new DataField(name, opType, dataType);
		dataField.setDisplayName(displayName);

		List<DataField> dataFields = getDataDictionary().getDataFields();
		dataFields.add(dataField);

		return dataField;
	}

	/**
	 * Find the value in the transformation dictionary.
	 * 
	 * @param name The name to look for.
	 * @return the DerivedField or null if not found.
	 * @throws ModelManagerException
	 */
	// FIXME: Check that this function handles the transformation as it looks like.
	public DerivedField resolve(FieldName name) throws ModelManagerException {
		TransformationDictionary transformationDictionary = getOrCreateTransformationDictionary();

		List<DerivedField> derivedFields = transformationDictionary.getDerivedFields();

		return find(derivedFields, name);
	}

	/**
	 * Return the PMML managed.
	 * 
	 * @return The PMML.
	 */
	public PMML getPmml() {
		return this.pmml;
	}

	private void setPmml(PMML pmml) {
		this.pmml = pmml;
	}

	public Header getHeader() {
		return getPmml().getHeader();
	}

	public DataDictionary getDataDictionary() {
		return getPmml().getDataDictionary();
	}

	/**
	 * Return the transformation dictionary. If none exists, an empty one is created.
	 */
	public TransformationDictionary getOrCreateTransformationDictionary() {

		if (this.transformationDictionary == null) {
			PMML pmml = getPmml();

			TransformationDictionary transformationDictionary = pmml.getTransformationDictionary();
			if (transformationDictionary == null) {
				transformationDictionary = new TransformationDictionary();

				pmml.setTransformationDictionary(transformationDictionary);
			}

			this.transformationDictionary = transformationDictionary;
		}

		return this.transformationDictionary;
	}

	/**
	 * Get the output field of the model.
	 * 
	 * @param model
	 * @return
	 * @throws Exception
	 */
	public static DataField getOutputField(ModelManager<?> model) throws ModelManagerException {
		String outputVariableName = null;
		List<FieldName> predictedFields = model.getPredictedFields();

		// Get the predicted field. If there is none, it is an error.
		if (predictedFields != null && predictedFields.size() > 0) {
			outputVariableName = predictedFields.get(0).getValue();
		}

		if (outputVariableName == null) {
			throw new ModelManagerException("Predicted variable is not defined");
		}

		DataField outputField = model.getDataField(new FieldName(outputVariableName));
		if (outputField == null || outputField.getDataType() == null) {
			throw new ModelManagerException("Predicted variable [" + outputVariableName +
					"] does not have type defined");
		}

		return outputField;
	}

	/**
	 * Get all the models.
	 * 
	 * @return the list of the models in the PMML.
	 */
	public List<Model> getModels() {
		return getPmml().getContent();
	}

	/**
	 * @param modelName The name of the Model to be selected. If <code>null</code>, the first model
	 *            is selected.
	 * 
	 * @see Model#getModelName()
	 */
	public Model getModel(String modelName) {
		List<Model> models = getModels();

		if (modelName != null) {
			for (Model model : models) {
				if (modelName.equals(model.getModelName())) {
					return model;
				}
			}

			return null;
		}

		if (models.size() > 0) {
			return models.get(0);
		}

		return null;
	}

	/**
	 * Return a manager for the model type 'modelName'.
	 * 
	 * @param modelName The name to look for.
	 * @return A valid manager for the model type.
	 * @throws ModelManagerException
	 */
	public ModelManager<? extends Model> getModelManager(String modelName)
			throws ModelManagerException {
		return getModelManager(modelName, ModelManagerFactory.getInstance());
	}

	/**
	 * Return a manager for the model type 'modelName' with the factory 'modelManagerFactory'.
	 * 
	 * @param modelName The name to look for.
	 * @param modelManagerFactory The factory that gives the manager.
	 * @return
	 * @throws ModelManagerException
	 */
	public ModelManager<? extends Model> getModelManager(String modelName,
			ModelManagerFactory modelManagerFactory) throws ModelManagerException {
		Model model = getModel(modelName);

		return modelManagerFactory.getModelManager(getPmml(), model);
	}

	/**
	 * Return the first element of the list 'objects' that has a type of value 'type'.
	 * 
	 * @param objects The list of objects.
	 * @param type The type to look for.
	 */
	@SuppressWarnings({ "unchecked" })
	public static <E extends PMMLObject> E find(Collection<? extends PMMLObject> objects,
			Class<? extends E> type) {

		for (PMMLObject object : objects) {
			if (object.getClass().equals(type)) {
				return (E) object;
			}
		}

		return null;
	}

	/**
	 * Return the first element of the list 'objects' that has a name equal to 'name'.
	 * 
	 * @param objects The list to find an object from.
	 * @param name The name to look for.
	 */
	public static <E extends PMMLObject & HasName> E find(Collection<E> objects, FieldName name) {

		for (E object : objects) {

			if ((object.getName()).equals(name)) {
				return object;
			}
		}

		return null;
	}
}
