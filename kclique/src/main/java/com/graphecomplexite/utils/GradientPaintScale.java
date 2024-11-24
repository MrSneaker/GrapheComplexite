package com.graphecomplexite.utils;

import java.awt.Color;
import java.awt.Paint;
import org.jfree.chart.renderer.PaintScale;

public class GradientPaintScale implements PaintScale {

    private final double lowerBound;
    private final double upperBound;
    private final Color lowColor;
    private final Color highColor;

    public GradientPaintScale(double lowerBound, double upperBound, Color lowColor, Color highColor) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.lowColor = lowColor;
        this.highColor = highColor;
    }

    @Override
    public double getLowerBound() {
        return lowerBound;
    }

    @Override
    public double getUpperBound() {
        return upperBound;
    }

    @Override
    public Paint getPaint(double value) {
        double range = upperBound - lowerBound;
        double normalizedValue = Math.min(Math.max((value - lowerBound) / range, 0.0), 1.0);
        
        int red = (int) (lowColor.getRed() + normalizedValue * (highColor.getRed() - lowColor.getRed()));
        int green = (int) (lowColor.getGreen() + normalizedValue * (highColor.getGreen() - lowColor.getGreen()));
        int blue = (int) (lowColor.getBlue() + normalizedValue * (highColor.getBlue() - lowColor.getBlue()));
        
        return new Color(red, green, blue);
    }
}
