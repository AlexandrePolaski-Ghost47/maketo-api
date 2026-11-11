-- Réordonner les photos (après une suppression) décale plusieurs lignes dans la même transaction :
-- la contrainte d'unicité n'est vérifiée qu'au COMMIT, une fois tous les décalages faits.
ALTER TABLE photo_annonce DROP CONSTRAINT uk_photo_ordre;
ALTER TABLE photo_annonce
    ADD CONSTRAINT uk_photo_ordre UNIQUE (annonce_id, ordre) DEFERRABLE INITIALLY DEFERRED;
