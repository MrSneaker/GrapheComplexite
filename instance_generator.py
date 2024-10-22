import os
import random
import subprocess
import sys
import argparse

def generate_graph_data(n, k, output_file):
    """
    Génère un graphe de taille n et clique de taille k et l'écrit dans un fichier au format MiniZinc (.dzn).
    """
    with open(output_file, 'w') as f:
        f.write(f"n = {n};\n")
        f.write(f"k = {k};\n")
        f.write("edges = [|\n")  # Utilisation de | pour démarrer la matrice
        for i in range(n):
            row = ["true" if random.randint(0, 1) == 1 and i != j else "false" for j in range(n)]
            row_str = ", ".join(map(str, row[:-1])) + f", {row[-1]} |"  # Utiliser | à la fin de chaque ligne
            f.write("  " + row_str + ("\n" if i < n-1 else "\n"))
        f.write("|];\n")
        
def create_fzn(model_file, data_file, output_file):
    """
    Crée un fichier FZN en exécutant la commande MiniZinc.
    """
    command = [
        "minizinc", "-c", "--solver", "choco",
        model_file, data_file, "-o", output_file
    ]
    try:
        subprocess.run(command, check=True)
        print(f"Fichier FZN créé : {output_file}")
    except subprocess.CalledProcessError as e:
        print(f"Erreur lors de la création du fichier FZN : {e}")
        

def generate_graph_dataset(n_range, k_range, output_dir):
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
    
    i = 0
    for k in k_range:
        for n in n_range:

            output_file = os.path.join(output_dir, f"graph_{i+1}.dzn")
            
            generate_graph_data(n, k, output_file)
            
            print(f"Graphe {i+1} généré : {output_file} (n={n}, k={k}")
            i += 1
    return i


def main():
    parser = argparse.ArgumentParser(description='Générer un dataset de graphes pour MiniZinc')
    parser.add_argument('--num_graphs', type=int, default=10, help='Nombre de graphes à générer')
    parser.add_argument('--n_max', type=int, default=10, help='Intervalle de taille des graphes (n min, n max)')
    parser.add_argument('--k_range', type=int, nargs=2, default=[2, 5], help='tailles possible des cliques k')
    parser.add_argument('--output_dir', type=str, default='graphs_dataset', help='Répertoire de sortie pour les fichiers de graphe (.dzn)')
    parser.add_argument('--model', type=str, required=True, help='Chemin vers le fichier modèle MiniZinc (.mzn)')
    parser.add_argument('--fzn_output_dir', type=str, default='fzn_outputs', help='Répertoire de sortie pour les fichiers FZN')
    
    args = parser.parse_args()
    
    step = int((args.n_max / args.num_graphs))
    
    print(step)
    
    n_range = [n for n in range(10, args.n_max, step)]
    
    k_range = [k for k in range(args.k_range[0], args.k_range[1] + 1, 1)]

    nb_graph = generate_graph_dataset(n_range, k_range, args.output_dir)
    
    if not os.path.exists(args.fzn_output_dir):
        os.makedirs(args.fzn_output_dir)
    
    for i in range(nb_graph):
        data_file = os.path.join(args.output_dir, f"graph_{i+1}.dzn")
        fzn_output_file = os.path.join(args.fzn_output_dir, f"output_{i+1}.fzn")
        create_fzn(args.model, data_file, fzn_output_file)

if __name__ == "__main__":
    main()