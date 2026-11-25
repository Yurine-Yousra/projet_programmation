package fr.uvsq;

import com.google.gson.JsonObject;

/** Construit un opérateur à partir de son noeud JSON. */
@FunctionalInterface
public interface OperatorFactory {

  /** Crée l'opérateur décrit par le noeud. */
  Operator create(JsonObject node, PlanBuilder builder);
}
