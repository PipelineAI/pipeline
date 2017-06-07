/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.Constant;
import com.turn.tpmml.DataType;
import com.turn.tpmml.Discretize;
import com.turn.tpmml.FieldColumnPair;
import com.turn.tpmml.FieldName;
import com.turn.tpmml.FieldRef;
import com.turn.tpmml.MapValues;
import com.turn.tpmml.NormContinuous;
import com.turn.tpmml.NormDiscrete;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExpressionUtilTest {

	@Test
	public void evaluateConstant() throws EvaluationException {
		Constant stringThree = new Constant("3");
		stringThree.setDataType(DataType.STRING);
		assertEquals("3", ExpressionUtil.evaluate(stringThree, null));

		Constant integerThree = new Constant("3");
		integerThree.setDataType(DataType.INTEGER);
		assertEquals(3, ExpressionUtil.evaluate(integerThree, null));

		Constant floatThree = new Constant("3");
		floatThree.setDataType(DataType.FLOAT);
		assertEquals(3f, ExpressionUtil.evaluate(floatThree, null));
	}

	@Test
	public void evaluateFieldRef() throws EvaluationException {
		FieldName name = new FieldName("x");

		FieldRef expression = new FieldRef(name);
		assertEquals("3",
				ExpressionUtil.evaluate(expression, new LocalEvaluationContext(name, "3")));

		assertEquals(null,
				ExpressionUtil.evaluate(expression, new LocalEvaluationContext(name, null)));
		expression.setMapMissingTo("Missing");
		assertEquals("Missing",
				ExpressionUtil.evaluate(expression, new LocalEvaluationContext(name, null)));
	}

	@Test
	public void evaluateNormContinuous() throws EvaluationException {
		FieldName name = new FieldName("x");

		NormContinuous expression = new NormContinuous(name);

		expression.setMapMissingTo(5d);

		assertEquals(5d,
				ExpressionUtil.evaluate(expression, new LocalEvaluationContext(name, null)));
	}

	@Test
	public void evaluateNormDiscrete() throws EvaluationException {
		FieldName name = new FieldName("x");

		Double equals = 1d;
		Double notEquals = 0d;

		NormDiscrete stringThree = new NormDiscrete(name, "3");
		assertEquals(equals,
				ExpressionUtil.evaluate(stringThree, new LocalEvaluationContext(name, "3")));
		assertEquals(notEquals,
				ExpressionUtil.evaluate(stringThree, new LocalEvaluationContext(name, "1")));

		stringThree.setMapMissingTo(5d);

		assertEquals(5d,
				ExpressionUtil.evaluate(stringThree, new LocalEvaluationContext(name, null)));

		NormDiscrete integerThree = new NormDiscrete(name, "3");
		assertEquals(equals,
				ExpressionUtil.evaluate(integerThree, new LocalEvaluationContext(name, 3)));
		assertEquals(notEquals,
				ExpressionUtil.evaluate(integerThree, new LocalEvaluationContext(name, 1)));

		NormDiscrete floatThree = new NormDiscrete(name, "3.0");
		assertEquals(equals,
				ExpressionUtil.evaluate(floatThree, new LocalEvaluationContext(name, 3f)));
		assertEquals(notEquals,
				ExpressionUtil.evaluate(floatThree, new LocalEvaluationContext(name, 1f)));
	}

	@Test
	public void evaluateDiscretize() throws EvaluationException {
		FieldName name = new FieldName("x");

		Discretize expression = new Discretize(name);

		assertEquals(null, ExpressionUtil.evaluate(expression, new LocalEvaluationContext()));
		expression.setMapMissingTo("Missing");
		assertEquals("Missing", ExpressionUtil.evaluate(expression, new LocalEvaluationContext()));

		assertEquals(null,
				ExpressionUtil.evaluate(expression, new LocalEvaluationContext(name, "3")));
		expression.setDefaultValue("Default");
		assertEquals("Default",
				ExpressionUtil.evaluate(expression, new LocalEvaluationContext(name, "3")));
	}

	@Test
	public void evaluateMapValues() throws EvaluationException {
		FieldName name = new FieldName("x");

		MapValues expression = new MapValues(null);
		(expression.getFieldColumnPairs()).add(new FieldColumnPair(name, null));

		assertEquals(null, ExpressionUtil.evaluate(expression, new LocalEvaluationContext()));
		expression.setMapMissingTo("Missing");
		assertEquals("Missing", ExpressionUtil.evaluate(expression, new LocalEvaluationContext()));

		assertEquals(null,
				ExpressionUtil.evaluate(expression, new LocalEvaluationContext(name, "3")));
		expression.setDefaultValue("Default");
		assertEquals("Default",
				ExpressionUtil.evaluate(expression, new LocalEvaluationContext(name, "3")));
	}
}
