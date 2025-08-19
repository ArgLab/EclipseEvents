package edu.runtimeanalysis.models;

import edu.runtimeanalysis.models.MethodSignature;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MethodSignatureTest {

    @Test
    void testConstructorWithValidParameters() {
        // Test constructor with method that has parameters
    	
    	String param1 = "ValidName";
    	Class[] param2 = new Class[1];
    	param2[0] = Integer.class;
    	Class param3 = Integer.class;
    	String param4 = "int ValidName(int a)";
    	
    	MethodSignature s = new MethodSignature(param1, param2, param3, param4);
    	
    	assertNotNull(s);
    	
    	assertEquals(param1, s.getMethodName());
    	Class[] x = s.getParameterTypes();
    	assertEquals(x.length, param2.length);
    	for(int i = 0; i < x.length; i++) {
    		assertEquals(x[i], param2[i]);
    	}
    	assertEquals(param3, s.getReturnType());
    	assertEquals(param4, s.getDisplaySignature());
    }

    @Test
    void testInvalidParameters() {
    	fail();
    }
}