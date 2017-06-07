/*
 * Copyright (c) 2009 University of Tartu
 */
package com.turn.tpmml.manager;

import com.turn.tpmml.MiningFunctionType;
import com.turn.tpmml.MiningSchema;
import com.turn.tpmml.Node;
import com.turn.tpmml.PMML;
import com.turn.tpmml.Predicate;
import com.turn.tpmml.ScoreDistribution;
import com.turn.tpmml.TreeModel;
import com.turn.tpmml.True;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TreeModelManager extends ModelManager<TreeModel> {

	private static final long serialVersionUID = 1L;

	private TreeModel treeModel = null;

	private Node root = null;

	private Set<String> ids = null;

	public TreeModelManager() {
	}

	public TreeModelManager(PMML pmml) {
		this(pmml, find(pmml.getContent(), TreeModel.class));
	}

	public TreeModelManager(PMML pmml, TreeModel treeModel) {
		super(pmml);

		this.treeModel = treeModel;
	}

	public String getSummary() {
		return "Tree";
	}

	@Override
	public TreeModel getModel() throws ModelManagerException {
		ensureNotNull(this.treeModel);

		return this.treeModel;
	}

	public TreeModel createClassificationModel() throws ModelManagerException {
		return createModel(MiningFunctionType.CLASSIFICATION);
	}

	/**
	 * @throws ModelManagerException If the Model already exists
	 * 
	 * @see #getModel()
	 */
	public TreeModel createModel(MiningFunctionType miningFunction) throws ModelManagerException {
		ensureNull(this.treeModel);

		this.treeModel = new TreeModel(new MiningSchema(), new Node(), miningFunction);

		getModels().add(this.treeModel);

		return this.treeModel;
	}

	/**
	 * @return The root Node
	 * @throws ModelManagerException 
	 */
	public Node getOrCreateRoot() throws ModelManagerException {

		if (this.root == null) {
			TreeModel treeModel = getModel();

			this.root = treeModel.getNode();
			if (this.root == null) {
				this.root = new Node();

				treeModel.setNode(this.root);
			}

			Predicate predicate = this.root.getPredicate();
			if (predicate == null) {
				this.root.setPredicate(new True());
			}
		}

		return this.root;
	}

	/**
	 * Adds a new Node to the root Node.
	 * 
	 * @return The newly added Node
	 * @throws ModelManagerException 
	 * 
	 * @see #getOrCreateRoot()
	 */
	public Node addNode(Predicate predicate) throws ModelManagerException {
		return addNode(getOrCreateRoot(), predicate);
	}

	/**
	 * Adds a new Node to the specified Node.
	 * 
	 * @return The newly added Node
	 */
	public Node addNode(Node parentNode, Predicate predicate) {
		Node node = new Node();
		node.setPredicate(predicate);

		parentNode.getNodes().add(node);

		return node;
	}

	public ScoreDistribution getOrAddScoreDistribution(Node node, String value) {
		List<ScoreDistribution> scoreDistributions = node.getScoreDistributions();

		for (ScoreDistribution scoreDistribution : scoreDistributions) {

			if ((scoreDistribution.getValue()).equals(value)) {
				return scoreDistribution;
			}
		}

		ScoreDistribution scoreDistribution = new ScoreDistribution(value, 0);
		scoreDistributions.add(scoreDistribution);

		return scoreDistribution;
	}

	public Set<String> getAllNodesId() throws ModelManagerException {
		if (ids == null) {
			// Fill ids
			Node root = getOrCreateRoot();
			ids = new HashSet<String>();
			// This function fills ids.
			getAllNodesIdFrom(root);
		}

		return ids;
	}

	private void getAllNodesIdFrom(Node current) {
		if (current != null) {
			if (current.getId() != null) {
				ids.add(current.getId());
			}
			for (Node child : current.getNodes()) {
				getAllNodesIdFrom(child);
			}
		}
	}

}
