package com.graphecomplexite.benchmarker;

import java.util.List;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.graphecomplexite.solver.ChocoSolverFromFzn;
import com.graphecomplexite.utils.MedianTimePlotter;

public class Benchmarker {

    public Benchmarker() {

    }

    public void solveMultipleModels() {
        List<String> modelsPath = new ArrayList<>();

        File directory = new File(System.getProperty("user.dir") + "/kclique/data");
        if (directory.isDirectory()) {
            String[] fznFiles = directory.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".fzn");
                }
            });

            if (fznFiles != null) {
                for (String fznFile : fznFiles) {
                    modelsPath.add(new File(directory, fznFile).getAbsolutePath());
                }
            }
        } else {
            System.out.println("Le chemin fourni n'est pas un répertoire valide: " + directory);
            return;
        }

        Map<Double, Double> executionTimes = new HashMap<>();

        for(String modelPath : modelsPath) {
            ChocoSolverFromFzn solver = new ChocoSolverFromFzn(modelPath);
            long startTime = System.currentTimeMillis();
            solver.findSolution();
            long endTime = System.currentTimeMillis();
            
            double executionTime = (endTime - startTime) / 1000.0;
            executionTimes.put((double) (solver.getNbClauses() / solver.getNbVar()), executionTime);
        }

        System.out.println(executionTimes);
        MedianTimePlotter chart = new MedianTimePlotter("Execution Times", "Time vs Clause-to-Variable Ratio", executionTimes);
        chart.pack();
        chart.setVisible(true);
    }
}
