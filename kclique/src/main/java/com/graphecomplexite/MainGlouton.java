package com.graphecomplexite;

import com.graphecomplexite.solver.ChocoSolverFromFzn;
import com.graphecomplexite.solver.GloutonSolverFromFzn;

public class MainGlouton {
    public static void main(String[] args) {

        String modelFzn = "kclique/data/dimacs_fzn_instance/C125.9_25.fzn";
        String dataDzn = "kclique/data/dimacs_dzn_instance/C125.9_25.dzn";

        GloutonSolverFromFzn glouton = new GloutonSolverFromFzn(dataDzn);
        glouton.findSolution();

        ChocoSolverFromFzn chocoSolver = new ChocoSolverFromFzn(modelFzn, true);
        chocoSolver.findSolution();
    }
}
