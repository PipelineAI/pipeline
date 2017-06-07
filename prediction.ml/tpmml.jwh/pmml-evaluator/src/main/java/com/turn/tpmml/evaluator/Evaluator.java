/*
 * Copyright (c) 2012 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.FieldName;
import com.turn.tpmml.manager.Consumer;
import com.turn.tpmml.manager.IPMMLResult;
import com.turn.tpmml.manager.ModelManagerException;

import java.util.Map;

/**
 * <p>
 * Performs the evaluation of a {@link Model} in "interpreted mode".
 * </p>
 * 
 * Obtaining {@link Evaluator} instance:
 * 
 * <pre>
 * PMML pmml = ...;
 * PMMLManager pmmlManager = new PMMLManager(pmml);
 * Evaluator evaluator =
 *  (Evaluator)pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance());
 * </pre>
 * 
 * Preparing {@link Evaluator#getActiveFields() active fields}:
 * 
 * <pre>
 * Map&lt;FieldName, Object&gt; parameters = new LinkedHashMap&lt;FieldName, Object&gt;();
 * List&lt;FieldName&gt; activeFields = evaluator.getActiveFields();
 * for(FieldName activeField : activeFields){
 *   parameters.put(activeField, evaluator.prepare(activeField, ...));
 * }
 * </pre>
 * 
 * Performing the {@link Evaluator#evaluate(Map) evaluation}:
 * 
 * <pre>
 * Map&lt;FieldName, ?&gt; result = evaluator.evaluate(parameters);
 * </pre>
 * 
 * Retrieving the value of the {@link Evaluator#getTarget() predicted field} and
 * {@link Evaluator#getOutputFields() output fields}:
 * 
 * <pre>
 * FieldName targetField = evaluator.getTarget();
 * Object targetValue = result.get(targetField);
 * 
 * List&lt;FieldName&gt; outputFields = evaluator.getOutputFields();
 * for (FieldName outputField : outputFields) {
 * 	Object outputValue = result.get(outputField);
 * }
 * </pre>
 * 
 * Decoding {@link Computable complex value} to simple value:
 * 
 * <pre>
 * Object value = ...;
 * if(value instanceof Computable){
 *   Computable&lt;?&gt; computable = (Computable&lt;?&gt;)value;
 * 
 *   value = computable.getResult();
 * }
 * </pre>
 * 
 * @see EvaluatorUtil
 */
public interface Evaluator extends Consumer {

	/**
	 * Convenience method for retrieving the predicted field.
	 * 
	 * @return The predicted field
	 * 
	 * @throws ModelManagerException If the number of predicted fields is not exactly one
	 * 
	 * @see Consumer#getPredictedFields()
	 */
	FieldName getTarget() throws ModelManagerException;

	/**
	 * Prepares the input value for a field.
	 * 
	 * First, the value is converted from the user-supplied representation to internal
	 * representation. Later on, the value is subjected to missing value treatment, invalid value
	 * treatment and outlier treatment.
	 * 
	 * @param name The name of the field
	 * @param string The input value in user-supplied representation. Use <code>null</code> to
	 *            represent missing input value.
	 * 
	 * @throws EvaluationException If the input value preparation fails
	 * 
	 * @see #getDataField(FieldName)
	 * @see #getMiningField(FieldName)
	 */
	Object prepare(FieldName name, Object value) throws EvaluationException;

	/**
	 * @param parameters Map of {@link #getActiveFields() active field} values.
	 * 
	 * @return Map of {@link #getPredictedFields() predicted field} and {@link #getOutputFields()
	 *         output field} values. Simple values are represented using the Java equivalents of
	 *         PMML data types (eg. String, Integer, Float, Double etc.). Complex values are
	 *         represented as instances of {@link Computable} that return simple values.
	 * 
	 * @throws EvaluationException If the evaluation fails
	 * 
	 * @see Computable
	 */
	IPMMLResult evaluate(Map<FieldName, ?> parameters) throws EvaluationException;
}
