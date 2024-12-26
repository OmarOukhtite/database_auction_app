SELECT u.email
FROM utilisateur u
JOIN offre o ON u.email = o.email
GROUP BY u.email
HAVING SUM(o.prixachat) = (
    -- Trouver le montant maximal des enchères
    SELECT MAX(total)
    FROM (
        SELECT SUM(o1.prixachat) AS total
        FROM offre o1
        GROUP BY o1.email
    ) max_totals
);
