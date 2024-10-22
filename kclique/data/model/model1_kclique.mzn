include "alldifferent.mzn";

% Paramètres
int: n;              % Nombre de sommets
int: k;              % Taille de la clique
array[1..n, 1..n] of bool: edges; % Matrice d'adjacence du graphe

% Variables
array[1..k] of var 1..n: clique_nodes; % Les k sommets de la clique

% Contraintes
% Les sommets de la clique doivent être distincts
constraint alldifferent(clique_nodes);

% Tous les sommets de la clique doivent être connectés entre eux
constraint forall(i in 1..k, j in 1..k where i != j) (
    edges[clique_nodes[i], clique_nodes[j]] = true
);

% Résoudre si une k-clique existe
solve satisfy;