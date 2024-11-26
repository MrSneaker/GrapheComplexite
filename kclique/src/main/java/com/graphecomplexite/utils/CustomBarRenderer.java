package com.graphecomplexite.utils;

import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class CustomBarRenderer extends BarRenderer {
    private final Color[] colors;

    public CustomBarRenderer(DefaultCategoryDataset dataset) {
        this.colors = generateColors(dataset.getRowCount());
    }

    @Override
    public Paint getItemPaint(int row, int column) {
        return colors[row];
    }

    private Color[] generateColors(int numberOfColors) {
        Color[] colors = new Color[numberOfColors];
        for (int i = 0; i < numberOfColors; i++) {
            colors[i] = Color.getHSBColor((float) i / numberOfColors, 0.8f, 0.8f);
        }
        return colors;
    }
}
