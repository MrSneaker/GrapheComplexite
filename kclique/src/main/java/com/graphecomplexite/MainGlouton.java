package com.graphecomplexite;

import com.graphecomplexite.benchmarker.Benchmarker;
import com.graphecomplexite.solver.GloutonSolverFromFzn;

public class MainGlouton {
    public static void main(String[] args) {

        // String modelFzn = "kclique/data/dimacs_fzn_instance/C250.9_20.fzn";
        String dataDzn = "kclique/data/dimacs_dzn_instance/C250.9_32.dzn";

        // Benchmarker b = new Benchmarker();
        // b.compareSolverOnOneClique(modelFzn, dataDzn);

        GloutonSolverFromFzn g = new GloutonSolverFromFzn(dataDzn);
        g.findSolution();

    }
}
