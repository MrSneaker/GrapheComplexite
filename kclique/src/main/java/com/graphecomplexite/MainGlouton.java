package com.graphecomplexite;

import com.graphecomplexite.solver.ChocoSolverFromFzn;
import com.graphecomplexite.solver.GloutonSolverFromFzn;

public class MainGlouton {
    public static void main(String[] args) {

        String modelFzn = "kclique/data/dimacs_fzn_instance/C250.9_25.fzn";
        String dataDzn = "kclique/data/dimacs_dzn_instance/C250.9_25.dzn";

        GloutonSolverFromFzn glouton = new GloutonSolverFromFzn(dataDzn);
        long startGl = System.currentTimeMillis();
        glouton.findSolution();
        long stopGl = System.currentTimeMillis();

        ChocoSolverFromFzn chocoSolver = new ChocoSolverFromFzn(modelFzn, true);

        long startCNo = System.currentTimeMillis();
        chocoSolver.findSolution(false);
        long stopCNo = System.currentTimeMillis();

        long startCo = System.currentTimeMillis();
        chocoSolver.findSolution(true);
        long stopCo = System.currentTimeMillis();

        System.out.println("La méthode gloutonne a pris " + (double) ((stopGl - startGl) / 1000.0) + "secondes");
        System.out.println("La méthode complète non-optimisée a pris " + (double) ((stopCNo - startCNo) / 1000.0) + "secondes");
        System.out.println("La méthode complète optimisée a pris " + (double) ((stopCo - startCo) / 1000.0) + "secondes");
    }
}
