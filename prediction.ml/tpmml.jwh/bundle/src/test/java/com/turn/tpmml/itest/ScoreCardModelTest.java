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
public class ScoreCardModelTest extends BaseModelTest {
	@Test
	public void testSampleScoreCardModel() throws Exception {
		PMML pmmlDoc = IOUtil.unmarshal(getClass().getResourceAsStream("/scorecard.xml"));
		Map<String, List<?>> variableToValues = new HashMap<String, List<?>>();
		// variableToValues.put("department", "engineering");
		variableToValues.put("age", Arrays.asList(22, 35, 45));
		variableToValues.put("income", Arrays.asList(1600, 1000, 500));
		variableToValues.put("department", Arrays.asList("engineering", "marketing", "business"));

		testModelEvaluation(pmmlDoc, SAMPLE_SCORECARD_MODEL_TEMPLATE, new SampleScoreCardModel(),
				variableToValues, 20);
	}

	@Test
	public void testSampleScoreCardModel2() throws Exception {
		PMML pmmlDoc = IOUtil.unmarshal(getClass().getResourceAsStream("/scorecard.xml"));
		Map<String, List<?>> variableToValues = new HashMap<String, List<?>>();
		// variableToValues.put("department", "engineering");
		variableToValues.put("age", Arrays.asList(22, 35, 45));
		variableToValues.put("income", Arrays.asList(1600, 1000, 500));
		variableToValues.put("department", Arrays.asList("engineering", "marketing", "business"));

		testModelEvaluation(pmmlDoc, SAMPLE_SCORECARD_MODEL_TEMPLATE, new SampleScoreCardModel(),
				variableToValues, 20);
	}

	@Test
	public void testSampleScoreCardModelModifiedName() throws Exception {
		PMML pmmlDoc = IOUtil.unmarshal(getClass().getResourceAsStream("/scorecard2.xml"));
		Map<String, List<?>> variableToValues = new HashMap<String, List<?>>();
		// variableToValues.put("department", "engineering");
		variableToValues.put("age", Arrays.asList(22, 35, 45));
		variableToValues.put("income", Arrays.asList(1600, 1000, 500));
		variableToValues.put("department", Arrays.asList("engineering", "marketing", "business"));

		testModelEvaluation(pmmlDoc, SAMPLE_SCORECARD_MODEL_TEMPLATE_MODIFIED_NAME,
				new SampleScoreCardModel(), variableToValues, 20, new TranslationContext() {
					// override missing value method, since in our template numeric
					// variables represented with Double class
					public String getMissingValue(OpType variableType) {
						if (variableType == OpType.CONTINUOUS) {
							return "null";
						}

						return super.getMissingValue(variableType);
					}

					public String getModelResultTrackingVariable() {
						return "resultExplanation";
					}

					@Override
					public String formatVariableName(ModelManager<?> modelManager,
							FieldName variableName) {
						return "p_" + variableName.getValue();
					}
				});
	}

	private static final String SAMPLE_SCORECARD_MODEL_TEMPLATE = "" +
			"package com.turn.tpmml.itest;\n" + "import java.util.Map;\n" +
			"import com.turn.tpmml.itest.BaseModelTest.CompiledModel;\n" + "" +
			"#foreach($import in $imports) \n" + "${import}\n" + "#end\n" + "\n" +
			"#foreach($constant in $constants) \n" + "static private final ${constant}\n" +
			"#end" + "\n" + "public class ${className} implements CompiledModel {\n" + "\n" +
			"	public Object execute(Map<String, Object> nameToValue) {\n" +
			"		Double overallScore = 0.0;\n" +
			"		Integer age = (Integer)nameToValue.get(\"age\");\n" +
			"		Integer income = (Integer)nameToValue.get(\"income\");\n" +
			"		String department = (String)nameToValue.get(\"department\");\n" + "		\n" +
			"${modelCode}\n" + "		return overallScore;\n" + "	}\n" +
			"	String resultExplanation = null;\n" +
			" 	public String getResultExplanation() {\n" +
			" 		return resultExplanation;\n" + "	}\n" + "}\n";
	
	private static final String SAMPLE_SCORECARD_MODEL_TEMPLATE_MODIFIED_NAME = "" +
			"package com.turn.tpmml.itest;\n" + "import java.util.Map;\n" +
			"import com.turn.tpmml.itest.BaseModelTest.CompiledModel;\n" + "" +
			"#foreach($import in $imports) \n" + "${import}\n" + "#end\n" + "\n" +
			"#foreach($constant in $constants) \n" + "static private final ${constant}\n" +
			"#end" + "\n" + "public class ${className} implements CompiledModel {\n" + "\n" +
			"	public Object execute(Map<String, Object> nameToValue) {\n" +
			"		Double overallScore = 0.0;\n" +
			"		Integer p_age = (Integer)nameToValue.get(\"age\");\n" +
			"		Integer p_income = (Integer)nameToValue.get(\"income\");\n" +
			"		String p_department = (String)nameToValue.get(\"department\");\n" +
			"		\n" +
			"${modelCode}\n" + "		return overallScore;\n" + "	}\n" +
			"	String resultExplanation = null;\n" +
			" 	public String getResultExplanation() {\n" +
			" 		return resultExplanation;\n" + "	}\n" + "}\n";

	protected double getMissingVarProbability() {
		return 0.01;
	}

	public static class SampleScoreCardModel implements ManualModelImplementation {

		public Object execute(Map<String, Object> nameToValue) {
			double score = 0.0;

			TreeMap<Double, String> diffToReasonCode = new TreeMap<Double, String>();

			String department = (String) nameToValue.get("department");
			Integer age = (Integer) nameToValue.get("age");
			Integer income = (Integer) nameToValue.get("income");

			// Department score
			Double baselineScore = 19.0;
			Double diff = 0.0;
			if (department == null) {
			} else if (department.equals("marketing")) {
				score += 19;
				diff = baselineScore - 19.0;
			} else if (department.equals("engineering")) {
				score += 3;
				diff = baselineScore - 3.0;
			} else if (department.equals("business")) {
				score += 6;
				diff = baselineScore - 6.0;
			} else {
			}
			diffToReasonCode.put(diff, "RC1");

			baselineScore = 18.0;
			// Age score
			if (age == null) {
				score += -1;
				diff = baselineScore + 1;
			} else if (isInRange(age, 0, 18)) {
				score += -3;
				diff = baselineScore + 3;
			} else if (isInRange(age, 19, 29)) {
				// Verbose but explicit...
				score += 0;
				diff = baselineScore;
			} else if (isInRange(age, 30, 39)) {
				score += 12;
				diff = baselineScore - 12;
			} else if (age >= 40) {
				score += 18;
				diff = baselineScore - 18;
			} else {
				score += -1;
				diff = baselineScore + 1;
			}

			diffToReasonCode.put(diff, "RC2");
			diff = 0.0;
			baselineScore = 10.0;

			// Income score
			if (income == null) {
				score += 5;
				diff = baselineScore - 5.0;
			} else if (income <= 1000) {
				score += 26;
				diff = baselineScore - 26;
			} else if (income > 1000 && income <= 1500) {
				score += 5;
				diff = baselineScore - 5;
			} else if (income > 1500) {
				score += -3;
				diff = baselineScore + 3;
			}

			diffToReasonCode.put(diff, "RC3");

			// The code that gives the reason code:
			resultExplanation = diffToReasonCode.lastEntry().getValue();

			return score;
		}

		String resultExplanation = null;

		public String getResultExplanation() {
			return resultExplanation;
		}

		private Boolean isInRange(Integer value, Integer lowerBound, Integer upperBound) {
			return lowerBound <= value && value <= upperBound;
		}
	}

}
