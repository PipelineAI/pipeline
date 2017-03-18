/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TableUtilTest {

	@Test
	public void matchSingleColumn() throws EvaluationException {
		Map<String, String> first = createRow(new String[][] { { "value", "1" },
				{ "output", "first" } });
		Map<String, String> second = createRow(new String[][] { { "value", "2" },
				{ "output", "second" } });
		Map<String, String> third = createRow(new String[][] { { "value", "3" },
				{ "output", "third" } });

		@SuppressWarnings("unchecked")
		List<Map<String, String>> rows = Arrays.asList(first, second, third);

		assertEquals(third, TableUtil.match(rows, createRow(new String[][] { { "value", "3" } })));
		assertEquals(null,
				TableUtil.match(rows, createRow(new String[][] { { "value", "three" } })));
	}

	@Test
	public void matchMultipleColumns() throws EvaluationException {
		Map<String, String> firstTrue = createRow(new String[][] { { "value", "1" },
				{ "flag", "true" }, { "output", "firstTrue" } });
		Map<String, String> firstFalse = createRow(new String[][] { { "value", "1" },
				{ "flag", "false" }, { "output", "firstFalse" } });
		Map<String, String> secondTrue = createRow(new String[][] { { "value", "2" },
				{ "flag", "true" }, { "output", "secondTrue" } });
		Map<String, String> secondFalse = createRow(new String[][] { { "value", "2" },
				{ "flag", "false" }, { "output", "secondFalse" } });
		Map<String, String> thirdTrue = createRow(new String[][] { { "value", "3" },
				{ "flag", "true" }, { "output", "thirdTrue" } });
		Map<String, String> thirdFalse = createRow(new String[][] { { "value", "3" },
				{ "flag", "false" }, { "output", "thirdFalse" } });

		@SuppressWarnings("unchecked")
		List<Map<String, String>> rows = Arrays.asList(firstTrue, firstFalse, secondTrue,
				secondFalse, thirdTrue, thirdFalse);

		assertEquals(null, TableUtil.match(rows, createRow(new String[][] { { "value", "3" } })));

		assertEquals(
				thirdTrue,
				TableUtil.match(rows, createRow(new String[][] { { "value", "3" },
						{ "flag", "true" } })));
		assertEquals(
				null,
				TableUtil.match(rows, createRow(new String[][] { { "value", "three" },
						{ "flag", "true" } })));
	}

	private static Map<String, String> createRow(String[][] strings) {
		Map<String, String> result = new LinkedHashMap<String, String>();

		for (int i = 0; i < strings.length; i++) {
			result.put(strings[i][0], strings[i][1]);
		}

		return result;
	}
}
