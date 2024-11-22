package com.graphecomplexite;

import com.graphecomplexite.benchmarker.Benchmarker;

public class MainCompareOpt {
    public static void main(String[] args) {

        String modelFzn = "kclique/data/dimacs_fzn_instance/C125.9_3.fzn";

        Benchmarker b = new Benchmarker();
        b.compareSolver(modelFzn);
    }
}
