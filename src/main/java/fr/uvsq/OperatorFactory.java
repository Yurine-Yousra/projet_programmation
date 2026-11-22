package fr.uvsq;

import com.google.gson.JsonObject;

/**
 * Construit un opérateur à partir de son noeud JSON.
 */
@FunctionalInterface
public interface OperatorFactory {

  /**
   * Crée l'opérateur décrit par le noeud.
   *
   * @param node noeud JSON de l'opérateur
   * @param builder sert à construire les sous-plans
   * @return l'opérateur
   */
  Operator create(JsonObject node, PlanBuilder builder);
}
