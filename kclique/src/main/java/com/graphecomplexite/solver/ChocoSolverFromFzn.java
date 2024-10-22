package com.graphecomplexite.solver;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;


public class ChocoSolverFromFzn extends SolverFromFzn {


    public ChocoSolverFromFzn(String pathToFzn) {
        super(pathToFzn);
    }

    @Override
    public void findSolution() {
        Solver solver = model.getSolver();

        Integer count = 0;
        Set<String> uniqueCliques = new HashSet<>();

        while(solver.solve()) {
            StringBuilder clique = new StringBuilder();
            for (IntVar var : model.retrieveIntVars(true)) {
                if(var.getName().contains("X_INTRODUCED")) {
                    clique.append(var.getValue()).append(",");
                }
            }

            String[] nodes = clique.toString().split(",");
            Arrays.sort(nodes);
            String sortedClique = String.join(",", nodes);

            if (uniqueCliques.add(sortedClique)) {
                if(count == 0) {
                    System.out.println("Une première solution a été trouvée : " + sortedClique);
                }
                count++;
            }
        }

        System.out.println("Il y a " + count + " solution");
    }
}
