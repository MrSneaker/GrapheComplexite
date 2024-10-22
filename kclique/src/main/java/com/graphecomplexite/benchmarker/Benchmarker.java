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

import org.javatuples.Pair;

import com.graphecomplexite.solver.ChocoSolverFromFzn;
import com.graphecomplexite.utils.MedianTimePlotter;

public class Benchmarker {

    public Benchmarker() {

    }

    private static Integer extractNValue(File dataFile) {
        if (dataFile.exists() && dataFile.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
                String line;
                Pattern pattern = Pattern.compile("\\bn\\s*=\\s*(\\d+);");

                while ((line = reader.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        return Integer.parseInt(matcher.group(1));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Le fichier de données n'existe pas : " + dataFile.getAbsolutePath());
        }
        return null;
    }

    private static Integer extractKValue(File dataFile) {
        if (dataFile.exists() && dataFile.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
                String line;
                Pattern pattern = Pattern.compile("\\bk\\s*=\\s*(\\d+);");

                while ((line = reader.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        return Integer.parseInt(matcher.group(1));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Le fichier de données n'existe pas : " + dataFile.getAbsolutePath());
        }
        return null;
    }


    private static Integer extractNumberFromFileName(String fznFileName) {
        Pattern pattern = Pattern.compile("_(\\d+)\\.fzn$");
        Matcher matcher = pattern.matcher(fznFileName);
    
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        
        return null;
    }

    public void solveMultipleModels() {
        Map<String, Pair<Integer, Integer>> modelsPathWithN = new HashMap<>();

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
                    Integer numModel = extractNumberFromFileName(fznFile);

                    File dataFile = new File(dataDirectory, "graph_" + numModel + ".dzn");

                    Integer nValue = extractNValue(dataFile);
                    Integer kValue = extractKValue(dataFile);
                    if (nValue != null) {
                        modelsPathWithN.put(new File(modelDirectory, fznFile).getAbsolutePath(), new Pair<Integer,Integer>(nValue, kValue));
                    } else {
                        System.out.println("No valid 'n' value found for: " + dataFile.getAbsolutePath());
                    }
                }
            }
        } else {
            System.out.println("Le chemin fourni n'est pas un répertoire valide: " + modelDirectory);
            return;
        }

        Map<Pair<Integer, Integer>, Double> executionTimes = new HashMap<>();

        for(Map.Entry<String, Pair<Integer, Integer>> entry : modelsPathWithN.entrySet()) {
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
        MedianTimePlotter chart = new MedianTimePlotter("Execution Times", "Time vs Number of Nodes", executionTimes);
        chart.pack();
        chart.setVisible(true);
    }
}
