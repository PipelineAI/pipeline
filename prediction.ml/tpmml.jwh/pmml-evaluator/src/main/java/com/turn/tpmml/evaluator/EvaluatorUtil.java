/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.FieldName;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;


public class EvaluatorUtil {

	private EvaluatorUtil() {
	}

	/**
	 * @throws EvaluationException 
	 * @see Computable
	 */
	public static Object decode(Object object) throws EvaluationException {

		if (object instanceof Computable) {
			Computable<?> computable = (Computable<?>) object;

			return computable.getResult();
		}

		return object;
	}

	/**
	 * Decouples a {@link Map} instance from the current runtime environment by decoding both its
	 * keys and values.
	 * @throws EvaluationException 
	 * 
	 * @see #decodeKeys(Map)
	 * @see #decodeValues(Map)
	 */
	public static Map<String, ?> decode(Map<FieldName, ?> map) throws EvaluationException {
		return decodeKeys(decodeValues(map));
	}

	/**
	 * Replaces String keys with {@link FieldName} keys.
	 */
	public static <V> Map<FieldName, V> encodeKeys(Map<String, V> map) {
		Map<FieldName, V> result = new LinkedHashMap<FieldName, V>();

		Collection<Map.Entry<String, V>> entries = map.entrySet();
		for (Map.Entry<String, V> entry : entries) {
			result.put(new FieldName(entry.getKey()), entry.getValue());
		}

		return result;
	}

	/**
	 * Replaces {@link FieldName} keys with String keys.
	 * 
	 * @see FieldName#getValue()
	 */
	public static <V> Map<String, V> decodeKeys(Map<FieldName, V> map) {
		Map<String, V> result = new LinkedHashMap<String, V>();

		Collection<Map.Entry<FieldName, V>> entries = map.entrySet();
		for (Map.Entry<FieldName, V> entry : entries) {
			result.put((entry.getKey()).getValue(), entry.getValue());
		}

		return result;
	}

	/**
	 * Replaces {@link Computable} complex values with simple values.
	 * @throws EvaluationException 
	 * 
	 * @see Computable
	 */
	public static <K> Map<K, ?> decodeValues(Map<K, ?> map) throws EvaluationException {
		Map<K, Object> result = new LinkedHashMap<K, Object>();

		Collection<? extends Map.Entry<K, ?>> entries = map.entrySet();
		for (Map.Entry<K, ?> entry : entries) {
			result.put(entry.getKey(), decode(entry.getValue()));
		}

		return result;
	}
}
