/*
 * Copyright (c) 2011 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.DataType;
import com.turn.tpmml.FieldName;
import com.turn.tpmml.FieldUsageType;
import com.turn.tpmml.NoTrueChildStrategyType;
import com.turn.tpmml.Node;
import com.turn.tpmml.OpType;
import com.turn.tpmml.SimplePredicate;
import com.turn.tpmml.TreeModel;
import com.turn.tpmml.manager.ModelManagerException;
import com.turn.tpmml.manager.TreeModelManager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class NoTrueChildStrategyTest {

	@Test
	public void returnNullPrediction() throws EvaluationException, ModelManagerException {
		TreeModelEvaluator2 treeModelManager =
				prepareModel(NoTrueChildStrategyType.RETURN_NULL_PREDICTION);

		FieldName name = new FieldName("prob1");

		Node node = treeModelManager.evaluateTree(new LocalEvaluationContext(name, 0d));

		assertNull(node);

		Node t1 = treeModelManager.evaluateTree(new LocalEvaluationContext(name, 1d));

		assertNotNull(t1);
		assertEquals("T1", t1.getId());
	}

	@Test
	public void returnLastPrediction() throws EvaluationException, ModelManagerException {
		TreeModelEvaluator2 treeModelManager =
				prepareModel(NoTrueChildStrategyType.RETURN_LAST_PREDICTION);

		FieldName name = new FieldName("prob1");

		Node n1 = treeModelManager.evaluateTree(new LocalEvaluationContext(name, 0d));

		assertNotNull(n1);
		assertEquals("N1", n1.getId());

		Node t1 = treeModelManager.evaluateTree(new LocalEvaluationContext(name, 1d));

		assertNotNull(t1);
		assertEquals("T1", t1.getId());
	}

	private static TreeModelEvaluator2 prepareModel(NoTrueChildStrategyType noTrueChildStrategy)
			throws ModelManagerException {
		TreeModelManager treeModelManager = new TreeModelManager();

		TreeModel treeModel = treeModelManager.createClassificationModel();
		treeModel.setNoTrueChildStrategy(noTrueChildStrategy);

		FieldName prob1 = new FieldName("prob1");
		treeModelManager.addField(prob1, null, OpType.CONTINUOUS, DataType.DOUBLE,
				FieldUsageType.ACTIVE);

		Node n1 = treeModelManager.getOrCreateRoot();
		n1.setId("N1");
		n1.setScore("0");

		SimplePredicate t1Predicate =
				new SimplePredicate(prob1, SimplePredicate.Operator.GREATER_THAN);
		t1Predicate.setValue("0.33");

		Node t1 = treeModelManager.addNode(n1, t1Predicate);
		t1.setId("T1");
		t1.setScore("1");

		return new TreeModelEvaluator2(treeModelManager);
	}
}
