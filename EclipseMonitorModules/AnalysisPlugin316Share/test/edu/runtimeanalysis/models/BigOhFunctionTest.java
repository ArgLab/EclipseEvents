package edu.runtimeanalysis.models;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for BigOhFunction enum
 */
public class BigOhFunctionTest {

    @Test
    public void testLinearFunction() {
        assertEquals(100.0, BigOhFunction.LINEAR.calculate(100), 0.01);
        assertEquals(1000.0, BigOhFunction.LINEAR.calculate(1000), 0.01);
    }

    @Test
    public void testQuadraticFunction() {
        assertEquals(10000.0, BigOhFunction.QUADRATIC.calculate(100), 0.01);
        assertEquals(1000000.0, BigOhFunction.QUADRATIC.calculate(1000), 0.01);
    }

    @Test
    public void testLogarithmicFunction() {
        double result100 = BigOhFunction.LOGARITHMIC.calculate(100);
        double result1000 = BigOhFunction.LOGARITHMIC.calculate(1000);
        
        assertTrue(result100 > 0);
        assertTrue(result1000 > result100);
        assertTrue(result1000 < result100 * 2); // Should be less than 2x due to log growth
    }

    @Test
    public void testLinearithmicFunction() {
        double result100 = BigOhFunction.LINEARITHMIC.calculate(100);
        double result1000 = BigOhFunction.LINEARITHMIC.calculate(1000);
        
        assertTrue(result100 > BigOhFunction.LINEAR.calculate(100));
        assertTrue(result100 < BigOhFunction.QUADRATIC.calculate(100));
        assertTrue(result1000 > result100);
    }

    @Test
    public void testExponentialFunction() {
        double result10 = BigOhFunction.EXPONENTIAL.calculate(10);
        double result15 = BigOhFunction.EXPONENTIAL.calculate(15);
        
        assertTrue(result10 > 0);
        assertTrue(result15 > result10 * 10); // Should grow very fast
    }

    @Test
    public void testZeroInput() {
        assertEquals(1.0, BigOhFunction.LINEAR.calculate(0), 0.01);
        assertEquals(1.0, BigOhFunction.QUADRATIC.calculate(0), 0.01);
        assertEquals(1.0, BigOhFunction.LOGARITHMIC.calculate(0), 0.01);
    }

    @Test
    public void testNegativeInput() {
        assertEquals(1.0, BigOhFunction.LINEAR.calculate(-5), 0.01);
        assertEquals(1.0, BigOhFunction.QUADRATIC.calculate(-5), 0.01);
    }

    @Test
    public void testNotationAndDescription() {
        assertEquals("O(n)", BigOhFunction.LINEAR.getNotation());
        assertEquals("Linear", BigOhFunction.LINEAR.getDescription());
        
        assertEquals("O(n²)", BigOhFunction.QUADRATIC.getNotation());
        assertEquals("Quadratic", BigOhFunction.QUADRATIC.getDescription());
        
        assertEquals("O(n log n)", BigOhFunction.LINEARITHMIC.getNotation());
        assertEquals("Linearithmic", BigOhFunction.LINEARITHMIC.getDescription());
    }

    @Test
    public void testAllFunctionsReturnPositiveValues() {
        for (BigOhFunction function : BigOhFunction.values()) {
            assertTrue(function.calculate(100) > 0, 
                "Function " + function.getNotation() + " should return positive value");
            assertTrue(function.calculate(1000) > 0, 
                "Function " + function.getNotation() + " should return positive value");
        }
    }

    @Test
    public void testGrowthRates() {
        double n = 1000;
        
        // Test that growth rates are in expected order
        double log = BigOhFunction.LOGARITHMIC.calculate(n);
        double sqrt = BigOhFunction.SQRT.calculate(n);
        double linear = BigOhFunction.LINEAR.calculate(n);
        double nlogn = BigOhFunction.LINEARITHMIC.calculate(n);
        double quadratic = BigOhFunction.QUADRATIC.calculate(n);
        double cubic = BigOhFunction.CUBIC.calculate(n);
        
        assertTrue(log < sqrt, "log(n) should be less than sqrt(n)");
        assertTrue(sqrt < linear, "sqrt(n) should be less than n");
        assertTrue(linear < nlogn, "n should be less than n*log(n)");
        assertTrue(nlogn < quadratic, "n*log(n) should be less than n²");
        assertTrue(quadratic < cubic, "n² should be less than n³");
        
        // Exponential grows much faster but we cap it, so just check it's reasonable
        double exponential = BigOhFunction.EXPONENTIAL.calculate(10); // Use smaller n
        assertTrue(exponential > 1000, "2^10 should be greater than 1000");
    }
}