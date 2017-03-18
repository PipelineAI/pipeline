package com.turn.tpmml.itest;

import com.turn.tpmml.FieldName;
import com.turn.tpmml.IOUtil;
import com.turn.tpmml.OpType;
import com.turn.tpmml.PMML;
import com.turn.tpmml.manager.ModelManager;
import com.turn.tpmml.translator.TranslationContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.testng.annotations.Test;

@Test
public class CompileModelTest extends BaseModelTest {
	@Test
	public void testCensusCompileModel() throws Exception {
		PMML pmmlDoc = IOUtil.unmarshal(getClass().getResourceAsStream("/pmml_census.pmml"));
		compileModel(pmmlDoc, SAMPLE_CENSUS_MODEL_TEMPLATE);
	}

	@Test
	public void testAirbnbCompileModel() throws Exception {
		PMML pmmlDoc = IOUtil.unmarshal(getClass().getResourceAsStream("/pmml_airbnb.pmml"));
		compileModel(pmmlDoc, SAMPLE_AIRBNB_MODEL_TEMPLATE);
	}
	private static final String SAMPLE_CENSUS_MODEL_TEMPLATE = "" +
			"package com.turn.tpmml.itest;\n" + "import java.util.Map;\n" +
			"import com.turn.tpmml.itest.BaseModelTest.CompiledModel;\n" + "" +
			"#foreach($import in $imports) \n" + "${import}\n" + "#end\n" + "\n" +
			"#foreach($constant in $constants) \n" + "static private final ${constant}\n" +
			"#end" + "\n" + "public class ${className} implements CompiledModel {\n" + "\n" +
			"	public Object execute(Map<String, Object> nameToValue) {\n" +
			"		String income = (String)nameToValue.get(\"income\");\n" + "		\n" +
			"		String education = (String)nameToValue.get(\"education\");\n" + "		\n" +
			"		String marital_status = (String)nameToValue.get(\"marital_status\");\n" + "		\n" +
			"		String occupation = (String)nameToValue.get(\"occupation\");\n" + "		\n" +
			"		String native_country = (String)nameToValue.get(\"native_country\");\n" + "		\n" +
			"		Integer age = (Integer)nameToValue.get(\"age\");\n" +
			"		Integer education_num = (Integer)nameToValue.get(\"education_num\");\n" +
			"		Integer capital_gain = (Integer)nameToValue.get(\"capital_gain\");\n" +
			"		Integer capital_loss = (Integer)nameToValue.get(\"capital_loss\");\n" +
			"		Integer hours_per_week = (Integer)nameToValue.get(\"hours_per_week\");\n" +
			"${modelCode}\n" + "		return income;\n" + "	}\n" +
			"	String resultExplanation = null;\n" +
			" 	public String getResultExplanation() {\n" +
			" 		return resultExplanation;\n" + "	}\n" +
      "}\n";

	private static final String SAMPLE_AIRBNB_MODEL_TEMPLATE = "" +
			"package com.turn.tpmml.itest;\n" + "import java.util.Map;\n" +
			"import com.turn.tpmml.itest.BaseModelTest.CompiledModel;\n" + "" +
			"#foreach($import in $imports) \n" + "${import}\n" + "#end\n" + "\n" +
			"#foreach($constant in $constants) \n" + "static private final ${constant}\n" +
			"#end" + "\n" + "public class ${className} implements CompiledModel {\n" + "\n" +
			"	public Object execute(Map<String, Object> nameToValue) {\n" +
			"		Double bathrooms = (Double)nameToValue.get(\"bathrooms\");\n" + "		\n" +
			"		Double bedrooms = (Double)nameToValue.get(\"bedrooms\");\n" + "		\n" +
			"		Double security_deposit = (Double)nameToValue.get(\"security_deposit\");\n" + "		\n" +
			"		Double cleaning_fee = (Double)nameToValue.get(\"cleaning_fee\");\n" + "		\n" +
			"		Double extra_people = (Double)nameToValue.get(\"extra_people\");\n" + "		\n" +
			"		Double number_of_reviews = (Double)nameToValue.get(\"number_of_reviews\");\n" + "		\n" +
			"		Double square_feet = (Double)nameToValue.get(\"square_feet\");\n" + "		\n" +
			"		Double review_scores_rating = (Double)nameToValue.get(\"review_scores_rating\");\n" + "		\n" +
			"		String room_type = (String)nameToValue.get(\"room_type\");\n" + "		\n" +
			"		String host_is_super_host = (String)nameToValue.get(\"host_is_super_host\");\n" + "		\n" +
			"		String cancellation_policy = (String)nameToValue.get(\"cancellation_policy\");\n" + "		\n" +
			"		String instant_bookable = (String)nameToValue.get(\"instant_bookable\");\n" + "		\n" +
			"		String state = (String)nameToValue.get(\"state\");\n" + "		\n" +
			"		Double price = (Double)nameToValue.get(\"price\");\n" +
			"${modelCode}\n" + "		return price;\n" + "	}\n" +
			"	String resultExplanation = null;\n" +
			" 	public String getResultExplanation() {\n" +
			" 		return resultExplanation;\n" + "	}\n" +
      "}\n";
}
