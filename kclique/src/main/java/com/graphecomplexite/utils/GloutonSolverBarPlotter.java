package com.graphecomplexite.utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.util.Map;
import java.util.HashMap;

public class GloutonSolverBarPlotter extends ApplicationFrame {

    public GloutonSolverBarPlotter(String title, Map<String, Map<String, Integer>> data) {
        super(title);

        DefaultCategoryDataset dataset = createDataset(data);
        JFreeChart chart = ChartFactory.createBarChart(
                "Pourcentage de succès par solveur",
                "Solveurs",
                "Réussite (%)",
                dataset);

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        CustomBarRenderer renderer = new CustomBarRenderer(dataset);
        renderer.setDrawBarOutline(false);
        renderer.setBarPainter(new StandardBarPainter());
        plot.setRenderer(renderer);

        assignSeriesColorsToPlot(plot, renderer);

        ChartPanel panel = new ChartPanel(chart);
        panel.setPreferredSize(new Dimension(800, 600));
        setContentPane(panel);
    }

    private DefaultCategoryDataset createDataset(Map<String, Map<String, Integer>> data) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Double> solverSum = new HashMap<>();
        Map<String, Integer> solverCount = new HashMap<>();

        for (Map<String, Integer> configuration : data.values()) {
            for (Map.Entry<String, Integer> entry : configuration.entrySet()) {
                String solver = entry.getKey();
                int tries = entry.getValue();

                if (tries != 10000) {
                    solverSum.put(solver, solverSum.getOrDefault(solver, 0.0) + 1 / (double) tries);
                }
                solverCount.put(solver, solverCount.getOrDefault(solver, 0) + 1);
            }
        }

        for (String solver : solverSum.keySet()) {
            double success = (solverSum.get(solver) / solverCount.get(solver)) * 100.0;
            System.out.println("success for " + solver + " is " + success);
            dataset.addValue(success, solver, "");
        }

        return dataset;
    }

    private void assignSeriesColorsToPlot(CategoryPlot plot, CustomBarRenderer renderer) {
        int rowCount = plot.getDataset().getRowCount();
        for (int i = 0; i < rowCount; i++) {
            plot.getRenderer().setSeriesPaint(i, renderer.getItemPaint(i, 0));
        }
    }

    public static void main(String[] args) {
        Map<String, Map<String, Integer>> exampleData = Map.of(
                "Configuration 1", Map.of(
                        "Glouton A", 45,
                        "Glouton B", 30,
                        "Glouton C", 60),
                "Configuration 2", Map.of(
                        "Glouton A", 25,
                        "Glouton B", 20,
                        "Glouton C", 35),
                "Configuration 3", Map.of(
                        "Glouton A", 55,
                        "Glouton B", 1000,
                        "Glouton C", 70));

        SwingUtilities.invokeLater(() -> {
            GloutonSolverBarPlotter plotter = new GloutonSolverBarPlotter("Graphique des pourcentages de réussites",
                    exampleData);
            plotter.pack();
            plotter.setVisible(true);
        });
    }
}
