package com.turn.tpmml.translator;

import com.turn.tpmml.PMML;
import com.turn.tpmml.manager.ModelManager;
import com.turn.tpmml.manager.PMMLManager;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Convert PMML model to java class
 *
 * @author asvirsky
 *
 */
@SuppressWarnings("restriction")
public class PmmlToJavaTranslator {
	private static final Logger logger = LoggerFactory.getLogger(PmmlToJavaTranslator.class);

	private PmmlToJavaTranslator() {
		// Forbid the creation of an instance.
	}
	
	public static String generateJavaCode(PMML pmml, String className, Reader templateReader,
			TranslationContext context) throws Exception {

		if (className == null || className.isEmpty()) {
			logger.error("You need to provide unique class name");
			return null;
		}

		PMMLManager pmmlManager = new PMMLManager(pmml);

		// get translator
		Translator translator = (Translator) pmmlManager.getModelManager(null,
				ModelTranslatorFactory.getInstance());

		String result = null;
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("className", className);
		parameters.put("activeVariables", translator.getActiveFields());
		// parameters.put("outputVariable", context.getOutputVariableName());
		// parameters.put("predictedVariables", translator.getPredictedFields());

		parameters.put("hookBeforeTranslation",
                   context.beforeTranslation(pmml, (ModelManager<?>) translator));
		parameters.put("hookAfterTranslation",
                   context.afterTranslation(pmml, (ModelManager<?>) translator));
		parameters.put("extraVariables",
                   context.declareExtraVariables(pmml, (ModelManager<?>) translator));
    parameters.put("modelCode", translator.translate(context));
		parameters.put("constants", context.getConstantDeclarations());
		parameters.put("imports", context.getRequiredImports());

		VelocityEngine velocity = new VelocityEngine();
		VelocityContext vc = new VelocityContext(parameters);
		StringWriter writer = new StringWriter();

		if (velocity.evaluate(vc, writer, className, templateReader)) {
			result = writer.toString();
		}

		return result;
	}

	public static Class<?> createModelClass(String className, String packageName, String javaSource)
			throws Exception {
		List<String> compilerOptions = new ArrayList<String>();
		compilerOptions.add("-cp");
		compilerOptions.add(System.getProperty("java.class.path"));

		return createModelClass(className, packageName, javaSource, compilerOptions);
	}

	public static Class<?> createModelClass(String className, String packageName,
			String javaSource, List<String> compilerOptions) throws Exception {
		Class<?> result = null;

		// make a copy
		compilerOptions = compilerOptions != null ? new ArrayList<String>(compilerOptions) :
				new ArrayList<String>();
		// compile class into temp folder
		String tempPath = System.getProperty("java.io.tmpdir");
		compilerOptions.add("-d");
		compilerOptions.add(tempPath);
		// compilerOptions.add("-g");

		if (packageName != null) {
			// create corresponding folders
			String dirs = packageName.replaceAll("\\.", File.separator);
			File classPath = new File(tempPath, dirs);
			classPath.mkdirs();
		}

		String classFullName = packageName != null ? packageName + "." + className : className;

		JavaFileObject javaObject = new RAMResidentJavaFileObject(classFullName, javaSource);

		StringWriter writer = new StringWriter();
		boolean status = compile(writer, compilerOptions, javaObject);

		// Load class and create an instance.
		if (status) {
			File classPathFile = new File(tempPath);
			URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { classPathFile
					.toURI().toURL() });
			result = Class.forName(classFullName, true, classLoader);
		} else {
			logger.error("Failed to compile " + classFullName + " for source:\n" + javaSource);
			logger.error(writer.toString());
		}

		return result;
	}

	/**
	 * Compile from within this JVM without spawning javac.exe or a separate JVM.
	 * 
	 * @param source points to source, possibly in RAM.
	 * @return status of the compile, true all went perfectly without error.
	 * @throws java.io.IOException if trouble writing class files.
	 */
	private static boolean compile(Writer writer, List<String> compilerOptions,
			JavaFileObject... source) {

		final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		if (compiler == null) {
			// logger.error("No JavaCompiler available - please install JDK (not JRE) environment");
			return false;
		}

		final JavaCompiler.CompilationTask task = compiler.getTask(
		// System.err if writer is null
				writer, null, // standard file manager, If we wrote our own we could
								// control the location of the generated class files
				null, // standard DiagnosticListener
				compilerOptions, // options
				null, // no annotation classes
				Arrays.asList(source) // source code, we must convert
										// JavaFileObject... to Iterable<?>
										// extends JavaFileObject>
				);

		return task.call();
	}

	private static class RAMResidentJavaFileObject extends SimpleJavaFileObject {

		private final String programText;

		/**
		 * constructor
		 * 
		 * @param className class name, without package
		 * @param programText text of the program.
		 * @throws java.net.URISyntaxException if malformed class name.
		 */
		public RAMResidentJavaFileObject(String className, String programText) {
			super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension),
					Kind.SOURCE);
			this.programText = programText;
		}

		/**
		 * Get the text of the java program
		 * 
		 * @param ignoreEncodingErrors ignored.
		 */
		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
			return programText;
		}
	}

}
