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
import org.chocosolver.solver.variables.IntVar;


public class ChocoSolverFromFzn extends SolverFromFzn {


    public ChocoSolverFromFzn(String pathToFzn) {
        super(pathToFzn);
    }

    @Override
    public void findSolution() {
        Solver solver = model.getSolver();
        
        Boolean hasTimedOut = false;
        Integer count = 0;
        
        final Duration timeout = Duration.ofMinutes(5);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        final Future<Integer> handler = executor.submit(new Callable<>() {
            @Override
            public Integer call() throws Exception {
                return processSolutions(solver);
            }
        });
        
        try {
            count = handler.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            hasTimedOut = true;
            handler.cancel(true);
        } catch (InterruptedException e) {
            System.out.println("La recherche de solution a été interrompu : " + e.getMessage());
        } catch (ExecutionException e) {
            System.out.println("Exception durant l'execution de la tâche de recherche : " + e.getMessage());
        }
        
        executor.shutdownNow();

        if(hasTimedOut) {
            System.out.println("La recherche prend trop de temps, timeout.");
        } else {
            System.out.println("Il y a " + count + " solution");
        }
    }

    private int processSolutions(Solver solver) {
        Set<String> uniqueCliques = new HashSet<>();
        int count = 0;
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

        return count;

    }
}
