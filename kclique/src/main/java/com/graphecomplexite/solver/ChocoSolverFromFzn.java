package com.graphecomplexite.solver;

import java.util.*;
import java.io.*;
import java.time.Duration;
import java.util.concurrent.*;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.variables.IntVar;

public class ChocoSolverFromFzn extends SolverFromFzn {
    private class NodeDegree {
        final int index;
        final int degree;

        NodeDegree(int index, int degree) {
            this.index = index;
            this.degree = degree;
        }
    }

    public ChocoSolverFromFzn(String pathToFzn, boolean oneSolutionMode, int n, int k) {
        super(pathToFzn, oneSolutionMode, n, k);
    }

    @Override
    public void findSolution(boolean optimized, boolean doTimeOut) {
        Solver solver = model.getSolver();
        if (optimized) {
            IntVar[] vars = model.retrieveIntVars(true);

            Map<Integer, IntVar> varMap = new HashMap<>();
            for (IntVar var : vars) {
                String name = var.getName();
                if (name.matches("val\\[\\d+\\]")) {
                    int index = Integer.parseInt(name.replaceAll("\\D", ""));
                    varMap.put(index, var);
                }
            }

            List<NodeDegree> degrees = computeDegreesStreaming();
            List<IntVar> sortedVarList = new ArrayList<>();

            for (NodeDegree degree : degrees) {
                IntVar sortedVar = varMap.get(degree.index);
                if (sortedVar != null) {
                    sortedVarList.add(sortedVar);
                } else {
                    System.err.println("Missing variable at index: " + degree.index);
                }
            }

            for (IntVar var : vars) {
                if (!sortedVarList.contains(var)) {
                    sortedVarList.add(var); // Append missing variables
                }
            }

            IntVar[] sortedVars = sortedVarList.toArray(new IntVar[0]);

            solver.setSearch(
                    Search.intVarSearch(
                            (VariableSelector<IntVar>) variable -> {
                                for (IntVar sortedVar : sortedVars) {
                                    if (!sortedVar.isInstantiated()) {
                                        return sortedVar;
                                    }
                                }
                                return null;
                            },
                            new IntDomainMin(),
                            vars));

        }

        processSolutionsWithTimeout(solver, doTimeOut, optimized);
    }

    private List<NodeDegree> computeDegreesStreaming() {
        List<NodeDegree> degrees = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(getPathToFzn()))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.matches(".*array \\[.*\\] of bool: .* = \\[.*\\].*")) {
                    String[] values = line.substring(line.indexOf('[') + 1, line.lastIndexOf(']')).split(",");

                    for (int i = 0; i < getN(); i++) {
                        int degree = 0;
                        for (int j = 0; j < getN(); j++) {
                            if (values[i * getN() + j].trim().equals("true")) {
                                degree++;
                            }
                        }
                        degrees.add(i, new NodeDegree(i, degree));
                    }
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier : " + e.getMessage());
        }

        // Trier par degré décroissant
        degrees.sort((a, b) -> Integer.compare(b.degree, a.degree));

        return degrees;
    }

    private void processSolutionsWithTimeout(Solver solver, boolean doTimeOut, boolean optimized) {
        Boolean hasTimedOut = false;
        Integer count = 0;

        final Duration timeout = Duration.ofMinutes(10);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        final Future<Integer> handler = executor.submit(() -> processSolutions(solver, optimized));

        try {
            count = doTimeOut ? handler.get(timeout.toMillis(), TimeUnit.MILLISECONDS)
                    : handler.get();
        } catch (TimeoutException e) {
            hasTimedOut = true;
            handler.cancel(true);
        } catch (InterruptedException e) {
            System.out.println("La recherche de solution a été interrompu : " + e.getMessage());
        } catch (ExecutionException e) {
            System.out.println("Exception durant l'execution de la tâche de recherche : " + e.getMessage());
        }

        executor.shutdownNow();

        if (hasTimedOut) {
            System.out.println("La recherche prend trop de temps, timeout.");
        } else if (!this.oneSolutionMode) {
            System.out.println("Il y a " + count + " solution");
        }
    }

    private int processSolutions(Solver solver, boolean optimized) {
        Set<String> uniqueCliques = new HashSet<>();
        int count = 0;

        long start = System.currentTimeMillis();
        while (solver.solve()) {
            StringBuilder clique = new StringBuilder();

            for (IntVar var : model.retrieveIntVars(true)) {
                if (var.getName().contains("X_INTRODUCED")) {
                    clique.append(var.getValue()).append(",");
                }
            }

            String sortedClique = Arrays.stream(clique.toString().split(","))
                    .sorted()
                    .collect(java.util.stream.Collectors.joining(","));

            if (uniqueCliques.add(sortedClique)) {
                if (count == 0) {
                    System.out.println("Une première solution a été trouvée : " + sortedClique);
                    if (this.oneSolutionMode) {
                        break;
                    }
                } else {
                    System.out.print(count + "\r");
                }
                count++;
            }
        }
        long stop = System.currentTimeMillis();

        System.out.println("Real time is : " + (stop - start) / 1000.0);

        return count;
    }
}