/*
 * Copyright (c) 2011 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.Array;
import com.turn.tpmml.CompoundPredicate;
import com.turn.tpmml.False;
import com.turn.tpmml.Predicate;
import com.turn.tpmml.SimplePredicate;
import com.turn.tpmml.SimpleSetPredicate;
import com.turn.tpmml.True;
import com.turn.tpmml.manager.TPMMLException.TPMMLCause;

import java.util.List;

public class PredicateUtil {

	private PredicateUtil() {
	}

	public static Boolean evaluate(Predicate predicate, EvaluationContext context)
			throws EvaluationException {

		if (predicate instanceof SimplePredicate) {
			return evaluateSimplePredicate((SimplePredicate) predicate, context);
		} else

		if (predicate instanceof CompoundPredicate) {
			return evaluateCompoundPredicate((CompoundPredicate) predicate, context);
		} else

		if (predicate instanceof SimpleSetPredicate) {
			return evaluateSimpleSetPredicate((SimpleSetPredicate) predicate, context);
		} else

		if (predicate instanceof True) {
			return evaluateTrue((True) predicate);
		} else

		if (predicate instanceof False) {
			return evaluateFalse((False) predicate);
		} else {
			throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
					predicate.toString());
		}
	}

	public static Boolean evaluateSimplePredicate(SimplePredicate simplePredicate,
			EvaluationContext context) throws EvaluationException {
		Object value = ExpressionUtil.evaluate(simplePredicate.getField(), context);

		switch (simplePredicate.getOperator()) {
		case IS_MISSING:
			return Boolean.valueOf(value == null);
		case IS_NOT_MISSING:
			return Boolean.valueOf(value != null);
		default:
			break;
		}

		if (value == null) {
			return null;
		}

		int order = ParameterUtil.compare(value, simplePredicate.getValue());

		SimplePredicate.Operator operator = simplePredicate.getOperator();
		switch (operator) {
		case EQUAL:
			return Boolean.valueOf(order == 0);
		case NOT_EQUAL:
			return Boolean.valueOf(order != 0);
		case LESS_THAN:
			return Boolean.valueOf(order < 0);
		case LESS_OR_EQUAL:
			return Boolean.valueOf(order <= 0);
		case GREATER_THAN:
			return Boolean.valueOf(order > 0);
		case GREATER_OR_EQUAL:
			return Boolean.valueOf(order >= 0);
		default:
			throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
					operator.name());
		}
	}

	public static Boolean evaluateCompoundPredicate(CompoundPredicate compoundPredicate,
			EvaluationContext context) throws EvaluationException {
		List<Predicate> predicates = compoundPredicate.getContent();

		Boolean result = evaluate(predicates.get(0), context);

		switch (compoundPredicate.getBooleanOperator()) {
		case AND:
		case OR:
		case XOR:
			break;
		case SURROGATE:
			if (result != null) {
				return result;
			}
			break;
		}

		for (Predicate predicate : predicates.subList(1, predicates.size())) {
			Boolean value = evaluate(predicate, context);

			switch (compoundPredicate.getBooleanOperator()) {
			case AND:
				result = PredicateUtil.binaryAnd(result, value);
				break;
			case OR:
				result = PredicateUtil.binaryOr(result, value);
				break;
			case XOR:
				result = PredicateUtil.binaryXor(result, value);
				break;
			case SURROGATE:
				if (value != null) {
					return value;
				}
				break;
			}
		}

		return result;
	}

	public static Boolean evaluateSimpleSetPredicate(SimpleSetPredicate simpleSetPredicate,
			EvaluationContext context) throws EvaluationException {
		Object value = ExpressionUtil.evaluate(simpleSetPredicate.getField(), context);
		if (value == null) {
			throw new EvaluationException(TPMMLCause.MISSING_PARAMETER,
					simpleSetPredicate.getField());
		}

		Array array = simpleSetPredicate.getArray();

		SimpleSetPredicate.BooleanOperator operator = simpleSetPredicate.getBooleanOperator();
		switch (operator) {
		case IS_IN:
			return ArrayUtil.isIn(array, value);
		case IS_NOT_IN:
			return ArrayUtil.isNotIn(array, value);
		default:
			throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION,
					operator.name());
		}
	}

	public static Boolean evaluateTrue(True truePredicate) {
		return Boolean.TRUE;
	}

	public static Boolean evaluateFalse(False falsePredicate) {
		return Boolean.FALSE;
	}

	public static Boolean binaryAnd(Boolean left, Boolean right) {

		if (left == null) {

			if (right == null || right.booleanValue()) {
				return null;
			} else {
				return Boolean.FALSE;
			}
		} else if (right == null) {

			if (left == null || left.booleanValue()) {
				return null;
			} else {
				return Boolean.FALSE;
			}
		} else {
			return Boolean.valueOf(left.booleanValue() & right.booleanValue());
		}
	}

	public static Boolean binaryOr(Boolean left, Boolean right) {

		if (left != null && left.booleanValue()) {
			return Boolean.TRUE;
		} else if (right != null && right.booleanValue()) {
			return Boolean.TRUE;
		} else if (left == null || right == null) {
			return null;
		} else {
			return Boolean.valueOf(left.booleanValue() | right.booleanValue());
		}
	}

	public static Boolean binaryXor(Boolean left, Boolean right) {

		if (left == null || right == null) {
			return null;
		} else {
			return Boolean.valueOf(left.booleanValue() ^ right.booleanValue());
		}
	}
}
