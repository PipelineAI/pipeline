/*
 * Copyright (c) 2009 University of Tartu
 */
package com.turn.tpmml.manager;

import com.turn.tpmml.CategoricalPredictor;
import com.turn.tpmml.FieldName;
import com.turn.tpmml.MiningFunctionType;
import com.turn.tpmml.MiningSchema;
import com.turn.tpmml.NumericPredictor;
import com.turn.tpmml.PMML;
import com.turn.tpmml.RegressionModel;
import com.turn.tpmml.RegressionNormalizationMethodType;
import com.turn.tpmml.RegressionTable;

import java.util.List;

/**
 * Provide an interface to the regressionModel class.
 * 
 * The regression functions are used to determine the relationship between the dependent variable
 * (target field) and one or more independent variables. The dependent variable is the one whose
 * values you want to predict, whereas the independent variables are the variables that you base
 * your prediction on. While the term regression usually refers to the prediction of numeric values,
 * the PMML element RegressionModel can also be used for classification. This is due to the fact
 * that multiple regression equations can be combined in order to predict categorical values.
 * 
 * 
 * @author tbadie
 * 
 */
public class RegressionModelManager extends ModelManager<RegressionModel> {

	private static final long serialVersionUID = 1L;

	private RegressionModel regressionModel = null;

	public RegressionModelManager() {
	}

	public RegressionModelManager(PMML pmml) {
		this(pmml, find(pmml.getContent(), RegressionModel.class));
	}

	public RegressionModelManager(PMML pmml, RegressionModel regressionModel) {
		super(pmml);

		this.regressionModel = regressionModel;
	}

	public String getSummary() {
		return "Regression";
	}

	@Override
	public RegressionModel getModel() throws ModelManagerException {
		ensureNotNull(this.regressionModel);

		return this.regressionModel;
	}

	public RegressionModel createRegressionModel() throws ModelManagerException {
		return createModel(MiningFunctionType.REGRESSION);
	}

	/**
	 * @throws ModelManagerException If the Model already exists
	 * 
	 * @see #getModel()
	 */
	public RegressionModel createModel(MiningFunctionType miningFunction)
			throws ModelManagerException {
		ensureNull(this.regressionModel);

		this.regressionModel = new RegressionModel(new MiningSchema(), miningFunction);

		getModels().add(this.regressionModel);

		return this.regressionModel;
	}

	@Override
	public FieldName getTarget() throws ModelManagerException {
		RegressionModel regressionModel = getModel();

		FieldName name = regressionModel.getTargetFieldName();
		if (name != null) {
			return name;
		}

		return super.getTarget();
	}

	public RegressionModel setTarget(FieldName name) throws ModelManagerException {
		RegressionModel regressionModel = getModel();
		regressionModel.setTargetFieldName(name);

		return regressionModel;
	}

	public List<RegressionTable> getRegressionTables() throws ModelManagerException {
		RegressionModel model = getModel();
		return model.getRegressionTables();
	}

	public static NumericPredictor getNumericPredictor(RegressionTable regressionTable,
			FieldName name) {
		return find(regressionTable.getNumericPredictors(), name);
	}

	public static NumericPredictor addNumericPredictor(RegressionTable regressionTable,
			FieldName name, Double coefficient) {
		NumericPredictor numericPredictor = new NumericPredictor(name, coefficient.doubleValue());
		(regressionTable.getNumericPredictors()).add(numericPredictor);

		return numericPredictor;
	}

	/**
	 * Get a particular categoricalPredictor.
	 * 
	 * @param rt The regressionTable used.
	 * @param name The name of the categoricalPredictor wanted.
	 * @return The categorical predictor wanted if found, null otherwise.
	 */
	public static CategoricalPredictor getCategoricalPredictor(RegressionTable regressionTable,
			FieldName name) {
		return find(regressionTable.getCategoricalPredictors(), name);
	}

	/**
	 * Add a new categorical predictor to the first regressionTable.
	 * 
	 * @param name The name of the variable.
	 * @param coefficient The corresponding coefficient.
	 * @return The categorical predictor.
	 */
	public static CategoricalPredictor addCategoricalPredictor(RegressionTable regressionTable,
			FieldName name, String value, Double coefficient) {
		CategoricalPredictor categoricalPredictor = new CategoricalPredictor(name, value,
				coefficient.doubleValue());
		(regressionTable.getCategoricalPredictors()).add(categoricalPredictor);

		return categoricalPredictor;
	}

	/**
	 * Get the type of normalization of the model.
	 * 
	 * @return The type of normalization.
	 */
	public RegressionNormalizationMethodType getNormalizationMethodType() {

		return regressionModel.getNormalizationMethod();
	}

	public MiningFunctionType getFunctionName() {
		return regressionModel.getFunctionName();
	}
}
