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

    public SolverComparatorPlotter(String applicationTitle, String chartTitle, Map<String, Map<String, Double>> data) {
        super(applicationTitle);
        
        DefaultCategoryDataset dataset = createDataset(data);
        
        JFreeChart barChart = ChartFactory.createBarChart(
                chartTitle,
                "Configurations",          
                "Temps (secondes)",              
                dataset,                  
                PlotOrientation.VERTICAL,
                true,                      // Display legend for series (clique sizes)
                true,                     
                false
        );
        
        customizeChart(barChart, dataset);
        
        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        setContentPane(chartPanel);
    }

    private DefaultCategoryDataset createDataset(Map<String, Map<String, Double>> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        data.forEach((cliqueSize, solversData) -> 
            solversData.forEach((solverConfig, solvingTime) -> 
                dataset.addValue(solvingTime, cliqueSize, solverConfig)
            )
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
            this.colors = generateColors(dataset.getRowCount());
        }

        @Override
        public Paint getItemPaint(int row, int column) {
            return colors[row]; // Color based on series (clique size)
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
        Map<String, Map<String, Double>> exampleData = Map.of(
                "Clique Size 3", Map.of(
                        "Solver A - Param1", 120.5,
                        "Solver B - Param1", 150.2,
                        "Solver A - Param2", 110.3,
                        "Solver B - Param2", 140.8
                ),
                "Clique Size 5", Map.of(
                        "Solver A - Param1", 200.7,
                        "Solver B - Param1", 250.4,
                        "Solver A - Param2", 180.9,
                        "Solver B - Param2", 230.1
                ),
                "Clique Size 7", Map.of(
                        "Solver A - Param1", 320.6,
                        "Solver B - Param1", 350.8,
                        "Solver A - Param2", 310.2,
                        "Solver B - Param2", 340.0
                )
        );

        SolverComparatorPlotter chart = new SolverComparatorPlotter(
                "Comparaison des Solveurs", 
                "Performance des Solveurs selon la Taille de la Clique", 
                exampleData
        );

        chart.pack();
        chart.setVisible(true);
    }
}
