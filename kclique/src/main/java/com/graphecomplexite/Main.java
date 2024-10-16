package com.graphecomplexite;

import org.chocosolver.parser.flatzinc.Flatzinc;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.Model;

public class Main {
    public static void main(String[] args) {
        // Chemin vers le fichier FlatZinc à résoudre
        String projectRoot = System.getProperty("user.dir");
        String filePath = projectRoot + "/kclique/data/model1_graph100_k_5.fzn";

        // Crée une instance de Flatzinc
        Flatzinc flatzinc = new Flatzinc();

        // Spécifie le fichier FlatZinc à résoudre
        flatzinc.instance = filePath;

        // Crée le modèle
        flatzinc.createSettings();
        flatzinc.createSolver();
        flatzinc.buildModel();

        // Utilisation du solveur pour trouver une solution
        Model model = flatzinc.getModel(); // Obtient le solveur
        Solver solver = model.getSolver(); // Résout le modèle
        boolean hasSolution = solver.solve();

        if (hasSolution) {
            System.out.println("Solution trouvée !");
            for (IntVar var : model.retrieveIntVars(true)) {
                if(var.getName().contains("X_")) { // on print les nodes qui constitue la clique
                    System.out.println(var.getName() + " = " + var.getValue());
                }
            }

        } else {
            System.out.println("Aucune solution trouvée.");
        }
    }
}