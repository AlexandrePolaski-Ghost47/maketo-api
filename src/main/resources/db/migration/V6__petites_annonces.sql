-- =========================================================
-- Petites annonces : un simple message (« je donne des cours de maths en Terminale… »),
-- sans prix ni état. Les articles classiques gardent leurs règles.
-- =========================================================
ALTER TABLE annonce ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'ARTICLE'
    CHECK (type IN ('ARTICLE', 'MESSAGE'));

ALTER TABLE annonce ALTER COLUMN etat DROP NOT NULL;

ALTER TABLE annonce DROP CONSTRAINT chk_prix;
ALTER TABLE annonce ADD CONSTRAINT chk_contenu CHECK (
    (type = 'ARTICLE' AND etat IS NOT NULL AND (prix IS NOT NULL OR prix_a_debattre = TRUE))
    OR (type = 'MESSAGE' AND description IS NOT NULL)
);

CREATE INDEX idx_annonce_type ON annonce (type);

INSERT INTO categorie (nom, slug, icone, ordre_affichage) VALUES
 ('Petites annonces', 'petites-annonces', 'message', 0);
