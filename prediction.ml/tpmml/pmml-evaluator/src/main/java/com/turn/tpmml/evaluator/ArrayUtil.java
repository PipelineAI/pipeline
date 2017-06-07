/*
 * Copyright (c) 2012 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.Array;
import com.turn.tpmml.DataType;
import com.turn.tpmml.manager.TPMMLException.TPMMLCause;

import java.util.ArrayList;
import java.util.List;

public class ArrayUtil {

	private ArrayUtil() {
	}

	public static Boolean isIn(Array array, Object value) throws EvaluationException {
		List<String> values = getContent(array);

		validateDataType(value);

		boolean result = values.contains(ParameterUtil.toString(value));

		return Boolean.valueOf(result);
	}

	public static Boolean isNotIn(Array array, Object value) throws EvaluationException {
		List<String> values = getContent(array);

		validateDataType(value);

		boolean result = !values.contains(ParameterUtil.toString(value));

		return Boolean.valueOf(result);
	}

	public static List<String> getContent(Array array) throws EvaluationException {
		List<String> values = array.getContent();

		if (values == null) {
			values = tokenize(array);

			array.setContent(values);
		}

		return values;
	}

	public static List<String> tokenize(Array array) throws EvaluationException {
		List<String> result;

		Array.Type type = array.getType();
		switch (type) {
		case INT:
		case REAL:
			result = tokenize(array.getValue(), false);
			break;
		case STRING:
			result = tokenize(array.getValue(), true);
			break;
		default:
			throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION, type.name());
		}

		Number n = array.getN();
		if (n != null && n.intValue() != result.size()) {
			throw new EvaluationException("Wrong size in the array, expected " +
					n + ", got " + result.size());
		}

		return result;
	}

	public static List<String> tokenize(String string, boolean enableQuotes) {
		List<String> result = new ArrayList<String>();

		StringBuffer sb = new StringBuffer();

		boolean quoted = false;

		tokens: for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);

			if (quoted) {

				if (c == '\\' && i < (string.length() - 1)) {
					c = string.charAt(i + 1);

					if (c == '\"') {
						sb.append('\"');

						i++;
					} else {
						sb.append('\\');
					}

					continue tokens;
				}

				sb.append(c);

				if (c == '\"') {
					result.add(createToken(sb, enableQuotes));

					quoted = false;
				}
			} else {
				if (c == '\"' && enableQuotes) {

					if (sb.length() > 0) {
						result.add(createToken(sb, enableQuotes));
					}

					sb.append('\"');

					quoted = true;
				} else

				if (Character.isWhitespace(c)) {

					if (sb.length() > 0) {
						result.add(createToken(sb, enableQuotes));
					}
				} else {
					sb.append(c);
				}
			}
		}

		if (sb.length() > 0) {
			result.add(createToken(sb, enableQuotes));
		}

		return result;
	}

	private static String createToken(StringBuffer sb, boolean enableQuotes) {
		String result;

		if (sb.length() > 1 && (sb.charAt(0) == '\"' && sb.charAt(sb.length() - 1) == '\"') &&
				enableQuotes) {
			result = sb.substring(1, sb.length() - 1);
		} else {
			result = sb.substring(0, sb.length());
		}

		sb.setLength(0);

		return result;
	}

	private static void validateDataType(Object value) throws EvaluationException {
		DataType dataType = ParameterUtil.getDataType(value);

		switch (dataType) {
		case STRING:
		case INTEGER:
			break;
		case FLOAT:
		case DOUBLE:
		default:
			throw new EvaluationException(TPMMLCause.UNSUPPORTED_OPERATION, dataType.name());
		}
	}
}
