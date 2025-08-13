package com.arglab.eclipsedatacollector.CSC316Graph.ui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CSC316GraphView  extends ViewPart{

	public static final String ID = "com.arglab.eclipsedatacollector.CSC316Graph.ui.CSC316Graph";

	private Canvas canvas;
    private List<DataPoint> dataPoints = new ArrayList<>();
    
    class DataPoint {
        double x, y;
        String label;
        
        public DataPoint(double x, double y, String label) {
            this.x = x;
            this.y = y;
            this.label = label;
        }
    }
    
    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout());
        
        canvas = new Canvas(parent, SWT.BORDER);
        
        canvas.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                drawGraph(e.gc);
            }
        });
        
        loadDataFromJSON();
        canvas.redraw();
    }
    
    private void loadDataFromJSON() {
        try {
            InputStream is = getClass().getResourceAsStream("GraphData.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder jsonContent = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            
            JsonElement rootElement = JsonParser.parseString(jsonContent.toString());
            
            if (rootElement.isJsonObject()) {
                JsonObject jsonObject = rootElement.getAsJsonObject();
                
                if (jsonObject.has("points")) {
                    JsonArray pointsArray = jsonObject.getAsJsonArray("points");
                    
                    for (JsonElement pointElement : pointsArray) {
                        if (pointElement.isJsonObject()) {
                            JsonObject pointObj = pointElement.getAsJsonObject();
                            
                            double x = pointObj.get("x").getAsDouble();
                            double y = pointObj.get("y").getAsDouble();
                            String label = pointObj.has("label") ? pointObj.get("label").getAsString() : "";
                            
                            dataPoints.add(new DataPoint(x, y, label));
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void drawGraph(GC gc) {
        if (dataPoints.isEmpty()) return;
        
        Point canvasSize = canvas.getSize();
        int width = canvasSize.x;
        int height = canvasSize.y;
        
        int margin = 50;
        int graphWidth = width - 2 * margin;
        int graphHeight = height - 2 * margin;
        
        double minX = dataPoints.stream().mapToDouble(p -> p.x).min().orElse(0);
        double maxX = dataPoints.stream().mapToDouble(p -> p.x).max().orElse(100);
        double minY = dataPoints.stream().mapToDouble(p -> p.y).min().orElse(0);
        double maxY = dataPoints.stream().mapToDouble(p -> p.y).max().orElse(100);
        
        double xRange = maxX - minX;
        double yRange = maxY - minY;
        minX -= xRange * 0.1;
        maxX += xRange * 0.1;
        minY -= yRange * 0.1;
        maxY += yRange * 0.1;
        
        Display display = Display.getCurrent();
        Color backgroundColor = display.getSystemColor(SWT.COLOR_WHITE);
        Color axisColor = display.getSystemColor(SWT.COLOR_BLACK);
        Color pointColor = display.getSystemColor(SWT.COLOR_BLUE);
        Color lineColor = display.getSystemColor(SWT.COLOR_RED);
        
        gc.setBackground(backgroundColor);
        gc.fillRectangle(0, 0, width, height);
        
        gc.setForeground(axisColor);
        gc.setLineWidth(2);
        

        gc.drawLine(margin, height - margin, width - margin, height - margin);
        gc.drawLine(margin, margin, margin, height - margin);
        
        gc.setLineStyle(SWT.LINE_DOT);
        gc.setLineWidth(1);
        
        for (int i = 1; i < 10; i++) {
            int x = margin + (graphWidth * i / 10);
            gc.drawLine(x, margin, x, height - margin);
        }
        
        for (int i = 1; i < 10; i++) {
            int y = margin + (graphHeight * i / 10);
            gc.drawLine(margin, y, width - margin, y);
        }
        
        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setLineWidth(2);
        
        gc.setForeground(lineColor);
        for (int i = 0; i < dataPoints.size() - 1; i++) {
            DataPoint p1 = dataPoints.get(i);
            DataPoint p2 = dataPoints.get(i + 1);
            
            int x1 = margin + (int)((p1.x - minX) / (maxX - minX) * graphWidth);
            int y1 = height - margin - (int)((p1.y - minY) / (maxY - minY) * graphHeight);
            int x2 = margin + (int)((p2.x - minX) / (maxX - minX) * graphWidth);
            int y2 = height - margin - (int)((p2.y - minY) / (maxY - minY) * graphHeight);
            
            gc.drawLine(x1, y1, x2, y2);
        }
        
        gc.setBackground(pointColor);
        gc.setForeground(pointColor);
        
        for (DataPoint point : dataPoints) {
            int x = margin + (int)((point.x - minX) / (maxX - minX) * graphWidth);
            int y = height - margin - (int)((point.y - minY) / (maxY - minY) * graphHeight);
            
            gc.fillOval(x - 4, y - 4, 8, 8);
            
            if (point.label != null && !point.label.isEmpty()) {
                gc.setForeground(axisColor);
                gc.drawText(point.label, x + 8, y - 8, true);
                gc.setForeground(pointColor);
            }
        }
        
        gc.setForeground(axisColor);
        Font font = new Font(display, "Arial", 10, SWT.BOLD);
        gc.setFont(font);
        
        gc.drawText("X-Axis", width/2 - 20, height - 20, true);
        
        gc.drawText("Y-Axis", 10, height/2, true);
        
        Font smallFont = new Font(display, "Arial", 8, SWT.NORMAL);
        gc.setFont(smallFont);
        
        for (int i = 0; i <= 5; i++) {
            double value = minX + (maxX - minX) * i / 5;
            int x = margin + (graphWidth * i / 5);
            gc.drawText(String.format("%.1f", value), x - 10, height - margin + 5, true);
        }
        
        for (int i = 0; i <= 5; i++) {
            double value = minY + (maxY - minY) * i / 5;
            int y = height - margin - (graphHeight * i / 5);
            gc.drawText(String.format("%.1f", value), 5, y - 8, true);
        }
        
        font.dispose();
        smallFont.dispose();
    }
    
    @Override
    public void setFocus() {
        canvas.setFocus();
    }
    
    public void refreshGraph() {
        dataPoints.clear();
        loadDataFromJSON();
        canvas.redraw();
    }
}