BEGIN
   FOR r IN (SELECT table_name FROM user_tables) LOOP
      EXECUTE IMMEDIATE 'DROP TABLE ' || r.table_name || ' CASCADE CONSTRAINTS';
   END LOOP;
END;
/


-- Creation de la table Utilisateur
CREATE TABLE Utilisateur (
 Email VARCHAR2(255) PRIMARY KEY,
 Nom VARCHAR2(100),
 Prenom VARCHAR2(100),
 Adresse VARCHAR2(255)
);

-- Creation de la table Categorie
CREATE TABLE Categorie (
 NomCategorie VARCHAR2(100) PRIMARY KEY,
 Description VARCHAR2(255)
);

-- Creation de la table Salle
CREATE TABLE Salle (
 NumeroSalle NUMBER PRIMARY KEY,
 NomCategorie VARCHAR2(100),
 CONSTRAINT fk_salle_a_pour_categorie FOREIGN KEY (NomCategorie) REFERENCES Categorie(NomCategorie)
);

-- Creation de la table Produit
CREATE TABLE Produit (
 NumeroProduit NUMBER PRIMARY KEY,
 NomProduit VARCHAR2(100),
 PrixRevient NUMBER,
 Stock NUMBER,
 NomCategorie VARCHAR2(100),
 CONSTRAINT fk_produit_appartient_a_categorie FOREIGN KEY (NomCategorie) REFERENCES Categorie(NomCategorie)
);

-- Creation de la table Caracteristique
CREATE TABLE Caracteristique (
 NomCaracteristique VARCHAR2(50),
 NumeroProduit NUMBER(10) NOT NULL,
 Valeur VARCHAR2(100),
 PRIMARY KEY (NomCaracteristique, NumeroProduit),
 CONSTRAINT fk_caracteristique_decrit_produit FOREIGN KEY (NumeroProduit) REFERENCES Produit(NumeroProduit)
);

-- Creation de la table Vente
CREATE TABLE Vente (
 NumeroVente NUMBER PRIMARY KEY,
 NumeroSalle NUMBER,
 NumeroProduit NUMBER,
 PrixDepart NUMBER,
 Revocable CHAR(1),
 Montante CHAR(1),
 PlusieursOffresParUtilisateur CHAR(1),
 DateDebut TIMESTAMP,
 CONSTRAINT fk_vente_a_lieu_dans_salle FOREIGN KEY (NumeroSalle) REFERENCES Salle(NumeroSalle),
 CONSTRAINT fk_vente_du_produit FOREIGN KEY (NumeroProduit) REFERENCES Produit(NumeroProduit)
);

-- Creation de la table VenteDureeLimitee
CREATE TABLE VenteDureeLimitee (
 NumeroVente NUMBER PRIMARY KEY,
 DateFin TIMESTAMP,
 CONSTRAINT fk_vdlimitee_vente FOREIGN KEY (NumeroVente) REFERENCES Vente(NumeroVente)
);

-- Creation de la table VenteDureeIllimitee
CREATE TABLE VenteDureeIllimitee (
 NumeroVente NUMBER PRIMARY KEY,
 CONSTRAINT fk_vdillimitee_vente FOREIGN KEY (NumeroVente) REFERENCES Vente(NumeroVente)
);

-- Creation de la table Offre
CREATE TABLE Offre (
 NumeroOffre NUMBER PRIMARY KEY,
 PrixAchat NUMBER,
 DateOffre TIMESTAMP,
 Quantite NUMBER,
 Email VARCHAR2(255),
 NumeroVente NUMBER,
 CONSTRAINT fk_offre_proposee_par_utilisateur FOREIGN KEY (Email) REFERENCES Utilisateur(Email),
 CONSTRAINT fk_offre_porte_sur_vente FOREIGN KEY (NumeroVente) REFERENCES Vente(NumeroVente)
);


-- Insertion des donnees dans la table Utilisateur
INSERT INTO Utilisateur (Email, Nom, Prenom, Adresse) VALUES ('omar.oukhtite@example.com', 'Oukhtite', 'Omar', '123 Main St');
INSERT INTO Utilisateur (Email, Nom, Prenom, Adresse) VALUES ('ziyad.chlihi@example.com', 'Chlihi', 'Ziyad', '456 Oak Rd');
INSERT INTO Utilisateur (Email, Nom, Prenom, Adresse) VALUES ('yassine.ouaggass@example.com', 'Ouaggass', 'Yassine', '789 Elm St');
INSERT INTO Utilisateur (Email, Nom, Prenom, Adresse) VALUES ('adil.kassaoui@example.com', 'Kassaoui', 'Adil', '321 Pine Ave');
INSERT INTO Utilisateur (Email, Nom, Prenom, Adresse) VALUES ('arsene.lupin@example.com', 'Lupin', 'Arsene', '654 Oak Blvd');

-- Insertion des donnees dans la table Categorie
INSERT INTO Categorie (NomCategorie, Description) VALUES ('Electronique', 'Produits electroniques grand public');
INSERT INTO Categorie (NomCategorie, Description) VALUES ('Ameublement', 'Meubles et articles de decoration');
INSERT INTO Categorie (NomCategorie, Description) VALUES ('Habillement', 'Vêtements et accessoires mode');
INSERT INTO Categorie (NomCategorie, Description) VALUES ('Livres', 'Ouvrages litteraires et de reference');
INSERT INTO Categorie (NomCategorie, Description) VALUES ('Jouets', 'Jeux et jouets pour enfants');

-- Insertion des donnees dans la table Produit
INSERT INTO Produit (NumeroProduit, NomProduit, PrixRevient, Stock, NomCategorie) VALUES (1, 'Televiseur HD 55"', 599, 25, 'Electronique');
INSERT INTO Produit (NumeroProduit, NomProduit, PrixRevient, Stock, NomCategorie) VALUES (2, 'Canape 3 places en cuir', 799, 15, 'Ameublement');
INSERT INTO Produit (NumeroProduit, NomProduit, PrixRevient, Stock, NomCategorie) VALUES (3, 'Robe de soiree noire', 149, 32, 'Habillement');
INSERT INTO Produit (NumeroProduit, NomProduit, PrixRevient, Stock, NomCategorie) VALUES (4, 'Encyclopedie des sciences', 59, 48, 'Livres');
INSERT INTO Produit (NumeroProduit, NomProduit, PrixRevient, Stock, NomCategorie) VALUES (5, 'Peluche ourson geant', 39, 18, 'Jouets');
INSERT INTO Produit (NumeroProduit, NomProduit, PrixRevient, Stock, NomCategorie) VALUES (6, 'Ordinateur portable i7', 899, 20, 'Electronique');
INSERT INTO Produit (NumeroProduit, NomProduit, PrixRevient, Stock, NomCategorie) VALUES (7, 'Chaise de bureau ergonomique', 199, 22, 'Ameublement');
INSERT INTO Produit (NumeroProduit, NomProduit, PrixRevient, Stock, NomCategorie) VALUES (8, 'Jean slim bleu fonce', 79, 41, 'Habillement');
INSERT INTO Produit (NumeroProduit, NomProduit, PrixRevient, Stock, NomCategorie) VALUES (9, 'Guide de cuisine vegetarienne', 24, 36, 'Livres');
INSERT INTO Produit (NumeroProduit, NomProduit, PrixRevient, Stock, NomCategorie) VALUES (10, 'Jeu de construction 3D', 49, 27, 'Jouets');

-- Insertion des donnees dans la table Caracteristique
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Taille ecran', 1, '55 pouces');
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Resolution', 1, '1920x1080');
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Technologie', 1, 'LED');
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Materiau', 2, 'Cuir');
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Couleur', 2, 'Brun fonce');
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Taille', 2, '3 places');
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Materiau', 3, 'Soie');
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Couleur', 3, 'Noir');
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Taille', 3, '38');
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Nb pages', 4, '1248');
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Domaine', 4, 'Sciences');
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Materiau', 5, 'Peluche');
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Taille', 5, '1 mètre');
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Processeur', 6, 'Intel Core i7');
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Ram', 6, '16 Go');
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Disque dur', 6, '1 To');
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Materiau', 7, 'Metal, cuir');
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Reglages', 7, 'Hauteur, inclinaison');
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Coupe', 8, 'Slim');
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Couleur', 8, 'Bleu fonce');
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Nb recettes', 9, '300');
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Regime', 9, 'Vegetarien');
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Materiau', 10, 'Plastique');
INSERT INTO Caracteristique (NomCaracteristique, NumeroProduit, Valeur) VALUES ('Nb pièces', 10, '245');