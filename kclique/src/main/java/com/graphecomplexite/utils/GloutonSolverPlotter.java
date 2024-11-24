package com.graphecomplexite.utils;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.xy.DefaultXYZDataset;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Map;

import javax.swing.SwingUtilities;

public class GloutonSolverPlotter extends ApplicationFrame {

    public GloutonSolverPlotter(String title, Map<String, Map<String, Integer>> data) {
        super(title);
        
        DefaultXYZDataset dataset = createDataset(data);
        JFreeChart chart = createHeatmapChart(dataset, data);
        
        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(800, 600));
        setContentPane(panel);
    }

    private DefaultXYZDataset createDataset(Map<String, Map<String, Integer>> data) {
        DefaultXYZDataset dataset = new DefaultXYZDataset();

        int xSize = data.size();
        int ySize = data.values().iterator().next().size();

        double[] xValues = new double[xSize * ySize];
        double[] yValues = new double[xSize * ySize];
        double[] zValues = new double[xSize * ySize];

        int index = 0;
        int xIndex = 0;

        for (Map.Entry<String, Map<String, Integer>> entry : data.entrySet()) {
            int yIndex = 0;
            for (Map.Entry<String, Integer> solverEntry : entry.getValue().entrySet()) {
                xValues[index] = xIndex;
                yValues[index] = yIndex;
                zValues[index] = solverEntry.getValue();
                index++;
                yIndex++;
            }
            xIndex++;
        }

        dataset.addSeries("Heatmap", new double[][] { xValues, yValues, zValues });
        return dataset;
    }

    private JFreeChart createHeatmapChart(DefaultXYZDataset dataset, Map<String, Map<String, Integer>> data) {
        String[] xLabels = data.keySet().toArray(new String[0]);
        String[] yLabels = data.values().iterator().next().keySet().toArray(new String[0]);

        SymbolAxis xAxis = new SymbolAxis("Configurations", xLabels);
        SymbolAxis yAxis = new SymbolAxis("Solveurs", yLabels);

        XYBlockRenderer renderer = new XYBlockRenderer();
        PaintScale paintScale = new GradientPaintScale(0, 100, Color.BLUE, Color.RED);
        renderer.setPaintScale(paintScale);

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        plot.setBackgroundPaint(Color.WHITE);

        // addAnnotations(plot, data);

        return new JFreeChart("Comparaison des Algorithmes Gloutons", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
    }

    private void addAnnotations(XYPlot plot, Map<String, Map<String, Integer>> data) {
        int xIndex = 0;
        for (Map.Entry<String, Map<String, Integer>> entry : data.entrySet()) {
            int yIndex = 0;
            for (Map.Entry<String, Integer> solverEntry : entry.getValue().entrySet()) {
                double x = xIndex;
                double y = yIndex;
                String label = String.valueOf(solverEntry.getValue());

                XYTextAnnotation annotation = new XYTextAnnotation(label, x, y);
                annotation.setFont(new Font("SansSerif", Font.BOLD, 12));
                annotation.setPaint(Color.BLACK);
                annotation.setTextAnchor(TextAnchor.BASELINE_CENTER);

                plot.addAnnotation(annotation);
                yIndex++;
            }
            xIndex++;
        }
    }


    public static void main(String[] args) {
        Map<String, Map<String, Integer>> exampleData = Map.of(
                "Configuration 1", Map.of(
                        "Glouton A", 45,
                        "Glouton B", 30,
                        "Glouton C", 60
                ),
                "Configuration 2", Map.of(
                        "Glouton A", 25,
                        "Glouton B", 20,
                        "Glouton C", 35
                ),
                "Configuration 3", Map.of(
                        "Glouton A", 55,
                        "Glouton B", 40,
                        "Glouton C", 70
                )
        );

        SwingUtilities.invokeLater(() -> {
            GloutonSolverPlotter plotter = new GloutonSolverPlotter("Heatmap des Algorithmes Gloutons", exampleData);
            plotter.pack();
            plotter.setVisible(true);
        });
    }
}
