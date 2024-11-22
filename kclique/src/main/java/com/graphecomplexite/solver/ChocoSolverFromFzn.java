package com.graphecomplexite.solver;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.time.Duration;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.DomOverWDeg;
import org.chocosolver.solver.variables.IntVar;

public class ChocoSolverFromFzn extends SolverFromFzn {

    public ChocoSolverFromFzn(String pathToFzn, boolean oneSolutionMode) {
        super(pathToFzn, oneSolutionMode);
    }

    @Override
    public void findSolution(boolean optimized, boolean doTimeOut) {
        Solver solver = model.getSolver();

        if (optimized) {
            /*
             * EXPLIQUATION STRATÉGIE OPTI :
             * Supposons un graphe de 6 nœuds où nous cherchons une 3-clique :
             * 
             * Variables :
             * Chaque variable xi (booléenne) indique si le nœud i est dans la clique (xi =
             * 1).
             * 
             * Contraintes :
             * La somme des xi doit être exactement égale à 3 (cardinalité de la clique).
             * Si xi = 1 et xj = 1, alors i et j doivent être connectés (selon la matrice
             * d'adjacence).
             * 
             * Heuristique DomOverWDeg :
             * Si x3 est un nœud avec 4 voisins (connecté à de nombreux autres nœuds), et si
             * le
             * domaine de x3 est {0,1} :
             * WeightedDegree = 4 (lié à 4 autres contraintes).
             * Domaine = 2 valeurs possibles (0,1).
             * Score = 2/4 = 0.5.
             * Une autre variable x5, connectée à seulement 1 autre nœud, aurait un score
             * 2/1=2.
             * 
             * Dans ce cas, le solveur explore d'abord x3 (score plus faible), car sa valeur
             * a un impact potentiel plus fort sur la
             * propagation.
             */
            solver.setSearch(
                    Search.intVarSearch(
                            new DomOverWDeg<IntVar>(model.retrieveIntVars(true), 0),
                            new IntDomainMin(),
                            model.retrieveIntVars(true)));
        }

        Boolean hasTimedOut = false;
        Integer count = 0;

        final Duration timeout = Duration.ofMinutes(10);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        final Future<Integer> handler = executor.submit(new Callable<>() {
            @Override
            public Integer call() throws Exception {
                return processSolutions(solver);
            }
        });

        try {
            if (doTimeOut) {
                count = handler.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } else {
                count = handler.get();
            }
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

    private int processSolutions(Solver solver) {
        Set<String> uniqueCliques = new HashSet<>();
        int count = 0;
        while (solver.solve()) {
            StringBuilder clique = new StringBuilder();
            for (IntVar var : model.retrieveIntVars(true)) {
                if (var.getName().contains("X_INTRODUCED")) {
                    clique.append(var.getValue()).append(",");
                }
            }

            String[] nodes = clique.toString().split(",");
            Arrays.sort(nodes);
            String sortedClique = String.join(",", nodes);

            if (uniqueCliques.add(sortedClique)) {
                if (count == 0) {
                    System.out.println("Une première solution a été trouvée : " + sortedClique);
                    if (this.oneSolutionMode) {
                        break;
                    }
                }
                count++;
            }
        }

        return count;

    }
}
