package com.graphecomplexite.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class MedianTimePlotter extends ApplicationFrame {
    public MedianTimePlotter(String applicationTitle, String chartTitle, Map<Integer, Double> data) {
        super(applicationTitle);
        JFreeChart lineChart = ChartFactory.createScatterPlot(
                chartTitle,
                "Median Computational Time Cost", "Ration of clauses to variables",
                createDataset(data),
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 800));
        setContentPane(chartPanel);
    }

    private XYSeriesCollection createDataset(Map<Integer, Double> data) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        XYSeries series1 = new XYSeries(50);

        for(Entry<Integer, Double> entry : data.entrySet()) {
            series1.add(entry.getKey(), entry.getValue());
        }
        dataset.addSeries(series1);
        return dataset;
    }

    public static void main(String[] args) {
        Map<Integer, Double> data = new HashMap<>();
        data.put(50, 50.0);
        data.put(100, 60.0);
        data.put(70, 70.0);
        MedianTimePlotter chart = new MedianTimePlotter("TEST", "TESTChart", data);

        chart.pack();
        chart.setVisible(true);
    }
}