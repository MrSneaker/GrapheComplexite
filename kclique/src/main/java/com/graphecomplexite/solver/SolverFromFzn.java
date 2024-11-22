package com.graphecomplexite.solver;

import org.chocosolver.parser.flatzinc.Flatzinc;
import org.chocosolver.solver.Model;

public abstract class SolverFromFzn {
    private String pathToFzn;
    protected Model model;
    protected boolean oneSolutionMode;

    public SolverFromFzn(String pathToFzn, boolean oneSolutionMode) {
        this.pathToFzn = pathToFzn;
        this.oneSolutionMode = oneSolutionMode;
        this.model = loadFznModel();
    }

    public String getPathToFzn() {
        return this.pathToFzn;
    }

    public Model loadFznModel() {
        Flatzinc flatzinc = new Flatzinc();

        flatzinc.instance = this.getPathToFzn();

        flatzinc.createSettings();
        flatzinc.createSolver();
        flatzinc.buildModel();

        Model model = flatzinc.getModel();
        return model;
    }

    public void findSolution(boolean optimized, boolean doTimeOut) {
        return;
    }

    public int getNbClauses() {
        return model.getCstrs().length;
    }

    public int getNbVar() {
        return model.getVars().length;
    }
}