# Rendu : Mini Moteur Algébrique étendu

## En bref

- Remplacement du `switch` dans `PlanBuilder` par un registre d'opérateurs.
- Utilisation des streams et séparation des opérateurs dans leurs propres classes.
- Ajout de `UNION`, `INTERSECTION`, `DIFFERENCE` et `AGGREGATE`.
- 67 tests JUnit 5, 95 % de couverture des lignes et aucune violation Checkstyle.

## 1. Modifications apportées au code

Au départ, la méthode `build()` de `PlanBuilder` utilisait un `switch` pour choisir l'opérateur à créer. Le problème était qu'il fallait modifier cette méthode à chaque ajout d'un opérateur.

J'ai remplacé le `switch` par un registre qui associe chaque type à une `OperatorFactory`. Par exemple, pour `UNION` :

```java
register("UNION", (node, b) -> new Union(b.input(node, "left"), b.input(node, "right")));
```

Avec cette solution, il suffit d'enregistrer le nouvel opérateur sans modifier la méthode `build()`. On se rapproche ainsi du principe ouvert/fermé (SOLID). Le test `PlanBuilderTest.nouvelOperateurAjouteSansModifierLaClasse` permet de vérifier ce fonctionnement.

J'ai gardé l'interface `Operator` car elle convenait déjà. Pour les trois opérations ensemblistes, j'ai créé une classe abstraite `SetOperation` afin d'éviter de répéter la vérification des schémas et la suppression des doublons dans chaque classe.

J'ai aussi apporté quelques modifications au reste du code :

- `Select`, `Project` et `Join` utilisent maintenant les streams, comme demandé dans l'énoncé.
- Pour la jointure, j'utilise une `HashMap` pour indexer la table de droite plutôt que de parcourir les deux tables avec une double boucle. La complexité passe en moyenne de O(n × m) à O(n + m + k), avec n et m les tailles des deux tables et k le nombre de lignes du résultat.
- `CSVUtils` a été renommée en `CsvUtils` pour respecter le style Google.
- Les méthodes `equals` et `hashCode` ont été ajoutées à `Row` pour pouvoir comparer les lignes.
- Le dossier `results` est créé automatiquement s'il n'existe pas.
- La lecture des CSV ignore les lignes vides et les espaces, et signale les lignes qui n'ont pas le bon nombre de valeurs.
- En cas de requête invalide, un message d'erreur est affiché plutôt qu'une trace Java.

## 2. Nouveaux opérateurs

| Opérateur | Champs JSON | Fonctionnement |
|---|---|---|
| `UNION` | `left`, `right` | Regroupe les lignes des deux tables (gauche puis droite) sans doublon |
| `INTERSECTION` | `left`, `right` | Garde les lignes communes aux deux tables |
| `DIFFERENCE` | `left`, `right` | Garde les lignes de gauche qui ne sont pas dans la table de droite |
| `AGGREGATE` | `input`, `function`, `column`, `groupBy`, `as` | Calcule une valeur par groupe |

Pour `AGGREGATE`, les fonctions disponibles sont `COUNT`, `SUM`, `AVG`, `MIN` et `MAX`. Le champ `groupBy` est facultatif.

Voici un exemple pour calculer le montant total des commandes par utilisateur :

```json
{"type": "AGGREGATE", "function": "SUM", "column": "amount", "groupBy": ["user_id"],
 "as": "total", "input": {"type": "SCAN", "table": "orders"}}
```

Pour les opérations ensemblistes, deux lignes sont considérées comme identiques si elles contiennent les mêmes valeurs dans le même ordre (`Row.equals`). Les deux tables doivent également avoir les mêmes colonnes dans le même ordre, sinon une `SchemaMismatchException` est levée.

## 3. Tests et validation

Pour tester les opérateurs, j'ai utilisé les fichiers CSV suivants dans le dossier `data/` :

```
users.csv            orders.csv
id,name,age          id,user_id,amount
1,Alice,22           10,2,100
2,Bob,30             11,1,150
3,Charlie,27         12,2,200
4,David,19           13,3,50
```

Dans `ExecutePlanTest`, chaque plan du dossier `plans/` est exécuté et son résultat est comparé au fichier attendu dans `expected/` (ou à l'erreur attendue).

| Plan | Requête | Résultat attendu |
|---|---|---|
| `plan_union` | `age < 28` UNION `age > 20` | Alice, Charlie, David, Bob |
| `plan_intersection` | `age < 28` INTERSECTION `age > 20` | Alice, Charlie |
| `plan_difference` | `age < 28` DIFFERENCE `age > 20` | David |
| `plan_aggregate` | SUM(amount) par name après la jointure | Alice 150, Bob 300, Charlie 50 |
| `plan_schema_incompatible` | users UNION orders | `SchemaMismatchException` |

Voici une partie de la sortie obtenue avec `./mvnw clean package` :

```
[INFO] You have 0 Checkstyle violations.
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0 -- in fr.uvsq.AggregateTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 -- in fr.uvsq.UnionTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 -- in fr.uvsq.IntersectionTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 -- in fr.uvsq.DifferenceTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0 -- in fr.uvsq.ExecutePlanTest
[INFO] Tests run: 67, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Les 67 tests passent. Avec JaCoCo, j'obtiens une couverture de 95 % des lignes et de 83 % des branches. Le rapport est disponible dans `target/site/jacoco/index.html`.

## 4. Lancer le projet

Pour compiler le projet et exécuter, par exemple, le plan d'agrégation :

```bash
./mvnw clean package
java -jar target/td4_alg-1.0-SNAPSHOT-jar-with-dependencies.jar plans/plan_aggregate.json
```
