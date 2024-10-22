package com.graphecomplexite.benchmarker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.graphecomplexite.solver.ChocoSolverFromFzn;
import com.graphecomplexite.utils.MedianTimePlotter;

public class Benchmarker {

    public Benchmarker() {

    }

    private static Integer extractNValue(File dataFile) {
        if (dataFile.exists() && dataFile.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
                String line;
                // Regular expression to match "k = <number>;"
                Pattern pattern = Pattern.compile("\\bn\\s*=\\s*(\\d+);");

                while ((line = reader.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        // If a match is found, return the integer value of k
                        return Integer.parseInt(matcher.group(1));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception
            }
        } else {
            System.out.println("Le fichier de données n'existe pas : " + dataFile.getAbsolutePath());
        }
        return null; // Return null if 'k' is not found
    }

    private static Integer extractNumberFromFileName(String fznFileName) {
        // Regular expression to match the number before the file extension (e.g., "output_8.fzn" -> 8)
        Pattern pattern = Pattern.compile("_(\\d+)\\.fzn$");
        Matcher matcher = pattern.matcher(fznFileName);
    
        if (matcher.find()) {
            // Return the number as an integer
            return Integer.parseInt(matcher.group(1));
        }
        
        return null; // Return null if no number is found
    }

    public void solveMultipleModels() {
        Map<String, Integer> modelsPathWithN = new HashMap<>();

        File modelDirectory = new File(System.getProperty("user.dir") + "/kclique/data/flatzinc_instance/");
        File dataDirectory = new File(System.getProperty("user.dir") + "/kclique/data/graph_data/");
        if (modelDirectory.isDirectory()) {
            String[] fznFiles = modelDirectory.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".fzn");
                }
            });

            if (fznFiles != null) {
                for (String fznFile : fznFiles) {
                    // Get the base file name without extension
                    Integer numModel = extractNumberFromFileName(fznFile);

                    // Construct the corresponding data file path
                    File dataFile = new File(dataDirectory, "graph_" + numModel + ".dzn");

                    // Parse the data file to extract the value of 'k'
                    Integer nValue = extractNValue(dataFile);
                    if (nValue != null) {
                        // Store the path of the .fzn file and the value of k in the map
                        modelsPathWithN.put(new File(modelDirectory, fznFile).getAbsolutePath(), nValue);
                    } else {
                        System.out.println("No valid 'n' value found for: " + dataFile.getAbsolutePath());
                    }
                }
            }
        } else {
            System.out.println("Le chemin fourni n'est pas un répertoire valide: " + modelDirectory);
            return;
        }

        Map<Integer, Double> executionTimes = new HashMap<>();

        for(Map.Entry<String, Integer> entry : modelsPathWithN.entrySet()) {
            System.out.println();
            System.out.println("Solving " +  entry.getKey().split("/")[entry.getKey().split("/").length - 1] + " ...");

            ChocoSolverFromFzn solver = new ChocoSolverFromFzn(entry.getKey());
            long startTime = System.currentTimeMillis();
            solver.findSolution();
            long endTime = System.currentTimeMillis();
            
            double executionTime = (endTime - startTime) / 1000.0;
            

            executionTimes.put(entry.getValue(), executionTime);
            System.out.println();
        }

        System.out.println(executionTimes);
        MedianTimePlotter chart = new MedianTimePlotter("Execution Times", "Time vs Clause-to-Variable Ratio", executionTimes);
        chart.pack();
        chart.setVisible(true);
    }
}
