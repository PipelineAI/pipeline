/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.FieldName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class CsvUtil {

	private CsvUtil() {
	}

	public static List<Map<FieldName, String>> load(InputStream is) throws IOException {
		List<Map<FieldName, String>> table = new ArrayList<Map<FieldName, String>>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "US-ASCII"));

		try {
			List<FieldName> keys = new ArrayList<FieldName>();

			String headerLine = reader.readLine();

			List<String> headerCells = parseLine(headerLine);
			for (int i = 0; i < headerCells.size(); i++) {
				keys.add(new FieldName(headerCells.get(i)));
			}

			while (true) {
				String bodyLine = reader.readLine();
				if (bodyLine == null) {
					break;
				}

				Map<FieldName, String> row = new LinkedHashMap<FieldName, String>();

				List<String> bodyCells = parseLine(bodyLine);

				// Must be of equal length
				if (bodyCells.size() != headerCells.size()) {
					throw new RuntimeException();
				}

				for (int i = 0; i < bodyCells.size(); i++) {
					row.put(keys.get(i), bodyCells.get(i));
				}

				table.add(row);
			}
		} finally {
			reader.close();
		}

		return table;
	}

	private static List<String> parseLine(String line) {
		return Arrays.asList(line.split(","));
	}
}
