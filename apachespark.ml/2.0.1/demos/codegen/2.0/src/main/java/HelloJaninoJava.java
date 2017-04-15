import java.lang.reflect.InvocationTargetException;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.*;

public class HelloJaninoJava {

    public static void
    main(String[] args) throws CompileException, NumberFormatException, InvocationTargetException {
    	ExpressionEvaluator ee = new ExpressionEvaluator();
    	ee.cook("3 + 4");
    	System.out.println(ee.evaluate(null));
    }
}