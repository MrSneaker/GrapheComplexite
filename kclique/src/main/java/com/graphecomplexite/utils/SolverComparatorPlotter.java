package com.graphecomplexite.utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ui.ApplicationFrame;
import java.awt.Paint;
import java.awt.Color;
import java.util.Map;

public class SolverComparatorPlotter extends ApplicationFrame {

    public SolverComparatorPlotter(String applicationTitle, String chartTitle, Map<String, Double> data) {
        super(applicationTitle);
        
        DefaultCategoryDataset dataset = createDataset(data);
        
        JFreeChart barChart = ChartFactory.createBarChart(
                chartTitle,
                "Configurations",          
                "Temps (secondes)",              
                dataset,                  
                PlotOrientation.VERTICAL,
                false,                      
                true,                     
                false
        );
        
        customizeChart(barChart, dataset);
        
        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        setContentPane(chartPanel);
    }

    private DefaultCategoryDataset createDataset(Map<String, Double> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        data.forEach((solverConfig, solvingTime) -> 
            dataset.addValue(solvingTime, "Temps de solving", solverConfig)
        );
        
        return dataset;
    }

    private void customizeChart(JFreeChart chart, DefaultCategoryDataset dataset) {
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = new CustomBarRenderer(dataset);
        
        renderer.setDrawBarOutline(false);
        
        renderer.setBarPainter(new StandardBarPainter());
        
        plot.setRenderer(renderer);
    }

    private static class CustomBarRenderer extends BarRenderer {
        private final Color[] colors;

        public CustomBarRenderer(DefaultCategoryDataset dataset) {
            this.colors = generateColors(dataset.getColumnCount());
        }

        @Override
        public Paint getItemPaint(int row, int column) {
            return colors[column];
        }

        private Color[] generateColors(int numberOfColors) {
            Color[] colors = new Color[numberOfColors];
            for (int i = 0; i < numberOfColors; i++) {
                colors[i] = Color.getHSBColor((float) i / numberOfColors, 0.8f, 0.8f);
            }
            return colors;
        }
    }

    public static void main(String[] args) {
        Map<String, Double> exampleData = Map.of(
                "Solver A - Param1", 120.5,
                "Solver B - Param1", 150.2,
                "Solver A - Param2", 110.3,
                "Solver B - Param2", 140.8
        );

        SolverComparatorPlotter chart = new SolverComparatorPlotter(
                "Comparaison des Solveurs", 
                "Performance des Solveurs", 
                exampleData
        );

        chart.pack();
        chart.setVisible(true);
    }
}
