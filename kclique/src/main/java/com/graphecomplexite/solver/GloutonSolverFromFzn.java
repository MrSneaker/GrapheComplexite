package com.graphecomplexite.solver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GloutonSolverFromFzn {

    private String pathToDzn;
    private int n;
    private int k;
    private boolean[][] edges;

    public GloutonSolverFromFzn(String pathToDzn) {
        this.pathToDzn = pathToDzn;
    }

    private void parseDzn() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(pathToDzn));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();

            if (line.startsWith("n")) {
                n = Integer.parseInt(line.split("=")[1].replace(";", "").trim());
            } else if (line.startsWith("k")) {
                k = Integer.parseInt(line.split("=")[1].replace(";", "").trim());
            } else if (line.startsWith("edges")) {
                StringBuilder edgesData = new StringBuilder();
                while (!line.contains("];")) {
                    edgesData.append(line.replace("edges = ", "").replace("[|", "").replace("|];", "").trim());
                    line = reader.readLine().trim();
                }
                edgesData.append(line.replace("];", "").replace("|", "").trim());
            
                String[] rows = edgesData.toString().split("\\|");
                edges = new boolean[n][n];
                for (int i = 0; i < rows.length; i++) {
                    String[] values = rows[i].trim().split(",");
                    for (int j = 0; j < values.length; j++) {
                        edges[i][j] = values[j].trim().equals("true");
                    }
                }
            }            
        }
        reader.close();
    }

    public void findSolution() {
        try {
            parseDzn();
        } catch (IOException e) {
            System.err.println("Erreur lors du parsing du fichier : " + e.getMessage());
            return;
        }

        List<Integer> clique = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if (clique.size() < k) {
                boolean canAdd = true;
                for (int node : clique) {
                    if (!edges[node][i]) {
                        canAdd = false;
                        break;
                    }
                }
                if (canAdd) {
                    clique.add(i);
                }
            }
        }

        if (clique.size() == k) {
            System.out.println("Clique trouvée de taille " + k + " : " + clique);
        } else {
            System.out.println("Impossible de trouver une clique de taille " + k + " dans le graphe.");
        }
    }
}
