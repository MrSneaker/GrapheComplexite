package com.graphecomplexite.benchmarker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javatuples.Pair;

import com.graphecomplexite.solver.ChocoSolverFromFzn;
import com.graphecomplexite.solver.GloutonSolverFromFzn;
import com.graphecomplexite.utils.MultiInstancePlotter;
import com.graphecomplexite.utils.SolverComparatorPlotter;

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

    private static List<Integer> extractNumberFromFileName(String fznFileName, boolean isDIMACS) {
        List<Integer> res = new ArrayList<>();
        if (isDIMACS) {
            Pattern pattern = Pattern.compile("C(\\d+)\\.\\d+_(\\d+)\\.fzn$");
            Matcher matcher = pattern.matcher(fznFileName);

            if (matcher.find()) {
                res.add(Integer.parseInt(matcher.group(1)));
                res.add(Integer.parseInt(matcher.group(2)));
            }

        } else {
            Pattern pattern = Pattern.compile("_(\\d+)\\.fzn$");
            Matcher matcher = pattern.matcher(fznFileName);

            if (matcher.find()) {
                res.add(Integer.parseInt(matcher.group(1)));
            }
        }

        return res;
    }

    private Map<String, Pair<Integer, Integer>> foundModelInstances(File modelDirectory, File dataDirectory,
            boolean isDMACS, int maxK) {
        Map<String, Pair<Integer, Integer>> modelsPathWithN = new HashMap<>();
        if (modelDirectory.isDirectory()) {
            String[] fznFiles = modelDirectory.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".fzn");
                }
            });

            if (fznFiles != null) {
                for (String fznFile : fznFiles) {
                    if (isDMACS) {
                        List<Integer> values = extractNumberFromFileName(fznFile, true);
                        Integer nValue = values.get(0);
                        Integer kValue = values.get(1);
                        if (nValue != null) {
                            if (kValue <= maxK) {
                                modelsPathWithN.put(new File(modelDirectory, fznFile).getAbsolutePath(),
                                        new Pair<Integer, Integer>(nValue, kValue));
                            }
                        } else {
                            System.out.println("No valid 'n' value found for: " + fznFile);
                        }
                    } else {
                        Integer numModel = extractNumberFromFileName(fznFile, false).get(0);
                        File dataFile = null;
                        dataFile = new File(dataDirectory, "graph_" + numModel + ".dzn");
                        Integer nValue = extractNValue(dataFile);
                        Integer kValue = extractKValue(dataFile);
                        if (nValue != null) {
                            modelsPathWithN.put(new File(modelDirectory, fznFile).getAbsolutePath(),
                                    new Pair<Integer, Integer>(nValue, kValue));
                        } else {
                            System.out.println("No valid 'n' value found for: " + dataFile.getAbsolutePath());
                        }
                    }

                }
            }
        } else {
            System.out.println("Le chemin fourni n'est pas un répertoire valide: " + modelDirectory);
            return new HashMap<>();
        }
        return modelsPathWithN;
    }

    public void solveMultipleModels() {

        File modelDirectory = new File(System.getProperty("user.dir") + "/kclique/data/flatzinc_instance/");
        File dataDirectory = new File(System.getProperty("user.dir") + "/kclique/data/graph_data/");

        Map<String, Pair<Integer, Integer>> modelsPathWithN = foundModelInstances(modelDirectory, dataDirectory, false,
                1000);

        Map<Pair<Integer, Integer>, Double> executionTimes = new HashMap<>();

        for (Map.Entry<String, Pair<Integer, Integer>> entry : modelsPathWithN.entrySet()) {
            System.out.println();
            System.out.println("Solving " + entry.getKey().split("/")[entry.getKey().split("/").length - 1] + " ...");

            ChocoSolverFromFzn solver = new ChocoSolverFromFzn(entry.getKey(), false);
            long startTime = System.currentTimeMillis();
            solver.findSolution(false, true);
            long endTime = System.currentTimeMillis();

            double executionTime = (endTime - startTime) / 1000.0;

            executionTimes.put(entry.getValue(), executionTime);
            System.out.println();
        }

        System.out.println(executionTimes);
        MultiInstancePlotter chart = new MultiInstancePlotter("Execution Times", "Time vs Number of Nodes",
                executionTimes);
        chart.pack();
        chart.setVisible(true);
    }

    public void compareSolver(int maxK) {
        File modelDirectory = new File(System.getProperty("user.dir") + "/kclique/data/dimacs_fzn_instance");
        File dataDirectory = new File(System.getProperty("user.dir") + "/kclique/data/dimacs_dzn_instance");

        Map<String, Pair<Integer, Integer>> modelsPathWithN = foundModelInstances(modelDirectory, dataDirectory, true,
                maxK);

        Map<String, Map<String, Double>> data = new HashMap<>();

        for (Map.Entry<String, Pair<Integer, Integer>> entry : modelsPathWithN.entrySet()) {
            ChocoSolverFromFzn chocoSolver = new ChocoSolverFromFzn(entry.getKey(), false);
            ChocoSolverFromFzn chocoSolverOpti = new ChocoSolverFromFzn(entry.getKey(), false);

            long startCNo = System.currentTimeMillis();
            chocoSolver.findSolution(false, false);
            long stopCNo = System.currentTimeMillis();

            long startCo = System.currentTimeMillis();
            chocoSolverOpti.findSolution(true, false);
            long stopCo = System.currentTimeMillis();

            Map<String, Double> solverTimes = Map.of(
                    "Solver - Stratégie par défaut", (double) (stopCNo - startCNo) / 1000.0,
                    "Solver - Stratégie optimisée", (double) (stopCo - startCo) / 1000.0);
            data.put("n = " + entry.getValue().getValue0() + ", k = " + entry.getValue().getValue1(), solverTimes);
        }

        SolverComparatorPlotter plot = new SolverComparatorPlotter("Complete solution comparaison",
                "Complete solution comparaison", data);

        plot.pack();
        plot.setVisible(true);
    }

    public void compareSolverOnKClique(int maxK) {

        File modelDirectory = new File(System.getProperty("user.dir") + "/kclique/data/dimacs_fzn_instance");
        File dataDirectory = new File(System.getProperty("user.dir") + "/kclique/data/dimacs_dzn_instance");

        Map<String, Pair<Integer, Integer>> modelsPathWithN = foundModelInstances(modelDirectory, dataDirectory, true,
                maxK);

        Map<String, Map<String, Double>> data = new HashMap<>();

        for (Map.Entry<String, Pair<Integer, Integer>> entry : modelsPathWithN.entrySet()) {
            ChocoSolverFromFzn chocoSolver = new ChocoSolverFromFzn(entry.getKey(), true);
            ChocoSolverFromFzn chocoSolverOpti = new ChocoSolverFromFzn(entry.getKey(), true);
            String dznPath = entry.getKey().replace("fzn", "dzn");
            GloutonSolverFromFzn glouton = new GloutonSolverFromFzn(dznPath);

            long startCNo = System.currentTimeMillis();
            chocoSolver.findSolution(false, false);
            long stopCNo = System.currentTimeMillis();

            long startCo = System.currentTimeMillis();
            chocoSolverOpti.findSolution(true, false);
            long stopCo = System.currentTimeMillis();

            long startG = System.currentTimeMillis();
            glouton.findSolution();
            long stopG = System.currentTimeMillis();

            Map<String, Double> solverTimes = Map.of(
                    "Solver - Stratégie par défaut", (double) (stopCNo - startCNo) / 1000.0,
                    "Solver - Stratégie optimisée", (double) (stopCo - startCo) / 1000.0,
                    "Solver glouton", (double) (stopG - startG) / 1000.0);

            data.put("n = " + entry.getValue().getValue0() + ", k = " + entry.getValue().getValue1(), solverTimes);
        }

        SolverComparatorPlotter plot = new SolverComparatorPlotter("All solution comparaison",
                "Comparaison de toutes les solutions pour trouver une k-clique", data);

        plot.pack();
        plot.setVisible(true);
    }
}
