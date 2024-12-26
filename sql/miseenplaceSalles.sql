-- dans ce qui suit, dans les requetes, les paramèttres sont à remplacer, ils devront etre des variables deja recuperees dans le code


-- Crearion de la salle de vente
INSERT INTO Salle (NumeroSalle, NomCategorie) VALUES (n, 'cat'); -- n est le nbre de salle + 1 (les numeros de salle commencent de 1), cat est donnee par l'utilisateur

-- Ajout de la vente
INSERT INTO Vente (NumeroSalle, NumeroVente, NumeroProduit, PrixDepart, Revocable, Montante, PlusieursOffresParUtilisateur, DateDebut, HeureDebut) VALUES
    (1, x, 1, 699.99, 'O', 'N', 'O', TO_TIMESTAMP('2023-04-15 10:30:00', 'YYYY-MM-DD HH24:MI:SS'));

-- si la vente est de duree limitee 
        INSERT INTO VenteDureeIllimitee (NumeroVente) VALUES (x);
-- si la vente est de duree illimitee
        INSERT INTO VenteDureeLimitee (NumeroVente, DateFin, HeureFin) VALUES
            (x, TO_TIMESTAMP('2023-04-30 23:59:59', 'YYYY-MM-DD HH24:MI:SS'));
