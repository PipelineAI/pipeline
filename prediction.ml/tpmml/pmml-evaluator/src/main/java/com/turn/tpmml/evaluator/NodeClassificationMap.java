/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.Node;
import com.turn.tpmml.ScoreDistribution;

import java.util.List;


class NodeClassificationMap extends ClassificationMap {

	private static final long serialVersionUID = 1L;

	private Node node = null;

	private String score = null;

	NodeClassificationMap(Node node) {
		setNode(node);

		List<ScoreDistribution> scoreDistributions = node.getScoreDistributions();

		double sum = 0;

		for (ScoreDistribution scoreDistribution : scoreDistributions) {
			sum += scoreDistribution.getRecordCount();
		}

		ScoreDistribution result = null;

		for (ScoreDistribution scoreDistribution : scoreDistributions) {

			if (result == null || result.getRecordCount() < scoreDistribution.getRecordCount()) {
				result = scoreDistribution;
			}

			Double probability = scoreDistribution.getProbability();
			if (probability == null) {
				probability = (scoreDistribution.getRecordCount() / sum);
			}

			put(scoreDistribution.getValue(), probability);
		}

		String score = node.getScore();
		if (score == null) {
			score = result.getValue();
		}

		setScore(score);
	}

	@Override
	public String getResult() {
		return getScore();
	}

	public Node getNode() {
		return this.node;
	}

	private void setNode(Node node) {
		this.node = node;
	}

	public String getScore() {
		return this.score;
	}

	private void setScore(String score) {
		this.score = score;
	}
}
