package com.graphecomplexite.solver;

import java.util.List;

import org.chocosolver.graphsolver.GraphModel;
import org.chocosolver.graphsolver.variables.IGraphVarFactory;
import org.chocosolver.graphsolver.variables.UndirectedGraphVar;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;

import com.graphecomplexite.utils.GraphData;
import com.graphecomplexite.utils.MiniZincParser;

public class ChocoGraphSolverFromFzn extends SolverFromFzn{

    public ChocoGraphSolverFromFzn(String pathToFzn) {
        super(pathToFzn);
    }

    @Override
    public void findSolution() {
        GraphModel graphModel = new GraphModel();
        GraphData graphData = MiniZincParser.parseMiniZincFile(getPathToFzn());

        int numberOfNodes = graphData.getNumberOfNodes();
        List<int[]> edges = graphData.getEdges();

        UndirectedGraph LB = new UndirectedGraph(graphModel, numberOfNodes, SetType.BITSET, true);
		UndirectedGraph UB = new UndirectedGraph(graphModel, numberOfNodes, SetType.BITSET, true);


        for (int[] edge : edges) {
            // for (int e : edge) {
            //     System.out.println("elem of edge is " + e); 
            // }
            // System.out.println("edge[0]: " + edge);
            UB.addEdge(edge[0], edge[1]);
        }

        IGraphVarFactory factory = (IGraphVarFactory) graphModel;
        UndirectedGraphVar graphVar = factory.graphVar(getPathToFzn(), LB, UB);
        
        IntVar[] clique = new IntVar[graphData.getK()];
        for (int i = 0; i < graphData.getK(); i++) {
            clique[i] = graphModel.intVar("node" + i, 0, numberOfNodes - 1);
        }

        for (String constraint : graphData.getConstraints()) {
            if (constraint.contains("fzn_all_different_int")) {
                graphModel.allDifferent(clique).post();
            }
        }

        Solver solver = graphModel.getSolver();

        Boolean hasSolution = solver.solve();

        if(hasSolution) {
            System.out.println("Solution trouvée !");
            for (IntVar var : graphModel.retrieveIntVars(true)) {
                if(var.getName().contains("X_INTRODUCED")) {
                    System.out.println(var.getName() + " = " + var.getValue());
                }
            }
        } else {
            System.out.println("Pas de solution.");
        }
    }
    
}
