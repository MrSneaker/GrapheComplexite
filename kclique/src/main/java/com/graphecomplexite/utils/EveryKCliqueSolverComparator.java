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

public class EveryKCliqueSolverComparator extends ApplicationFrame {

    public EveryKCliqueSolverComparator(String title, Map<String, Map<String, Integer>> data) {
        super(title);

        DefaultCategoryDataset dataset = createDataset(data);
        JFreeChart chart = ChartFactory.createBarChart(
                "Comparaison du nombre de solutions trouvées",
                "Configurations",
                "Nombre de solutions trouvées",
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

        for (Map.Entry<String, Map<String, Integer>> entry : data.entrySet()) {
            String configuration = entry.getKey();
            Map<String, Integer> solvers = entry.getValue();

            for (Map.Entry<String, Integer> solverEntry : solvers.entrySet()) {
                String solver = solverEntry.getKey();
                int solutionsFound = solverEntry.getValue();
                dataset.addValue(solutionsFound, solver, configuration);
            }
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
                        "Solveur Glouton", 10,
                        "Solveur Complet", 15),
                "Configuration 2", Map.of(
                        "Solveur Glouton", 12,
                        "Solveur Complet", 20),
                "Configuration 3", Map.of(
                        "Solveur Glouton", 8,
                        "Solveur Complet", 25));

        SwingUtilities.invokeLater(() -> {
            EveryKCliqueSolverComparator plotter = new EveryKCliqueSolverComparator(
                    "Comparaison Solveurs Glouton et Complet", exampleData);
            plotter.pack();
            plotter.setVisible(true);
        });
    }
}
