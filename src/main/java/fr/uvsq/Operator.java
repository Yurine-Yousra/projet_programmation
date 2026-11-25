package fr.uvsq;

/** Noeud d'un plan d'exécution : produit une table quand on l'évalue. */
public interface Operator {

  /** Évalue l'opérateur et renvoie la table résultat. */
  Table execute();
}
