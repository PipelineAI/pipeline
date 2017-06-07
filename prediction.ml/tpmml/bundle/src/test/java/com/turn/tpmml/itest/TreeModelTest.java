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

import org.testng.annotations.Test;

@Test
public class TreeModelTest extends BaseModelTest {

	@Test
	public void testTroubledScenario() throws Exception {
		PMML pmmlDoc = IOUtil.unmarshal(getClass().getResourceAsStream("/golf_tree.xml"));
		Map<String, Object> variableToValues = new HashMap<String, Object>();

		variableToValues.put("humidity", 26.0);
		variableToValues.put("outlook", "sunny");

		runSingleModelEvaluation(pmmlDoc, GOLF_MODEL_TEMPLATE, new GolfModel(), variableToValues);
	}

	@Test
	public void testGolfModel() throws Exception {

		PMML pmmlDoc = IOUtil.unmarshal(getClass().getResourceAsStream("/golf_tree.xml"));
		Map<String, List<?>> variableToValues = new HashMap<String, List<?>>();
		variableToValues.put("temperature", null);
		variableToValues.put("humidity", null);
		variableToValues.put("windy", Arrays.asList("true", "false"));
		variableToValues.put("outlook", Arrays.asList("sunny", "outcast", "rain"));

		testModelEvaluation(pmmlDoc, GOLF_MODEL_TEMPLATE, new GolfModel(), variableToValues, 20);
	}

	@Test
	public void testGolfModelModifiedName() throws Exception {

		PMML pmmlDoc = IOUtil.unmarshal(getClass().getResourceAsStream("/golf_tree.xml"));
		Map<String, List<?>> variableToValues = new HashMap<String, List<?>>();
		variableToValues.put("temperature", null);
		variableToValues.put("humidity", null);
		variableToValues.put("windy", Arrays.asList("true", "false"));
		variableToValues.put("outlook", Arrays.asList("sunny", "outcast", "rain"));

		testModelEvaluation(pmmlDoc, GOLF_MODEL_TEMPLATE_MODIFIED_NAME, new GolfModel(),
				variableToValues, 20, new TranslationContext() {
					// override missing value method, since in our template numeric variables
					// represented with Double class
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

	@Test
	public void testGolfModelLastPrediction() throws Exception {

		PMML pmmlDoc = IOUtil.unmarshal(getClass().getResourceAsStream(
				"/golf_tree_last_prediction.xml"));
		Map<String, List<?>> variableToValues = new HashMap<String, List<?>>();
		variableToValues.put("temperature", null);
		variableToValues.put("humidity", null);
		variableToValues.put("windy", Arrays.asList("true", "false"));
		variableToValues.put("outlook", Arrays.asList("sunny", "outcast", "rain"));

		testModelEvaluation(pmmlDoc, GOLF_MODEL_TEMPLATE, new GolfModelLastPrediction(),
				variableToValues, 20);

	}

	protected double getMissingVarProbability() {
		return 0.01;
	}


	private static final String GOLF_MODEL_TEMPLATE = "" + "package com.turn.tpmml.itest;\n" +
			"import java.util.Map;\n" +
			"import com.turn.tpmml.itest.BaseModelTest.CompiledModel;\n" + "" +
			"#foreach($import in $imports) \n" + "${import}\n" + "#end\n" + "\n" +
			"#foreach($constant in $constants) \n" + "static private final ${constant}\n" +
			"#end" + "\n" + "public class ${className} implements CompiledModel {\n" + "\n" +
			"	public Object execute(Map<String, Object> nameToValue) {\n" +
			"		String whatIdo = null;\n" +
			"		Double temperature = (Double)nameToValue.get(\"temperature\");\n" +
			"		Double humidity = (Double)nameToValue.get(\"humidity\");\n" +
			"		String windy = (String)nameToValue.get(\"windy\");\n" +
			"		String outlook = (String)nameToValue.get(\"outlook\");\n" + "		\n" +
			"		${modelCode}\n" + "		\n" + "		return whatIdo;\n" + "	}\n" +
			"	String resultExplanation = null;\n" +
			" 	public String getResultExplanation() {\n" +
			" 		return resultExplanation;\n" + "	}\n" + "}\n";

	private static final String GOLF_MODEL_TEMPLATE_MODIFIED_NAME = "" +
			"package com.turn.tpmml.itest;\n" + "import java.util.Map;\n" +
			"import com.turn.tpmml.itest.BaseModelTest.CompiledModel;\n" + "" +
			"#foreach($import in $imports) \n" + "${import}\n" + "#end\n" + "\n" +
			"#foreach($constant in $constants) \n" + "static private final ${constant}\n" +
			"#end" + "\n" + "public class ${className} implements CompiledModel {\n" + "\n" +
			"	public Object execute(Map<String, Object> nameToValue) {\n" +
			"		String whatIdo = null;\n" +
			"		Double p_temperature = (Double)nameToValue.get(\"temperature\");\n" +
			"		Double p_humidity = (Double)nameToValue.get(\"humidity\");\n" +
			"		String p_windy = (String)nameToValue.get(\"windy\");\n" +
			"		String p_outlook = (String)nameToValue.get(\"outlook\");\n" + "		\n" +
			"		${modelCode}\n" + "		\n" + "		return whatIdo;\n" + "	}\n" +
			"	String resultExplanation = null;\n" +
			" 	public String getResultExplanation() {\n" +
			" 		return resultExplanation;\n" + "	}\n" + "}\n";
	 
	public static class GolfModelLastPrediction implements ManualModelImplementation {
		
		public Object execute(Map<String, Object> nameToValue) {
			String whatIdo = "will play";
			
			Double temperature = (Double) nameToValue.get("temperature");
			Double humidity = (Double) nameToValue.get("humidity");
			String windy = (String) nameToValue.get("windy");
			String outlook = (String) nameToValue.get("outlook");
			resultExplanation = "0";
			
			if (outlook != null && outlook.equals("sunny")) {
				whatIdo = "will play";
				resultExplanation = "1";
				if (temperature != null && temperature > 50 && temperature < 90) {
					whatIdo = "will play";
					resultExplanation = "2";
					if (humidity != null && humidity < 80) {
						whatIdo = "will play";
						resultExplanation = "3";
					} else if (humidity != null && humidity > 80) {
						whatIdo = "no play";
						resultExplanation = "4";
					}
				} else if (temperature != null && (temperature >= 90 || temperature <= 50)) {
					whatIdo = "no play";
					resultExplanation = "5";
				}
			} else if (outlook != null && (outlook.equals("overcast") || outlook.equals("rain"))) {
				whatIdo = "may play";
				resultExplanation = "6";
				if (temperature != null && temperature > 60 && temperature < 100 &&
						outlook != null && outlook.equals("overcast") && humidity != null &&
						humidity < 70 && windy != null && windy.equals("false")) {
					whatIdo = "may play";
					resultExplanation = "7";
				} else if (outlook != null && outlook.equals("rain") && humidity != null &&
						humidity < 70) {
					whatIdo = "no play";
					resultExplanation = "8";
				}
			}
			
			return whatIdo;
		}
		
		String resultExplanation;
		
		public String getResultExplanation() {
			return resultExplanation;
		}
		
	}
	

	public static class GolfModel implements ManualModelImplementation {

		public Object execute(Map<String, Object> nameToValue) {
			String whatIdo = "will play";

			Double temperature = (Double) nameToValue.get("temperature");
			Double humidity = (Double) nameToValue.get("humidity");
			String windy = (String) nameToValue.get("windy");
			String outlook = (String) nameToValue.get("outlook");

			if (outlook != null && outlook.equals("sunny")) {
				whatIdo = "will play";
				resultExplanation = "1";
				if (temperature != null && temperature > 50 && temperature < 90) {
					whatIdo = "will play";
					resultExplanation = "2";
					if (humidity != null && humidity < 80) {
						whatIdo = "will play";
						resultExplanation = "3";
					} else if (humidity != null && humidity > 80) {
						whatIdo = "no play";
						resultExplanation = "4";
					} else {
						resultExplanation = null;
						whatIdo = null;
					}
				} else if (temperature != null && (temperature >= 90 || temperature <= 50)) {
					whatIdo = "no play";
					resultExplanation = "5";
				} else {
					resultExplanation = null;
					whatIdo = null;
				}
			} else if (outlook != null && (outlook.equals("overcast") || outlook.equals("rain"))) {
				whatIdo = "may play";
				resultExplanation = "6";
				if (temperature != null && temperature > 60 && temperature < 100 &&
						outlook != null && outlook.equals("overcast") && humidity != null &&
						humidity < 70 && windy != null && windy.equals("false")) {
					whatIdo = "may play";
					resultExplanation = "7";
				} else if (outlook != null && outlook.equals("rain") && humidity != null &&
						humidity < 70) {
					whatIdo = "no play";
					resultExplanation = "8";
				} else {
					resultExplanation = null;
					whatIdo = null;
				}
			} else {
				resultExplanation = null;
				whatIdo = null;
			}

			return whatIdo;
		}

		String resultExplanation = null;

		public String getResultExplanation() {
			return resultExplanation;
		}
	}

}
