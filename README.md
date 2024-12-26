![Logo Ensimag](docs/ensimag.jpg)

# Projet de Base de Données : Gestion des Ventes aux Enchères

## Description
Ce projet implémente un système de gestion des ventes aux enchères basé sur une base de données relationnelle. Les fonctionnalités principales sont :
- Création de salles de vente et ajout de produits.
- Soumission d'offres sur des produits.
- Gestion des ventes limitées ou illimitées dans le temps.
- Sélection du gagnant d'une enchère.

Ce projet a été développé dans un cadre académique.

Pour plus de détails, consultez le répertoire `docs` où se trouvent le sujet et la documentation du projet.

---

## Structure du Projet
- **src/** : Fichiers source Java, incluant :
  - `Main.java` : Point d'entrée.
  - `DatabaseConnection.java` : Gestion JDBC.
  - Autres modules comme `Acheter.java` et `CreerSalle.java`.
- **sql/** : Scripts SQL :
  - `creertables.sql` : Création des tables.
  - `ajoutdedonnees.sql` : Peuplement initial.
  - `reinitialiser.sql` : Réinitialisation complète.
- **lib/** : Bibliothèque `ojdbc6.jar` pour Oracle.
- **docs/** : Documentation du projet et sujet détaillé.

---

## Prérequis
- Java JDK 8 ou supérieur.
- Base de données Oracle avec `ojdbc6.jar`.
- Système d'exploitation supportant JDBC.

---

## Installation
1. Clonez le dépôt :
   ```bash
   git clone <URL_du_dépôt>
   cd projet-bd-encheres
   ```
2. Configurez la base de données avec les scripts dans `sql/` :
   ```sql
   @creertables.sql;
   @ajoutdedonnees.sql;
   ```
3. Compilez avec `make` :
   ```bash
   make
   ```

---

## Utilisation
1. Lancez le programme principal :
   ```bash
   make exeMain
   ```
2. Connectez-vous avec un email valide.
3. Naviguez dans le menu pour créer des salles, enchérir ou consulter les résultats.

---

## Auteurs
- Mohamed Ziyad CHLIHI
- Omar OUKHTITE
- Yassine OUAGGASS
- Adil KASSAOUI

---

Pour tous les autres détails, référez-vous au répertoire `docs`. Bonne exploration !

