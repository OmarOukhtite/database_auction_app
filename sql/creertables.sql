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
