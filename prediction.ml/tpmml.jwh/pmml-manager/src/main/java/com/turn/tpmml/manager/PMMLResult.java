package com.turn.tpmml.manager;

import com.turn.tpmml.FieldName;

import java.util.HashMap;
import java.util.Map;


/**
 * This is the mother class of the Result hierarchy.
 * There is some child classes that are used when some
 * particular information is needed, for example a node
 * id in a tree model.
 *
 * @author tbadie
 *
 */
public class PMMLResult implements IPMMLResult {
	HashMap<FieldName, Object> results;

	public PMMLResult() {
		results = new HashMap<FieldName, Object>();
	}

	@SuppressWarnings("unchecked")
	public PMMLResult(PMMLResult other) {
		results = (HashMap<FieldName, Object>) other.results.clone();
	}

	public Object getValue(FieldName key) throws ModelManagerException {
		if (!results.containsKey(key)) {
			throw new ModelManagerException("There is no field " + key.getValue() +
					" in the result.");
		}

		return results.get(key);
	}

	public boolean containsKey(FieldName key) {
		return results.containsKey(key);
	}

	/**
	 * Associate key with value. If key already exists, the old value is overridden.
	 * 
	 * @param key The key.
	 * @param value The value.
	 */
	public void put(FieldName key, Object value) {
		results.put(key, value);
	}

	/**
	 * Take a map and add all the content of the result to this map.
	 * 
	 * @param m The map to fill.
	 * @return
	 */
	public void merge(Map<FieldName, Object> m) {
		for (Map.Entry<FieldName, Object> e : results.entrySet()) {
			m.put(e.getKey(), e.getValue());
		}
	}

	public void absorb(Map<FieldName, Object> m) {
		for (Map.Entry<FieldName, Object> e : m.entrySet()) {
			results.put(e.getKey(), e.getValue());
		}
	}

	public void absorb(PMMLResult that) {
		absorb(that.results);
	}

	public Object getResult() throws ModelManagerException {
		if (results.size() == 1) {
			for (Map.Entry<FieldName, Object> e : results.entrySet()) {
				return e.getValue();
			}
		} else {
			if (!isEmpty()) {				
				throw new ModelManagerException("There is more than one result.");
			} else {
				throw new ModelManagerException("There is no result.");
			}
		}
		return null;
	}

	public Boolean isEmpty() {
		return results.isEmpty();
	}
}
