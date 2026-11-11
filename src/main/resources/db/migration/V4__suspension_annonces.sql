-- Nouveau statut : une annonce trop signalée est masquée automatiquement en attendant la modération.
ALTER TABLE annonce DROP CONSTRAINT annonce_statut_check;
ALTER TABLE annonce ADD CONSTRAINT annonce_statut_check
    CHECK (statut IN ('ACTIVE', 'VENDUE', 'EXPIREE', 'SUPPRIMEE', 'SUSPENDUE'));

INSERT INTO parametre (cle, valeur, description) VALUES
 ('seuil_suspension_signalements', '3', 'Nombre de signalements ouverts qui masque automatiquement une annonce');
