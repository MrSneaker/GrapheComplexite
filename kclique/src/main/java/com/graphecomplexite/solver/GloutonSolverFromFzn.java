package com.graphecomplexite.solver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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

    private int getNodeDegree(int node) {
        int degree = 0;
        for (boolean connected : edges[node]) {
            if (connected)
                degree++;
        }
        return degree;
    }

    public int findSolution() {
        try {
            parseDzn();
        } catch (IOException e) {
            System.err.println("Erreur lors du parsing du fichier : " + e.getMessage());
            return -1;
        }

        int nbTry = 0;

        List<Integer> clique = new ArrayList<>();

        for (int m = 0; m < 10000; ++m) {
            clique.clear();
            List<Integer> nodes = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                nodes.add(i);
            }
            nodes.sort((a, b) -> Integer.compare(getNodeDegree(b), getNodeDegree(a)));

            if (m > 0) {
                Collections.shuffle(nodes);
            }

            for (int i : nodes) {
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
                System.out.print(
                        "Méthode 1 - Essaie " + (m + 1) + " : Clique trouvée de taille " + k + " : " + clique + "\r");
                nbTry = m + 1;
                break;
            } else {
                System.out.print("Méthode 1 - Essaie " + (m + 1) + " : Impossible de trouver une clique de taille " + k
                        + " dans le graphe.\r");
                nbTry = m + 1;

            }

        }
        System.out.println();
        return nbTry;

    }

    public int findSolutionAlternative() {
        try {
            parseDzn();
        } catch (IOException e) {
            System.err.println("Erreur lors du parsing du fichier : " + e.getMessage());
            return -1;
        }

        int nbTry = 0;

        List<Integer> clique = new ArrayList<>();

        for (int m = 0; m < 10000; ++m) {
            List<Integer> nodes = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                nodes.add(i);
            }
            Collections.shuffle(nodes);

            clique.clear();

            for (int i : nodes) {
                boolean canAdd = true;
                for (int node : clique) {
                    if (!edges[node][i]) {
                        canAdd = false;
                        break;
                    }
                }
                if (canAdd) {
                    clique.add(i);
                    if (clique.size() == k) {
                        break;
                    }
                }
            }

            if (clique.size() == k) {
                System.out.print(
                        "Méthode 2 - Essaie " + (m + 1) + " : Clique trouvée de taille " + k + " : " + clique + "\r");
                nbTry = m + 1;
                break;
            } else {
                System.out.print("Méthode 2 - Essaie " + (m + 1) + " : Impossible de trouver une clique de taille " + k
                        + " dans le graphe.\r");
                nbTry = m + 1;
            }
        }
        System.out.println();
        return nbTry;
    }

    public List<List<Integer>> findUniqueKCliques() {
        try {
            parseDzn();
        } catch (IOException e) {
            System.err.println("Erreur lors du parsing du fichier : " + e.getMessage());
            return Collections.emptyList();
        }

        List<List<Integer>> allCliques = new ArrayList<>();
        List<Integer> clique = new ArrayList<>();
        List<Integer> nodes = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            nodes.add(i);
        }

        nodes.sort((a, b) -> Integer.compare(getNodeDegree(b), getNodeDegree(a)));

        for (int m = 0; m < 10000; ++m) {
            clique.clear();

            nodes.sort((a, b) -> Integer.compare(getNodeDegree(b), getNodeDegree(a)));

            if (m > 0) {
                Collections.shuffle(nodes);
            }

            for (int i : nodes) {
                boolean canAdd = true;
                for (int node : clique) {
                    if (!edges[node][i]) {
                        canAdd = false;
                        break;
                    }
                }

                if (canAdd) {
                    clique.add(i);
                    if (clique.size() == k) {
                        if (!isDuplicateClique(allCliques, clique)) {
                            allCliques.add(new ArrayList<>(clique));
                        }
                        break;
                    }
                }
            }
        }

        System.out.println("Nombre total de k-cliques uniques trouvées : " + allCliques.size());
        return allCliques;
    }

    private boolean isDuplicateClique(List<List<Integer>> allCliques, List<Integer> clique) {
        HashSet<Integer> cliqueSet = new HashSet<>(clique);
        for (List<Integer> existingClique : allCliques) {
            if (new HashSet<>(existingClique).equals(cliqueSet)) {
                return true;
            }
        }
        return false;
    }

}
