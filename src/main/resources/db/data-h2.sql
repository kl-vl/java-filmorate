--del
DELETE FROM "film_genre";
DELETE FROM "film_like";
DELETE FROM "friendship";
DELETE FROM "user";
DELETE FROM "film";

-- clear ids
ALTER TABLE "film" ALTER COLUMN id RESTART WITH 1;
ALTER TABLE "user" ALTER COLUMN id RESTART WITH 1;

-- genre
MERGE INTO "genre" (id, name)
VALUES (1, 'Комедия'),
       (2, 'Драма'),
       (3, 'Мультфильм'),
       (4, 'Триллер'),
       (5, 'Документальный'),
       (6, 'Боевик');

-- rate
MERGE INTO "mpa" (id, name)
VALUES (1, 'G'),
       (2, 'PG'),
       (3, 'PG-13'),
       (4, 'R'),
       (5, 'NC-17');