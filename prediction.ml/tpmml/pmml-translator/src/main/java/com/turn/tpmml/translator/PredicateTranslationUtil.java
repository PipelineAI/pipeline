package com.turn.tpmml.translator;

import com.turn.tpmml.CompoundPredicate;
import com.turn.tpmml.DataField;
import com.turn.tpmml.False;
import com.turn.tpmml.OpType;
import com.turn.tpmml.Predicate;
import com.turn.tpmml.SimplePredicate;
import com.turn.tpmml.SimpleSetPredicate;
import com.turn.tpmml.True;

import com.turn.tpmml.manager.ModelManager;

public class PredicateTranslationUtil {
	public static final int TRUE = 1;
	public static final int FALSE = 2;
	public static final int UNKNOWN = 3;
	
	private PredicateTranslationUtil() {
		// Forbid the creation of an instance.
	}

	public static String generateCode(Predicate predicate, ModelManager<?> modelManager,
			TranslationContext context) throws TranslationException {
		if (predicate instanceof SimplePredicate) {
			return generateCodeForSimplePredicate((SimplePredicate) predicate, modelManager,
					context);
		} else if (predicate instanceof CompoundPredicate) {
			return generateCodeForCompoundPredicate((CompoundPredicate) predicate, modelManager,
					context);
		} else if (predicate instanceof SimpleSetPredicate) {
			return generateCodeForSimpleSetPredicate((SimpleSetPredicate) predicate, modelManager,
					context);
		} else if (predicate instanceof True) {
			return String.valueOf(PredicateTranslationUtil.TRUE);
		} else if (predicate instanceof False) {
			return String.valueOf(PredicateTranslationUtil.FALSE);
		}
		throw new TranslationException("Unknown predicate type: " + predicate.getClass().getName());
	}

	private static String operationWrapper(String operation, String variableString,
			String constant, TranslationContext context) {
		return String.format("%s == %s? %s : ((%s %s %s)? %s : %s)", variableString,
				context.getMissingValue(OpType.CONTINUOUS), PredicateTranslationUtil.UNKNOWN,
				variableString, operation, constant, PredicateTranslationUtil.TRUE,
				PredicateTranslationUtil.FALSE);
	}

	private static String generateCodeForSimplePredicate(SimplePredicate predicate,
			ModelManager<?> modelManager, TranslationContext context) throws TranslationException {

		String code = null;

		DataField dataField = modelManager.getDataField(predicate.getField());

		if (dataField == null) {
			throw new TranslationException(predicate.getField().getValue() + " is not defined.");
		}

		String variableString = context.formatVariableName(modelManager, predicate.getField());
		String constant = context.formatConstant(modelManager, dataField, predicate.getValue());

		if (dataField.getOptype() == OpType.CATEGORICAL) {
			switch (predicate.getOperator()) {
			case EQUAL:
				code = String.format("%s == %s? %s : (%s.equals(%s) ? %s : %s)", variableString,
						context.getMissingValue(OpType.CATEGORICAL),
						PredicateTranslationUtil.UNKNOWN, variableString, constant,
						PredicateTranslationUtil.TRUE, PredicateTranslationUtil.FALSE);
				break;
			case NOT_EQUAL:
				code = String.format("%s == %s? %s : (%s.equals(%s) ? %s : %s)", variableString,
						context.getMissingValue(OpType.CATEGORICAL),
						PredicateTranslationUtil.UNKNOWN, variableString, constant,
						PredicateTranslationUtil.FALSE, PredicateTranslationUtil.TRUE);
				break;
			case IS_MISSING:
				code = String.format("%s == %s? %s : %s", variableString,
						context.getMissingValue(OpType.CATEGORICAL), PredicateTranslationUtil.TRUE,
						PredicateTranslationUtil.FALSE);
				break;
			case IS_NOT_MISSING:
				code = String.format("%s!=%s? %s : %s", variableString,
						context.getMissingValue(OpType.CATEGORICAL), PredicateTranslationUtil.TRUE,
						PredicateTranslationUtil.FALSE);
				break;
			case LESS_THAN:
			case LESS_OR_EQUAL:
			case GREATER_THAN:
			case GREATER_OR_EQUAL:
				throw new TranslationException("Invalid operator for categorical variable: " +
						predicate.getField() + "; operator: " + predicate.getOperator());
			default:
				throw new TranslationException("Unknown operator: " + predicate.getOperator());

			}
		} else {
			switch (predicate.getOperator()) {
			case EQUAL:
				code = operationWrapper("==", variableString, constant, context);
				break;
			case NOT_EQUAL:
				code = operationWrapper("!=", variableString, constant, context);
				break;
			case LESS_THAN:
				code = operationWrapper("<", variableString, constant, context);
				break;
			case LESS_OR_EQUAL:
				code = operationWrapper("<=", variableString, constant, context);
				break;
			case GREATER_THAN:
				code = operationWrapper(">", variableString, constant, context);
				break;
			case GREATER_OR_EQUAL:
				code = operationWrapper(">=", variableString, constant, context);
				break;
			case IS_MISSING:
				code = String.format("%s == %s? %s : %s", variableString,
						context.getMissingValue(OpType.CONTINUOUS), PredicateTranslationUtil.TRUE,
						PredicateTranslationUtil.FALSE);
				break;
			case IS_NOT_MISSING:
				code = String.format("%s != %s? %s : %s", variableString,
						context.getMissingValue(OpType.CONTINUOUS), PredicateTranslationUtil.TRUE,
						PredicateTranslationUtil.FALSE);
				break;
			default:
				throw new TranslationException("Unknown operator: " + predicate.getOperator());
			}
		}

		return code;

	}

	private static String generateCodeForCompoundPredicate(CompoundPredicate predicate,
			ModelManager<?> modelManager, TranslationContext context) throws TranslationException {
		context.addRequiredImport("com.turn.tpmml.translator.PredicateTranslationUtil");
		context.addRequiredImport("com.turn.tpmml.CompoundPredicate.BooleanOperator");

		StringBuilder code = new StringBuilder();

		code.append("PredicateTranslationUtil.evaluateCompoundPredicate(");
		code.append("BooleanOperator.").append(predicate.getBooleanOperator().toString());

		for (Predicate innerPredicate : predicate.getContent()) {
			String predicateCode = PredicateTranslationUtil.generateCode(innerPredicate,
					modelManager, context);
			code.append(',');
			code.append(predicateCode);
		}
		code.append(')');

		return code.toString();

	}

	private static String generateCodeForSimpleSetPredicate(SimpleSetPredicate predicate,
			ModelManager<?> modelManager, TranslationContext context) {

		return null;
	}

	public static int evaluateCompoundPredicate(CompoundPredicate.BooleanOperator operator,
			int... predicateResults) {

		int result = PredicateTranslationUtil.UNKNOWN;

		switch (operator) {
		case SURROGATE:
			for (int i = 0; i < predicateResults.length; i++) {
				if (predicateResults[i] != PredicateTranslationUtil.UNKNOWN) {
					result = predicateResults[i];
					break;
				}
			}
			break;
		case OR:

			for (int i = 0; i < predicateResults.length; i++) {
				if (i == 0) {
					result = predicateResults[i];
				} else {
					// regular OR; if at least one is not missing and TRUE - return TRUE
					if (result != PredicateTranslationUtil.UNKNOWN &&
							predicateResults[i] != PredicateTranslationUtil.UNKNOWN) {
						result = ((result == PredicateTranslationUtil.TRUE) ||
								(predicateResults[i] == PredicateTranslationUtil.TRUE)) ?
										PredicateTranslationUtil.TRUE :
										PredicateTranslationUtil.FALSE;
					} else if ((result == PredicateTranslationUtil.TRUE) ||
							(predicateResults[i] == PredicateTranslationUtil.TRUE)) {
						result = PredicateTranslationUtil.TRUE;
					} else {
						result = PredicateTranslationUtil.UNKNOWN;
					}
				}
			}
			break;
		case AND:
			for (int i = 0; i < predicateResults.length; i++) {
				if (i == 0) {
					result = predicateResults[i];
				} else {
					// regular AND; if at least one is not missing and FALSE - return FALSE
					if (result != PredicateTranslationUtil.UNKNOWN &&
							predicateResults[i] != PredicateTranslationUtil.UNKNOWN) {
						result = ((result == PredicateTranslationUtil.TRUE) &&
								(predicateResults[i] == PredicateTranslationUtil.TRUE)) ?
										PredicateTranslationUtil.TRUE :
										PredicateTranslationUtil.FALSE;
					} else if ((result == PredicateTranslationUtil.FALSE) ||
							(predicateResults[i] == PredicateTranslationUtil.FALSE)) {
						result = PredicateTranslationUtil.FALSE;
					} else {
						result = PredicateTranslationUtil.UNKNOWN;
					}
				}
			}
			break;
		case XOR:
			for (int i = 0; i < predicateResults.length; i++) {
				if (i == 0) {
					result = predicateResults[i];
				} else {
					// regular XOR; return null if at least is missing
					if (result != PredicateTranslationUtil.UNKNOWN &&
							predicateResults[i] != PredicateTranslationUtil.UNKNOWN) {
						result = ((result == PredicateTranslationUtil.TRUE) ^
								(predicateResults[i] == PredicateTranslationUtil.TRUE)) ?
										PredicateTranslationUtil.TRUE :
										PredicateTranslationUtil.FALSE;
					} else {
						result = PredicateTranslationUtil.UNKNOWN;
					}
				}
			}

			break;
		}
		return result;
	}

}
