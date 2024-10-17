package com.graphecomplexite;

import com.graphecomplexite.benchmarker.Benchmarker;
import com.graphecomplexite.solver.ChocoSolverFromFzn;

public class Main {
    public static void main(String[] args) {
        String projectRoot = System.getProperty("user.dir");
        String filePath = projectRoot + "/kclique/data/graph2_100_k_5.fzn";

        // ChocoSolverFromFzn chocoSolver = new ChocoSolverFromFzn(filePath);
        // chocoSolver.findSolution();

        Benchmarker benchmarker = new Benchmarker();

        benchmarker.solveMultipleModels();
    }
}