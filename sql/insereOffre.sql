-- insere une nouvelle offre dans la bd
INSERT INTO Offre (NumeroOffre, PrixAchat, DateOffre, HeureOffre, Quantite, Email, NumeroVente) VALUES
  (1, prix, TO_DATE('2023-04-16', 'YYYY-MM-DD'), TO_TIMESTAMP('2023-04-16 11:02:00', 'YYYY-MM-DD HH24:MI:SS'), 1, 'john.doe@example.com', 1001);
