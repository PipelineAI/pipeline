/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.InlineTable;
import com.turn.tpmml.Row;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

public class TableUtil {

	private TableUtil() {
	}

	public static List<Map<String, String>> parse(InlineTable table) {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();

		List<Row> rows = table.getRows();
		for (Row row : rows) {
			Map<String, String> map = new LinkedHashMap<String, String>();

			List<Object> cells = row.getContent();
			for (Object cell : cells) {

				if (cell instanceof Element) {
					Element element = (Element) cell;

					map.put(element.getTagName(), element.getTextContent());
				}
			}

			result.add(map);
		}

		return result;
	}

	public static Map<String, String> match(List<Map<String, String>> rows, Map<String, ?> values)
			throws EvaluationException {

		rows: for (Map<String, String> row : rows) {

			// A table row contains a certain number of input columns, plus an output column
			if (values.size() < (row.size() - 1)) {
				continue rows;
			}

			Set<? extends Map.Entry<String, ?>> entries = values.entrySet();
			for (Map.Entry<String, ?> entry : entries) {
				String rowValue = row.get(entry.getKey());
				if (rowValue == null) {
					continue rows;
				}

				boolean equals = ParameterUtil.equals(entry.getValue(), rowValue);
				if (!equals) {
					continue rows;
				}
			}

			return row;
		}

		return null;
	}
}
