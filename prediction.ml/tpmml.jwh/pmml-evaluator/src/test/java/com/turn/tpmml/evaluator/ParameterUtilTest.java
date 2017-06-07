/*
 * Copyright (c) 2013 University of Tartu
 */
package com.turn.tpmml.evaluator;

import com.turn.tpmml.DataField;
import com.turn.tpmml.DataType;
import com.turn.tpmml.FieldName;
import com.turn.tpmml.Interval;
import com.turn.tpmml.Interval.Closure;
import com.turn.tpmml.InvalidValueTreatmentMethodType;
import com.turn.tpmml.MiningField;
import com.turn.tpmml.OpType;
import com.turn.tpmml.OutlierTreatmentMethodType;
import com.turn.tpmml.Value;
import com.turn.tpmml.Value.Property;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ParameterUtilTest {

	@Test
	public void prepare() throws EvaluationException {
		FieldName name = new FieldName("x");

		DataField dataField = new DataField(name, OpType.CONTINUOUS, DataType.DOUBLE);

		List<Value> fieldValues = dataField.getValues();
		List<Interval> fieldIntervals = dataField.getIntervals();

		MiningField miningField = new MiningField(name);

		miningField.setLowValue(1d);
		miningField.setHighValue(3d);

		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, "1"));
		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, 1));
		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, 1f));
		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, 1d));

		Value missingValue = createValue("N/A", Property.MISSING);

		fieldValues.add(missingValue);

		assertEquals(null, ParameterUtil.prepare(dataField, miningField, null));
		assertEquals(null, ParameterUtil.prepare(dataField, miningField, "N/A"));

		miningField.setMissingValueReplacement("0");

		assertEquals(0d, ParameterUtil.prepare(dataField, miningField, null));
		assertEquals(0d, ParameterUtil.prepare(dataField, miningField, "N/A"));

		fieldValues.clear();
		fieldIntervals.clear();

		fieldValues.add(missingValue);

		Interval validInterval = new Interval(Closure.CLOSED_CLOSED);
		validInterval.setLeftMargin(1d);
		validInterval.setRightMargin(3d);

		fieldIntervals.add(validInterval);

		miningField.setOutlierTreatment(OutlierTreatmentMethodType.AS_IS);
		miningField.setInvalidValueTreatment(InvalidValueTreatmentMethodType.AS_IS);

		assertEquals(-1d, ParameterUtil.prepare(dataField, miningField, -1d));
		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, 1d));
		assertEquals(5d, ParameterUtil.prepare(dataField, miningField, 5d));

		miningField.setOutlierTreatment(OutlierTreatmentMethodType.AS_EXTREME_VALUES);
		miningField.setInvalidValueTreatment(InvalidValueTreatmentMethodType.AS_IS);

		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, -1d));
		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, 1d));
		assertEquals(3d, ParameterUtil.prepare(dataField, miningField, 5d));

		miningField.setOutlierTreatment(OutlierTreatmentMethodType.AS_IS);
		miningField.setInvalidValueTreatment(InvalidValueTreatmentMethodType.AS_MISSING);

		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, 1d));
		assertEquals(0d, ParameterUtil.prepare(dataField, miningField, 5d));

		fieldValues.clear();
		fieldIntervals.clear();

		List<Value> validValues = new ArrayList<Value>();
		validValues.add(createValue("1", Value.Property.VALID));
		validValues.add(createValue("2", Value.Property.VALID));
		validValues.add(createValue("3", Value.Property.VALID));

		fieldValues.add(missingValue);
		fieldValues.addAll(validValues);

		miningField.setInvalidValueTreatment(InvalidValueTreatmentMethodType.AS_IS);

		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, 1d));
		assertEquals(5d, ParameterUtil.prepare(dataField, miningField, 5d));

		miningField.setInvalidValueTreatment(InvalidValueTreatmentMethodType.AS_MISSING);

		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, 1d));
		assertEquals(0d, ParameterUtil.prepare(dataField, miningField, 5d));

		fieldValues.clear();
		fieldIntervals.clear();

		List<Value> invalidValues = new ArrayList<Value>();
		invalidValues.add(createValue("1", Value.Property.INVALID));

		fieldValues.add(missingValue);
		fieldValues.addAll(invalidValues);

		miningField.setInvalidValueTreatment(InvalidValueTreatmentMethodType.AS_IS);

		assertEquals(1d, ParameterUtil.prepare(dataField, miningField, 1d));
		assertEquals(5d, ParameterUtil.prepare(dataField, miningField, 5d));

		miningField.setInvalidValueTreatment(InvalidValueTreatmentMethodType.AS_MISSING);

		assertEquals(0d, ParameterUtil.prepare(dataField, miningField, 1d));
		assertEquals(5d, ParameterUtil.prepare(dataField, miningField, 5d));
	}

	@Test
	public void equals() throws EvaluationException {
		assertTrue(ParameterUtil.equals("1", "1"));

		assertTrue(ParameterUtil.equals(1, "1"));

		assertTrue(ParameterUtil.equals(1f, "1"));
		assertTrue(ParameterUtil.equals(1.0f, "1"));
		assertTrue(ParameterUtil.equals(1f, "1.0"));

		assertTrue(ParameterUtil.equals(1d, "1"));
		assertTrue(ParameterUtil.equals(1.0d, "1"));
		assertTrue(ParameterUtil.equals(1d, "1.0"));
	}

	@Test
	public void compare() throws EvaluationException {
		assertTrue(ParameterUtil.compare("1", "1") == 0);

		assertTrue(ParameterUtil.compare(1, "1") == 0);

		assertTrue(ParameterUtil.compare(1f, "1") == 0);
		assertTrue(ParameterUtil.compare(1.0f, "1") == 0);
		assertTrue(ParameterUtil.compare(1f, "1.0") == 0);

		assertTrue(ParameterUtil.compare(1d, "1") == 0);
		assertTrue(ParameterUtil.compare(1.0d, "1") == 0);
		assertTrue(ParameterUtil.compare(1d, "1.0") == 0);
	}

	@Test
	public void getDataType() throws EvaluationException {
		assertEquals(DataType.STRING, ParameterUtil.getDataType("value"));

		assertEquals(DataType.INTEGER, ParameterUtil.getDataType(1));
		assertEquals(DataType.FLOAT, ParameterUtil.getDataType(1f));
		assertEquals(DataType.DOUBLE, ParameterUtil.getDataType(1d));
	}

	@Test
	public void getResultDataType() throws EvaluationException {
		assertEquals(DataType.DOUBLE, ParameterUtil.getResultDataType(1d, 1f));
		assertEquals(DataType.DOUBLE, ParameterUtil.getResultDataType(1d, 1));

		assertEquals(DataType.DOUBLE, ParameterUtil.getResultDataType(1f, 1d));
		assertEquals(DataType.FLOAT, ParameterUtil.getResultDataType(1f, 1));

		assertEquals(DataType.DOUBLE, ParameterUtil.getResultDataType(1, 1d));
		assertEquals(DataType.FLOAT, ParameterUtil.getResultDataType(1, 1f));
	}

	@Test
	public void getConstantDataType() {
		assertEquals(DataType.FLOAT, ParameterUtil.getConstantDataType("1.0"));
		assertEquals(DataType.FLOAT, ParameterUtil.getConstantDataType("1.0E0"));
		assertEquals(DataType.STRING, ParameterUtil.getConstantDataType("1.0X"));

		assertEquals(DataType.INTEGER, ParameterUtil.getConstantDataType("1"));
		assertEquals(DataType.STRING, ParameterUtil.getConstantDataType("1E0"));
		assertEquals(DataType.STRING, ParameterUtil.getConstantDataType("1X"));
	}

	private static Value createValue(String value, Value.Property property) {
		Value result = new Value(value);
		result.setProperty(property);

		return result;
	}
}
