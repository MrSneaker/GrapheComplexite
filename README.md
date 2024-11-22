# GrapheComplexite

## Genération de graphe comportant des cliques

```bash
python3 instance_generator.py --num_graph 10 --n_max 300 --k_range 2 6 --step 5 --output_dir kclique/data/graph_data/ --model kclique/data/model/model1_kclique.mzn --fzn_output_dir kclique/data/flatzinc_instance/
```

Pour générer des instances basé sur les instances DIMACS:

```bash
python3 instance_generator.py --gen_dimacs True --k_range 7 20 --model kclique/data/model/model1_kclique.mzn
```

## Problème des k-cliques

Le problème est de trouver le nombre de k-cliques dans un graphe $G$ avec $n$ noeuds. On a donc notre espace de recherche de taille $2^n$.
