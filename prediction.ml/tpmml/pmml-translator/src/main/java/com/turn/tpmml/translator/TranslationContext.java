package com.turn.tpmml.translator;

import com.turn.tpmml.DataField;
import com.turn.tpmml.DataType;
import com.turn.tpmml.FieldName;
import com.turn.tpmml.OpType;
import com.turn.tpmml.PMML;
import com.turn.tpmml.manager.ModelManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Translation context
 *
 * This class keeps state of translation. Depending on host environment clients
 * will need to extend and customize methods of this class.
 *
 * There are several distinct functional areas:
 *   1) Code indentation
 *   2) Variables binding. By default we assume host environment has exactly the
 *   same variable names declared as in the model.
 *   3) Constant mapping. No mapping by default, but it is possible that human-readable
 *   values used in PMML doc will need translation into system-specific numeric constants.
 *   4) Declaration of new constants required for efficient code execution
 *   5) Tracking imports
 *
 * @author asvirsky
 *
 */
public class TranslationContext {
	protected String indentationString;
	// protected String currentOutputVariable;

	protected List<String> constantDeclarations;
	protected Set<String> requiredImports;

	protected String createdVariables;

	protected int localVariablesIndex;
	protected CodeFormatter formatter;

	protected String prefix = "__";



	public TranslationContext() {
		indentationString = "\t\t";
		createdVariables = new String();
		constantDeclarations = new ArrayList<String>();
		requiredImports = new TreeSet<String>();
		localVariablesIndex = 0;
		formatter = new StandardCodeFormatter();
	}

	public void incIndentation() {
		indentationString += "\t";
	}

	public void decIndentation() {
		if (indentationString.length() > 0) {
			indentationString = indentationString.substring(1);
		}
	}

	public String getIndentation() {
		return indentationString;
	}

	public CodeFormatter getFormatter() {
		return formatter;
	}

	public Boolean isLocalVariable(String var) {
		return var.startsWith(prefix);
	}

	/**
	 * Output variable is identified by model mining schema - variable with usageType='predicted'
	 * 
	 * Here you can override default output variable, or apply formatting/transformation
	 * 
	 * @param variable name
	 * @return formatted variable name
	 */
	public String formatOutputVariable(String variable) {
		return variable;
	}

	public void assignOutputVariable(StringBuilder code, String value, TranslationContext context,
			DataField outputVariable) throws TranslationException {
		code.append(context.getIndentation())
				.append(context.formatOutputVariable(outputVariable.getName().getValue()))
				.append(" = ").append(formatConstant(null, outputVariable, value)).append(";\n");
	}

	/**
	 * Format variable name
	 * 
	 * Default is to to output name as is, assuming for each active PMML field there will be
	 * corresponding Java variable declared. Override formatExternalVariable to match your
	 * environment variables declaration/extraction.
	 * 
	 * @param modelManager
	 * @param variableName
	 * @return
	 * @throws TranslationException If the variable is a function or a pmml variable, and there is
	 *             no variable keeper.
	 */
	public String formatVariableName(ModelManager<?> modelManager, FieldName variableName)
			throws TranslationException {
		return variableName.getValue();
	}

	/**
	 * This function format the variable that are aimed to interact with the rest of the world.
	 * Override it for your specific needs.
	 * 
	 * @param modelManager
	 * @param variableName
	 * @return
	 */
	protected String formatExternalVariable(ModelManager<?> modelManager, FieldName variableName) {
		return variableName.getValue();
	}

	/**
	 * Format constant
	 * 
	 * Override this method if you want to implement some sort of dictionary mapping. For example,
	 * convert constant 'SAFARI' for variable BROWSER into host-specific numeric constant.
	 * 
	 * @param modelManager
	 * @param fieldName
	 * @param constant
	 * @return
	 * @throws TranslationException
	 */
	public String formatConstant(ModelManager<?> modelManager, DataField formatForType,
			String constant) throws TranslationException {
		if (constant == null) {
			return null;
		}

		if (formatForType != null && formatForType.getDataType() == DataType.STRING) {
			return "\"" + constant + "\"";
		}
		return constant;
	}

	public String getMissingValue(OpType variableType) {
		switch (variableType) {
		case CATEGORICAL:
			return "null";
		case CONTINUOUS:
			return "-1";
		default:
			throw new UnsupportedOperationException("Unknown variable type: " + variableType);
		}
	}

	public String getExceptionName() {
		return "Exception";
	}

	public List<String> getConstantDeclarations() {
		return constantDeclarations;
	}

	public Set<String> getRequiredImports() {
		return requiredImports;
	}

	/**
	 * Add dependency that needs to be imported
	 * 
	 * Depending on the module different imports might be required. Translation code will call this
	 * method to register import. Duplicates are ignored. Full list can later on be requested by
	 * getRequiredImports method and inserted into code template.
	 * 
	 * @param requiredImport e.g. "java.util.ArrayList"
	 */
	public void addRequiredImport(String requiredImport) {
		requiredImports.add("import " + requiredImport + ";");
	}

	/**
	 * Add new constant. Context will keep track of all declared variables. Full list of declared
	 * can be accessed by calling getConstantDeclarations and inserted into code template.
	 * 
	 * @param type - variable type. Can be anything really
	 * @param prefix - variable prefix. Method will append number to ensure name is unique
	 * @param value - value of the variable
	 * @return generated variable name
	 */
	public String addNewConstant(String type, String prefix, String value) {
		String varName = prefix + constantDeclarations.size();
		constantDeclarations.add(type + " " + varName + " = " + value + ";");
		return varName;
	}

	/**
	 * Generate new local variable name
	 * 
	 * This is useful if code generation involves creation of local variables.
	 * 
	 * @param prefix - variable prefix. E.g. if prefix="var", and system already requested 3
	 *            variables, then next variable will be "var4"
	 * @return
	 */
	public String generateLocalVariableName(String prefix) {
		return this.prefix + prefix + localVariablesIndex++;
	}

	/**
	 * Return name of the variable that will store model result explanation. That mostly makes sense
	 * for the models that return distinct number of result values. Specifically, TreeModel which
	 * can have nodeId associated with each node. Can also be applied to RuleSet and ScoreCard (for
	 * storing reason code).
	 * 
	 * Default is null, which means do nothing. Override if your host system has dedicated variable
	 * for storing result value explanation/tracking.
	 * 
	 * @return variable name, or null if result tracking is not supported.
	 */
	public String getModelResultTrackingVariable() {
		return null;
	}

	/**
	 * Return value that represents "null" in hosting environment.
	 * 
	 * @param variableType categorical or continuous type
	 * 
	 * @return
	 */
	public String getNullValueForVariable(OpType variableType) {
		switch (variableType) {
		case CATEGORICAL:
			return "null";
		case CONTINUOUS:
			return "-1";
		default:
			throw new UnsupportedOperationException("Unknown variable type: " + variableType);
		}
	}

	/**
	 * This function is used to generate some code that will be put in the variable
	 * "hookBeforeTranslation".
	 * 
	 * @param pmml The pmml.
	 * @param context The translation context.
	 * @param manager The manager of the model.
	 * @return The code that will contain the variable "hookBeforeTranslation".
	 * @throws TranslationException
	 */
	public Object beforeTranslation(PMML pmml, ModelManager<?> manager)
							throws TranslationException {
		return "";
	}

	/**
	 * This function is used to generate some code that will be put in the variable
	 * "hookAfterTranslation".
	 * 
	 * @param pmml The pmml.
	 * @param context The translation context.
	 * @param manager The manager of the model.
	 * @return The code that will contain the variable "hookBeforeTranslation".
	 * @throws TranslationException
	 */
	public Object afterTranslation(PMML pmml, ModelManager<?> manager) throws TranslationException {
		return "";
	}

	public Object declareExtraVariables(PMML pmml, ModelManager<?> manager)
			throws TranslationException {
		return createdVariables;
	}

	public void setCreatedVariables(String createdVariables) {
		this.createdVariables = createdVariables;
	}

}
