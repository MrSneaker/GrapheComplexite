import time
from matplotlib.lines import Line2D
import matplotlib.pyplot as plt
import os
from ortools.sat.python import cp_model

def find_cliques_ortools(adj_matrix, k, timeout=600):
    """
    Trouve toutes les cliques de taille k dans un graphe à l'aide de OR-Tools avec un timeout.
    
    :param adj_matrix: Liste de listes représentant la matrice d'adjacence (n x n).
    :param k: Taille de la clique à rechercher.
    :param timeout: Temps limite en secondes avant que la recherche n'ait un timeout (default 60s).
    :return: Liste des cliques trouvées.
    """
    n = len(adj_matrix)
    model = cp_model.CpModel()

    # Variables : x[i] = 1 si le nœud i est dans la clique, sinon 0
    x = [model.NewBoolVar(f'x_{i}') for i in range(n)]

    # Contraintes :
    # 1. La clique doit contenir exactement k nœuds
    model.Add(sum(x) == k)

    # 2. Les nœuds sélectionnés doivent être connectés entre eux
    for i in range(n):
        for j in range(i + 1, n):
            if not adj_matrix[i][j]:  # Pas d'arête entre i et j
                model.AddBoolOr([x[i].Not(), x[j].Not()])  # x[i] et x[j] ne peuvent pas être dans la clique ensemble

    # Résoudre le problème
    solver = cp_model.CpSolver()
    solver.parameters.max_time_in_seconds = timeout  # Définir le timeout de 60 secondes
    
    cliques = []

    class CliqueCollector(cp_model.CpSolverSolutionCallback):
        """Collecte toutes les solutions (cliques trouvées)."""
        def __init__(self, variables):
            cp_model.CpSolverSolutionCallback.__init__(self)
            self.variables = variables
            self.solutions = []

        def on_solution_callback(self):
            clique = [i for i, var in enumerate(self.variables) if self.Value(var) == 1]
            self.solutions.append(clique)

    # Résoudre avec un callback pour collecter toutes les solutions
    solution_collector = CliqueCollector(x)
    solver.SearchForAllSolutions(model, solution_collector)

    # Vérification des résultats après la recherche
    if len(solution_collector.solutions) > 0:
        print(f"{len(solution_collector.solutions)} cliques trouvées.")
        return solution_collector.solutions
    else:
        print("Aucune solution trouvée (peut-être un timeout).")
        return []
    
def find_cliques_ortools_degree(adj_matrix, k, timeout=600):
    """
    Trouve toutes les cliques de taille k dans un graphe à l'aide de OR-Tools avec un timeout.
    
    :param adj_matrix: Liste de listes représentant la matrice d'adjacence (n x n).
    :param k: Taille de la clique à rechercher.
    :param timeout: Temps limite en secondes avant que la recherche n'ait un timeout (default 60s).
    :return: Liste des cliques trouvées.
    """
    n = len(adj_matrix)
    model = cp_model.CpModel()

    # Variables : x[i] = 1 si le nœud i est dans la clique, sinon 0
    x = [model.NewBoolVar(f'x_{i}') for i in range(n)]

    # Contraintes :
    # 1. La clique doit contenir exactement k nœuds
    model.Add(sum(x) == k)

    # 2. Les nœuds sélectionnés doivent être connectés entre eux
    for i in range(n):
        for j in range(i + 1, n):
            if not adj_matrix[i][j]:  # Pas d'arête entre i et j
                model.AddBoolOr([x[i].Not(), x[j].Not()])  # x[i] et x[j] ne peuvent pas être dans la clique ensemble

    # Calculer le degré des nœuds
    degrees = [sum(row) for row in adj_matrix]
    nodes_by_degree = sorted(range(n), key=lambda i: degrees[i], reverse=True)

    # Ajouter une stratégie de décision pour commencer par les nœuds avec les plus grands degrés
    model.AddDecisionStrategy(
        [x[i] for i in nodes_by_degree],  # Prioriser les variables dans l'ordre des degrés décroissants
        cp_model.CHOOSE_FIRST,           # Choisir la première variable non attribuée
        cp_model.SELECT_MAX_VALUE        # Attribuer d'abord la valeur maximale (1)
    )

    # Résoudre le problème
    solver = cp_model.CpSolver()
    solver.parameters.max_time_in_seconds = timeout  # Définir le timeout de 60 secondes
    
    cliques = []

    class CliqueCollector(cp_model.CpSolverSolutionCallback):
        """Collecte toutes les solutions (cliques trouvées)."""
        def __init__(self, variables):
            cp_model.CpSolverSolutionCallback.__init__(self)
            self.variables = variables
            self.solutions = []

        def on_solution_callback(self):
            clique = [i for i, var in enumerate(self.variables) if self.Value(var) == 1]
            self.solutions.append(clique)

    # Résoudre avec un callback pour collecter toutes les solutions
    solution_collector = CliqueCollector(x)
    solver.SearchForAllSolutions(model, solution_collector)

    # Vérification des résultats après la recherche
    if len(solution_collector.solutions) > 0:
        print(f"{len(solution_collector.solutions)} cliques trouvées.")
        return solution_collector.solutions
    else:
        print("Aucune solution trouvée (peut-être un timeout).")
        return []


def read_dzn_to_adj_matrix(dzn_file):
    """
    Lit un fichier MiniZinc (.dzn) et extrait la matrice d'adjacence.
    
    :param dzn_file: Chemin du fichier .dzn
    :return: Matrice d'adjacence (liste de listes) et k
    """
    with open(dzn_file, 'r') as f:
        lines = f.readlines()

    n = int([line for line in lines if line.startswith("n =")][0].split('=')[1].strip('; \n'))
    k = int([line for line in lines if line.startswith("k =")][0].split('=')[1].strip('; \n'))
    edges_start = lines.index("edges = [|\n") + 1
    edges_end = edges_start + n

    adj_matrix = []
    for line in lines[edges_start:edges_end]:
        row = line.strip(' |;\n').split(', ')
        adj_matrix.append([val == 'true' for val in row])

    return adj_matrix, k

import re

def find_cliques_with_ortools(data_dir, k_range, result_dir):
    """
    Recherche de cliques pour un ensemble de fichiers .dzn en appliquant des optimisations :
    - Tri des fichiers par ordre numérique (ex. graph_1.dzn, graph_5.dzn, ..., graph_100.dzn).
    - Arrêt anticipé si 3 timeouts consécutifs sont détectés pour une valeur donnée de k.

    :param data_dir: Répertoire contenant les fichiers d'entrée (.dzn).
    :param k_range: Liste ou range des valeurs de k à tester.
    :param result_dir: Répertoire pour enregistrer les résultats.
    :return: node_counts (nombre de nœuds par fichier), times (temps d'exécution), clique_counts (nombre de cliques).
    """
    times = {k: [] for k in k_range}  # Dictionnaire pour stocker les temps par k
    node_counts = []  # Liste pour stocker les nombres de nœuds
    clique_counts = {k: [] for k in k_range}  # Dictionnaire pour les cliques trouvées par k

    if not os.path.exists(result_dir):
        os.makedirs(result_dir)

    # Trier les fichiers par numéro dans leur nom
    def extract_number(filename):
        match = re.search(r'\d+', filename)  # Rechercher les nombres dans le nom de fichier
        return int(match.group()) if match else float('inf')  # Retourner le nombre trouvé, sinon un grand nombre

    data_files = sorted(
        [f for f in os.listdir(data_dir) if f.endswith('.dzn')],
        key=extract_number
    )

    for k_value in k_range:
        timeout_streak = 0  # Compteur de timeouts consécutifs pour la valeur k

        for i, data_file in enumerate(data_files):
            if timeout_streak >= 3:
                print(f"Arrêt anticipé pour k={k_value}, tous les fichiers restants seront marqués comme timeout.")
                times[k_value].extend([600] * (len(data_files) - i))  # Marquer les restants comme timeout
                clique_counts[k_value].extend([0] * (len(data_files) - i))  # Aucune clique trouvée
                break

            file_path = os.path.join(data_dir, data_file)
            adj_matrix, _ = read_dzn_to_adj_matrix(file_path)

            if len(node_counts) < len(data_files):
                node_counts.append(len(adj_matrix))  # Ajouter le nombre de nœuds (une fois par fichier)

            print(f"Recherche de cliques de taille {k_value} dans le graphe {data_file}")

            start_time = time.time()  # Temps de départ
            cliques = find_cliques_ortools(adj_matrix, k_value)
            end_time = time.time()  # Temps de fin

            time_taken = end_time - start_time
            if time_taken > 600:
                time_taken = 600

            times[k_value].append(time_taken)
            clique_counts[k_value].append(len(cliques))

            if time_taken >= 600:
                timeout_streak += 1
                print(f"Timeout détecté pour {data_file} avec k={k_value}. ({timeout_streak}/3)")
            else:
                timeout_streak = 0  # Réinitialiser si une solution est trouvée

            print(f"Temps écoulé : {time_taken:.4f} secondes pour {len(cliques)} cliques trouvées")

            # Écriture des résultats
            output_file = os.path.join(result_dir, f"ortools_cliques_{i+1}_k{k_value}.txt")
            with open(output_file, 'w') as f:
                for clique in cliques:
                    f.write(f"{clique}\n")
            print(f"Résultats écrits dans : {output_file}")

    return node_counts, times, clique_counts


def plot_results(node_counts, times, k_range, output_image_path):
    """
    Trace un nuage de points avec le nombre de nœuds (x), le temps de calcul (y),
    et colore les points en fonction de la taille de clique k.
    """
    
    plt.figure(figsize=(12, 8))

    # Mappage des couleurs par k
    colors = plt.cm.viridis_r
    color_map = {k: colors(i / (len(k_range) - 1)) for i, k in enumerate(k_range)}

    # Tracer les points pour chaque k
    for k in k_range:
        plt.scatter(node_counts, times[k], color=color_map[k], label=f'k={k}', s=100, alpha=0.7)

    # Ajouter des labels et un titre
    plt.xlabel('Nombre de nœuds dans le graphe', fontsize=12)
    plt.ylabel('Temps (secondes)', fontsize=12)
    plt.title('Résultats de la recherche de cliques pour différentes tailles de k', fontsize=14)

    # Ajouter une légende
    legend_elements = [Line2D([0], [0], marker='o', color='w', label=f'k={k}', 
                              markerfacecolor=color_map[k], markersize=10) for k in k_range]
    plt.legend(handles=legend_elements, title="Taille de clique (k)", fontsize=10)

    # Enregistrer le graphique
    plt.savefig(output_image_path)
    plt.show()

if __name__ == "__main__":
    # Utiliser k entre 2 et 6 inclus
    k_range = range(2, 7)

    node_counts, times, clique_counts = find_cliques_with_ortools(
        "kclique/data/graph_data", k_range, "kclique/data/flatzinc_instance"
    )

    # Spécifier le chemin pour sauvegarder l'image
    output_image_path = "cliques_plot_by_k.png"
    plot_results(node_counts, times, k_range, output_image_path)