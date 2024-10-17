package com.graphecomplexite.utils;

import java.util.List;

public class GraphData {
    private int numberOfNodes;
    private int k;
    private List<int[]> edges;
    private List<String> constraints;

    public GraphData(int numberOfNodes, int k, List<int[]> edges, List<String> constraints) {
        this.numberOfNodes = numberOfNodes;
        this.k = k;
        this.edges = edges;
        this.constraints = constraints;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public int getK() {
        return k;
    }

    public List<int[]> getEdges() {
        return edges;
    }

    public List<String> getConstraints() {
        return constraints;
    }
}
