package com.graphecomplexite;

import com.graphecomplexite.benchmarker.Benchmarker;

public class MainGlouton {
    public static void main(String[] args) {

        Benchmarker b = new Benchmarker();
        b.CompareNbTryGlouton(44);
        b.compareSolverOnKClique(44, true, false);

    }
}
