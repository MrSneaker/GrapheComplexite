import networkx as nx
import matplotlib.pyplot as plt
import argparse


def visualize_graph(dzn_file):
    """
    Visualise un graphe défini dans un fichier .dzn avec NetworkX.
    
    Args:
        dzn_file (str): Chemin vers le fichier .dzn contenant la définition du graphe.
    """
    with open(dzn_file, 'r') as f:
        lines = f.readlines()
    
    n = 0
    edges_matrix = []
    parsing_edges = False

    for line in lines:
        line = line.strip()
        if line.startswith("n ="):
            n = int(line.split('=')[1].strip(';'))
        elif line.startswith("edges = [|"):
            parsing_edges = True
        elif parsing_edges:
            if line == "|];":
                break
            else:
                row = line.strip('|').strip().split(", ")
                edges_matrix.append([val.strip() == "true" for val in row])

    G = nx.Graph()
    G.add_nodes_from(range(n))
    
    for i in range(n):
        for j in range(i+1, n):
            if edges_matrix[i][j]:
                G.add_edge(i, j)
    
    plt.figure(figsize=(8, 8))
    pos = nx.kamada_kawai_layout(G)
    nx.draw_networkx_nodes(G, pos, node_size=500, node_color='skyblue')
    nx.draw_networkx_labels(G, pos, font_size=10)
    nx.draw_networkx_edges(G, pos, alpha=0.2, edge_color='gray') 
    plt.title(f"Graph Visualization (n={n})", fontsize=15)
    plt.show()

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Visualiser un graphe généré au format .dzn')
    parser.add_argument('--graph_file', type=str, required=True, help='Chemin du fichier .dzn à visualiser')
    args = parser.parse_args()
    
    
    visualize_graph(args.graph_file)
