-- =========================================================
-- MAKETO - Données initiales (catégories, paramètres, offres)
-- =========================================================

INSERT INTO categorie (nom, slug, icone, ordre_affichage) VALUES
 ('Vêtements & chaussures',       'vetements',    'shirt',      1),
 ('Beauté & mèches',              'beaute',       'sparkles',   2),
 ('Nourriture & snacks',          'nourriture',   'utensils',   3),
 ('Téléphones & électronique',    'electronique', 'smartphone', 4),
 ('Livres, cours & fournitures',  'livres',       'book',       5),
 ('Logement & meubles',           'logement',     'home',       6),
 ('Services',                     'services',     'wrench',     7),
 ('Divers',                       'divers',       'box',        8);

INSERT INTO parametre (cle, valeur, description) VALUES
 ('duree_annonce_gratuite_jours', '7',          'Durée de visibilité d''une annonce gratuite'),
 ('duree_annonce_pro_jours',      '30',         'Durée de visibilité d''une annonce Pro'),
 ('quota_gratuit_mensuel',        '3',          'Nombre de publications gratuites par mois'),
 ('nb_max_photos',                '4',          'Nombre maximum de photos par annonce'),
 ('date_fin_periode_gratuite',    '2026-12-31', 'Avant cette date : pas de quota (lancement)');

INSERT INTO offre (code, libelle, type, prix, duree_heures) VALUES
 ('BOOST_24H',   'À la une pendant 24 h', 'BOOST',      100,  24),
 ('PRO_MENSUEL', 'Vendeur Pro - 1 mois',  'ABONNEMENT', 1000, 720);
