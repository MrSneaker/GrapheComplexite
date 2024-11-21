package com.graphecomplexite;

import com.graphecomplexite.solver.ChocoSolverFromFzn;

public class MainCompareOpt {
    public static void main(String[] args) {

        String modelFzn = "kclique/data/dimacs_fzn_instance/C250.9_25.fzn";

        ChocoSolverFromFzn chocoSolver = new ChocoSolverFromFzn(modelFzn, false);

        long startCNo = System.currentTimeMillis();
        chocoSolver.findSolution(false);
        long stopCNo = System.currentTimeMillis();

        long startCo = System.currentTimeMillis();
        chocoSolver.findSolution(true);
        long stopCo = System.currentTimeMillis();

        System.out.println(
                "La méthode complète non-optimisée a pris " + (double) ((stopCNo - startCNo) / 1000.0) + "secondes");
        System.out
                .println("La méthode complète optimisée a pris " + (double) ((stopCo - startCo) / 1000.0) + "secondes");
    }
}
