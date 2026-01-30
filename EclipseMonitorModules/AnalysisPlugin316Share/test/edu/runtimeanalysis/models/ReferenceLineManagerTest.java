package edu.runtimeanalysis.models;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for ReferenceLineManager
 */
public class ReferenceLineManagerTest {

    private ReferenceLineManager manager;
    private XYSeriesCollection dataset;
    private XYPlot plot;

    @BeforeEach
    public void setUp() {
        dataset = new XYSeriesCollection();
        plot = new XYPlot();
        plot.setRenderer(new XYLineAndShapeRenderer());
        manager = new ReferenceLineManager(dataset, plot);
    }

    @Test
    public void testEnableFunction() {
        assertEquals(0, dataset.getSeriesCount());
        
        manager.enableFunction(BigOhFunction.LINEAR);
        assertTrue(manager.isFunctionEnabled(BigOhFunction.LINEAR));
        assertEquals(1, dataset.getSeriesCount());
        assertEquals("O(n)", dataset.getSeriesKey(0));
    }

    @Test
    public void testDisableFunction() {
        manager.enableFunction(BigOhFunction.LINEAR);
        assertTrue(manager.isFunctionEnabled(BigOhFunction.LINEAR));
        assertEquals(1, dataset.getSeriesCount());
        
        manager.disableFunction(BigOhFunction.LINEAR);
        assertFalse(manager.isFunctionEnabled(BigOhFunction.LINEAR));
        assertEquals(0, dataset.getSeriesCount());
    }

    @Test
    public void testMultipleFunctions() {
        manager.enableFunction(BigOhFunction.LINEAR);
        manager.enableFunction(BigOhFunction.QUADRATIC);
        
        assertEquals(2, dataset.getSeriesCount());
        assertTrue(manager.isFunctionEnabled(BigOhFunction.LINEAR));
        assertTrue(manager.isFunctionEnabled(BigOhFunction.QUADRATIC));
        assertFalse(manager.isFunctionEnabled(BigOhFunction.CUBIC));
    }

    @Test
    public void testClearAll() {
        manager.enableFunction(BigOhFunction.LINEAR);
        manager.enableFunction(BigOhFunction.QUADRATIC);
        assertEquals(2, dataset.getSeriesCount());
        
        manager.clearAll();
        assertEquals(0, dataset.getSeriesCount());
        assertFalse(manager.isFunctionEnabled(BigOhFunction.LINEAR));
        assertFalse(manager.isFunctionEnabled(BigOhFunction.QUADRATIC));
    }

    @Test
    public void testUpdateScaling() {
        List<Double> actualTimes = Arrays.asList(10.0, 40.0, 90.0, 160.0);
        List<Integer> inputSizes = Arrays.asList(100, 200, 300, 400);
        
        manager.enableFunction(BigOhFunction.LINEAR);
        int initialPoints = dataset.getSeries(0).getItemCount();
        
        manager.updateScaling(actualTimes, inputSizes);
        
        // Should still have the same series but potentially different scaling
        assertEquals(1, dataset.getSeriesCount());
        assertTrue(dataset.getSeries(0).getItemCount() > 0);
    }

    @Test
    public void testSetInputSizeRange() {
        manager.enableFunction(BigOhFunction.LINEAR);
        int initialPoints = dataset.getSeries(0).getItemCount();
        
        manager.setInputSizeRange(100, 1000);
        
        // Should regenerate the series with new range
        assertEquals(1, dataset.getSeriesCount());
        assertTrue(dataset.getSeries(0).getItemCount() > 0);
    }

    @Test
    public void testGetEnabledFunctions() {
        assertTrue(manager.getEnabledFunctions().isEmpty());
        
        manager.enableFunction(BigOhFunction.LINEAR);
        manager.enableFunction(BigOhFunction.QUADRATIC);
        
        assertEquals(2, manager.getEnabledFunctions().size());
        assertTrue(manager.getEnabledFunctions().contains(BigOhFunction.LINEAR));
        assertTrue(manager.getEnabledFunctions().contains(BigOhFunction.QUADRATIC));
    }

    @Test
    public void testEmptyActualTimesHandling() {
        manager.enableFunction(BigOhFunction.LINEAR);
        
        // Should not crash with empty lists
        manager.updateScaling(Arrays.asList(), Arrays.asList());
        
        assertEquals(1, dataset.getSeriesCount());
    }
}