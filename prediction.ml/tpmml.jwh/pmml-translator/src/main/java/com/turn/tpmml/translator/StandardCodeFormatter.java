package com.turn.tpmml.translator;

import com.turn.tpmml.manager.TPMMLException.TPMMLCause;

public class StandardCodeFormatter implements CodeFormatter {

	public void declareVariable(StringBuilder code,
			TranslationContext context, Variable variable,
			String initializer) {
		code.append(context.getIndentation())
			.append(variable.getTypeName()).append(" ")
			.append(variable.getName()).append(" = ")
			.append(initializer).append(";\n");
	}

	public void declareVariable(StringBuilder code,
			TranslationContext context, Variable variable) throws TranslationException {
		String initializer = null;

		switch (variable.getType()) {
		case INTEGER:
			initializer = "0";
			break;
		case DOUBLE:
		case FLOAT:
			initializer = "0.0";
			break;
		case BOOLEAN:
			initializer = "false";
			break;
		case STRING:
		case OBJECT:
			initializer = "new " + variable.getTypeName() + "()";
			break;
		default:
			throw new TranslationException(TPMMLCause.UNSUPPORTED_OPERATION,
					variable.getType().name());
		}

		declareVariable(code, context, variable, initializer);
	}

	public void addLine(StringBuilder code, TranslationContext context,
			String line) {
		code.append(context.getIndentation()).append(line).append("\n");
	}

	public void assignVariableToNullValue(StringBuilder code,
			TranslationContext context, Variable variable) throws TranslationException {
		String initializer = null;

		switch (variable.getType()) {
		case INTEGER:
			initializer = "0";
			break;
		case DOUBLE:
		case FLOAT:
			initializer = "0.0";
			break;
		case OBJECT:
		case STRING:
			initializer = "null";
			break;
		default:
			throw new TranslationException(TPMMLCause.UNSUPPORTED_OPERATION,
					variable.getType().name());
		}

		assignVariable(code, context, Operator.EQUAL, variable, initializer);
	}

	public void assignVariable(StringBuilder code, TranslationContext context,
			Operator op, Variable variable, String expression) {
		assignVariable(code, context, op, variable.getName(), expression);
	}

	public void beginControlFlowStructure(StringBuilder code,
			TranslationContext context, String keyword,
			String conditionnalExpression) {
		code.append(context.getIndentation()).append(keyword);
		if (!(conditionnalExpression == null && keyword.equals("else"))) {
			code.append(" (").append(conditionnalExpression).append(") {\n");
		} else {
			code.append(" {\n");
		}

		context.incIndentation();

	}

	public void endControlFlowStructure(StringBuilder code,
			TranslationContext context) {
		context.decIndentation();
		code.append(context.getIndentation()).append("}\n");
	}

	public void assignVariable(StringBuilder code, TranslationContext context,
			Variable variable, String expression) {
		assignVariable(code, context, Operator.EQUAL, variable, expression);

	}

	public void assignVariable(StringBuilder code, TranslationContext context,
			String variableName, String expression) {
		code.append(context.getIndentation()).append(variableName)
		.append(" = ").append(expression)
		.append(";\n");
	}

	public void assignVariable(StringBuilder code, TranslationContext context,
			Operator op, String variableName, String expression) {
		code.append(context.getIndentation()).append(variableName)
		.append(" ").append(op.value()).append(" ").append(expression)
		.append(";\n");
	}

	public String stringify(String str) {
		return "\"" + str + "\"";
	}

	public void printStatement(StringBuilder code, TranslationContext context,
			String statement) {
		addLine(code, context, "System.out.println(\"" + statement + "\");");
	}

	public void printVariable(StringBuilder code, TranslationContext context,
			String variableName) {
		addLine(code, context, "System.out.println(\"" +
			variableName + ": \" + " + variableName + ");");
	}

}
