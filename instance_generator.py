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

def main():
    # Parser les arguments en ligne de commande
    parser = argparse.ArgumentParser(description='Générer une instance d\'un graphe pour MiniZinc')
    parser.add_argument('n', type=int, help='Nombre de sommets dans le graphe')
    parser.add_argument('k', type=int, help='Taille de la clique à rechercher')
    parser.add_argument('--output', type=str, default='large_graph.dzn', help='Nom du fichier de sortie (.dzn)')
    parser.add_argument('--model', type=str, required=True, help='Chemin vers le fichier modèle MiniZinc (.mzn)')
    parser.add_argument('--fzn_output', type=str, default='output.fzn', help='Nom du fichier de sortie (.fzn)')

    args = parser.parse_args()

    # Générer le graphe avec les paramètres n et k fournis
    generate_graph_data(args.n, args.k, args.output)

    print(f"Graphe généré avec {args.n} sommets et une clique de taille {args.k}. Fichier de sortie : {args.output}")
    
    create_fzn(args.model, args.output, args.fzn_output)


if __name__ == "__main__":
    main()