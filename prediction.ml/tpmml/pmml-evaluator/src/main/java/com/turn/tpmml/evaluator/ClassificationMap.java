/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

class ClassificationMap extends LinkedHashMap<String, Double> implements Classification {

	private static final long serialVersionUID = 1L;

	ClassificationMap() {
	}

	public String getResult() throws EvaluationException {
		Map.Entry<String, Double> result = null;

		Collection<Map.Entry<String, Double>> entries = entrySet();
		for (Map.Entry<String, Double> entry : entries) {
			if (result == null || (entry.getValue()).compareTo(result.getValue()) >= 0) {
				result = entry;
			}
		}

		if (result == null) {
			throw new EvaluationException("There are no results for this instance");
		}

		return result.getKey();
	}

	public Double getProbability(String value) {
		Double result = get(value);

		// The specified value was not encountered during scoring
		if (result == null) {
			result = 0d;
		}

		return result;
	}

	void normalizeProbabilities() {
		double sum = 0;

		Collection<Double> values = values();
		for (Double value : values) {
			sum += value.doubleValue();
		}

		Collection<Map.Entry<String, Double>> entries = entrySet();
		for (Map.Entry<String, Double> entry : entries) {
			entry.setValue(entry.getValue() / sum);
		}
	}
}
