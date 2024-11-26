package com.graphecomplexite.solver;

import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMax;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.variables.IntVar;

public class ChocoSolverFromFzn extends SolverFromFzn {

    private Map<IntVar, Integer> failureMap = new HashMap<>();

    public ChocoSolverFromFzn(String pathToFzn, boolean oneSolutionMode, int n, int k) {
        super(pathToFzn, oneSolutionMode, n, k);
    }

    @Override
    public void findSolution(boolean optimized, boolean doTimeOut) {
        Solver solver = model.getSolver();
        if (optimized) {
            IntVar[] vars = model.retrieveIntVars(true);

            setupFailureMonitor(solver);

            solver.setSearch(
                    Search.intVarSearch(
                            (VariableSelector<IntVar>) variable -> {
                                return Arrays.stream(vars)
                                        .filter(var -> !var.isInstantiated())
                                        .max(Comparator.comparingInt(this::computeCustomPriority))
                                        .orElse(null);
                            },
                            new IntDomainMax(),
                            vars));
        }

        processSolutionsWithTimeout(solver, doTimeOut, optimized);
    }

    private void setupFailureMonitor(Solver solver) {
        solver.plugMonitor(new IMonitorContradiction() {
            @Override
            public void onContradiction(ContradictionException cex) {
                IntVar var = (IntVar) cex.v;
                if (var != null) {
                    failureMap.put(var, failureMap.getOrDefault(var, 0) + 1);
                }
            }
        });
    }

    private int computeCustomPriority(IntVar var) {
        return computeDynamicCentrality(var) + computeDynamicWeight(var);
    }

    private int computeDynamicWeight(IntVar var) {
        int weight = 0;

        int residualDegree = var.getNbProps();
        weight += residualDegree;

        int domainSize = var.getDomainSize();
        weight += (domainSize > 0) ? (1000 / domainSize) : 1000;

        Integer failCount = failureMap.getOrDefault(var, 0);
        weight += failCount * 10;

        return weight;
    }

    private int computeDynamicCentrality(IntVar var) {
        int activeConstraints = var.getNbProps();

        int domainSize = var.getDomainSize();
        int domainFactor = (domainSize > 0) ? (1000 / domainSize) : 1000;

        return activeConstraints + domainFactor;
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