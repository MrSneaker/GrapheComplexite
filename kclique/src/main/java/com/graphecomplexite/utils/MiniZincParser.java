package com.graphecomplexite.utils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MiniZincParser {

    public static void main(String[] args) {
        String filePath = System.getProperty("user.dir") + "/kclique/data/graph_10_k_3.mzn";
        parseMiniZincFile(filePath);
    }

    public static GraphData parseMiniZincFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int numberOfNodes = 0;
            int k = 0;
            List<int[]> edges = new ArrayList<>();
            List<String> constraints = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                if (line.contains("var 1..")) {
                    Matcher matcher = Pattern.compile("var 1\\.\\.(\\d+)").matcher(line);
                    if (matcher.find()) {
                        numberOfNodes = Integer.parseInt(matcher.group(1));
                    }
                }

                if (line.contains("clique_nodes")) {
                    Matcher matcher = Pattern.compile("array \\[1\\.\\.(\\d+)\\]").matcher(line);
                    if (matcher.find()) {
                        k = Integer.parseInt(matcher.group(1));
                    }
                }

                if (line.contains("choco_array_var_bool_element2d_nonshifted")) {
                    Matcher matcher = Pattern.compile("X_INTRODUCED_(\\d+)_,\\d+,X_INTRODUCED_(\\d+)").matcher(line);
                    if (matcher.find()) {
                        int node1 = Integer.parseInt(matcher.group(1));
                        int node2 = Integer.parseInt(matcher.group(2));
                        edges.add(new int[]{node1, node2});
                    }
                }

                if (line.contains("constraint")) {
                    constraints.add(line);
                }
            }

            System.out.println("Nombre de nœuds : " + numberOfNodes);
            System.out.println("Valeur de k : " + k);
            System.out.println("Arêtes entre les nœuds : " + edges);
            System.out.println("Contraintes : " + constraints);

            return new GraphData(numberOfNodes, k, edges, constraints);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}