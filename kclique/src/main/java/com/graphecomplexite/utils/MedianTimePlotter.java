package com.graphecomplexite.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import org.javatuples.Pair;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class MedianTimePlotter extends ApplicationFrame {
    public MedianTimePlotter(String applicationTitle, String chartTitle, Map<Pair<Integer, Integer>, Double> data) {
        super(applicationTitle);
        JFreeChart scatterChart = ChartFactory.createScatterPlot(
                chartTitle,
                "Number of Nodes", "Computational Time",
                createDataset(data),
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel(scatterChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 800));
        chartPanel.setLocation(480, 270);
        setContentPane(chartPanel);
    }

    private XYSeriesCollection createDataset(Map<Pair<Integer, Integer>, Double> data) {
        XYSeriesCollection dataset = new XYSeriesCollection();

        List<XYSeries> series = new ArrayList<>();
        Set<Integer> kValues = new HashSet<>();

        for(Pair<Integer, Integer> key : data.keySet()) {
            kValues.add(key.getValue1());
        }

        for(Integer kVal : kValues) {
            series.add(new XYSeries(kVal));
        }


        for(Entry<Pair<Integer, Integer>, Double> entry : data.entrySet()) {
            for(XYSeries serie : series) {
                if(serie.getKey().equals(entry.getKey().getValue1())) {
                    serie.add(entry.getKey().getValue0(), entry.getValue());
                    break;
                }
            }
        }

        for(XYSeries serie : series) {
            dataset.addSeries(serie);
        }
        return dataset;
    }

    public static void main(String[] args) {
        Map<Pair<Integer, Integer>, Double> data = new HashMap<>();
        data.put(new Pair<Integer, Integer>(10, 5), 50.0);
        data.put(new Pair<Integer, Integer>(1, 5), 60.0);
        data.put(new Pair<Integer, Integer>(1, 7), 70.0);
        MedianTimePlotter chart = new MedianTimePlotter("TEST", "TESTChart", data);

        chart.pack();
        chart.setVisible(true);
    }
}