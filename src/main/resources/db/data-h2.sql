
--TRUNCATE TABLE rate RESTART IDENTITY;
--DELETE FROM rate;
--ALTER SEQUENCE rate_id_seq RESTART WITH 1;
--TRUNCATE TABLE genre RESTART IDENTITY;
--TRUNCATE TABLE friendship RESTART IDENTITY;
--TRUNCATE TABLE film_likes RESTART IDENTITY;
--TRUNCATE TABLE "user" RESTART IDENTITY;
--TRUNCATE TABLE film RESTART IDENTITY;

-- user
/*INSERT INTO "user" (login, name, email, birthday) VALUES
    ('john_doe', 'John Doe', 'john.doe@example.com', '1990-05-15'),
    ('jane_smith', 'Jane Smith', 'jane.smith@example.com', '1992-08-22'),
    ('michael_johnson', 'Michael Johnson', 'michael.johnson@example.com', '1985-11-03'),
    ('emily_williams', 'Emily Williams', 'emily.williams@example.com', '1988-04-30'),
    ('david_brown', 'David Brown', 'david.brown@example.com', '1995-07-18'),
    ('sarah_davis', 'Sarah Davis', 'sarah.davis@example.com', '1993-02-14'),
    ('robert_miller', 'Robert Miller', 'robert.miller@example.com', '1987-09-25'),
    ('jennifer_wilson', 'Jennifer Wilson', 'jennifer.wilson@example.com', '1991-12-05'),
    ('william_taylor', 'William Taylor', 'william.taylor@example.com', '1989-06-20'),
    ('linda_anderson', 'Linda Anderson', 'linda.anderson@example.com', '1994-03-08'),
    ('james_thomas', 'James Thomas', 'james.thomas@example.com', '1996-10-11'),
    ('patricia_martin', 'Patricia Martin', 'patricia.martin@example.com', '1986-01-17'),
    ('christopher_garcia', 'Christopher Garcia', 'christopher.garcia@example.com', '1997-07-23'),
    ('mary_martinez', 'Mary Martinez', 'mary.martinez@example.com', '1998-04-19'),
    ('matthew_robinson', 'Matthew Robinson', 'matthew.robinson@example.com', '1984-12-31'),
    ('elizabeth_clark', 'Elizabeth Clark', 'elizabeth.clark@example.com', '1999-09-02'),
    ('daniel_rodriguez', 'Daniel Rodriguez', 'daniel.rodriguez@example.com', '1983-05-28'),
    ('jessica_lee', 'Jessica Lee', 'jessica.lee@example.com', '1992-11-15'),
    ('kevin_white', 'Kevin White', 'kevin.white@example.com', '1981-07-07'),
    ('susan_harris', 'Susan Harris', 'susan.harris@example.com', '1995-08-12');
*/

-- friendship

/*INSERT INTO friendship (user_id, friend_id, accepted)
VALUES
-- Пользователь 1 (John Doe) дружит с 2,3,4
(1, 2, true),
(1, 3, true),
(1, 4, true),

-- Пользователь 2 (Jane Smith) дружит с 3,5
(2, 3, true),
(2, 5, true),

-- Пользователь 3 (Michael Johnson) дружит с 4,6
(3, 4, true),
(3, 6, true),

-- Пользователь 4 (Emily Williams) дружит с 7
(4, 7, true),

-- Пользователь 5 (David Brown) дружит с 6,8
(5, 6, true),
(5, 8, true),

-- Пользователь 6 (Sarah Davis) дружит с 9
(6, 9, true),

-- Пользователь 7 (Robert Miller) дружит с 8,10
(7, 8, true),
(7, 10, true),

-- Пользователь 8 (Jennifer Wilson) дружит с 11
(8, 11, true),

-- Пользователь 9 (William Taylor) дружит с 10,12
(9, 10, true),
(9, 12, true),

-- Пользователь 10 (Linda Anderson) дружит с 13
(10, 13, true),

-- Неподтвержденные запросы дружбы
(11, 14, false),  -- James Thomas → Patricia Martin
(12, 15, false),  -- Patricia Martin → Christopher Garcia
(13, 16, false),  -- Christopher Garcia → Mary Martinez
(14, 17, false),  -- Mary Martinez → Matthew Robinson
(15, 18, false),  -- Matthew Robinson → Elizabeth Clark
(16, 19, false),  -- Elizabeth Clark → Daniel Rodriguez
(17, 20, false),  -- Daniel Rodriguez → Jessica Lee
(18, 1, false),   -- Jessica Lee → John Doe
(19, 2, false),   -- Kevin White → Jane Smith
(20, 3, false)    -- Susan Harris → Michael Johnson
    ON CONFLICT (user_id, friend_id) DO NOTHING;
*/

-- film
/*INSERT INTO film (name, description, release_date, duration, rate_id) VALUES
    ('The Shawshank Redemption', 'Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.', '1994-09-23', 142, 4),
    ('The Godfather', 'The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant son.', '1972-03-24', 175, 4),
    ('The Dark Knight', 'When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice.', '2008-07-18', 152, 4),
    ('Pulp Fiction', 'The lives of two mob hitmen, a boxer, a gangster and his wife, and a pair of diner bandits intertwine in four tales of violence and redemption.', '1994-10-14', 154, 4),
    ('Fight Club', 'An insomniac office worker and a devil-may-care soapmaker form an underground fight club that evolves into something much, much more.', '1999-10-15', 139, 4),
    ('Inception', 'A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O.', '2010-07-16', 148, 3),
    ('The Matrix', 'A computer hacker learns from mysterious rebels about the true nature of his reality and his role in the war against its controllers.', '1999-03-31', 136, 3),
    ('Forrest Gump', 'The presidencies of Kennedy and Johnson, the events of Vietnam, Watergate, and other historical events unfold through the perspective of an Alabama man with an IQ of 75.', '1994-07-06', 142, 3),
    ('The Lion King', 'Lion prince Simba and his father are targeted by his bitter uncle, who wants to ascend the throne himself.', '1994-06-24', 88, 1),
    ('Toy Story', 'A cowboy doll is profoundly threatened and jealous when a new spaceman figure supplants him as top toy in a boy''s room.', '1995-11-22', 81, 1),
    ('Spirited Away', 'During her family''s move to the suburbs, a sullen 10-year-old girl wanders into a world ruled by gods, witches, and spirits, and where humans are changed into beasts.', '2001-07-20', 125, 2),
    ('Parasite', 'Greed and class discrimination threaten the newly formed symbiotic relationship between the wealthy Park family and the destitute Kim clan.', '2019-05-21', 132, 4),
    ('Interstellar', 'A team of explorers travel through a wormhole in space in an attempt to ensure humanity''s survival.', '2014-11-07', 169, 3),
    ('The Avengers', 'Earth''s mightiest heroes must come together and learn to fight as a team if they are going to stop the mischievous Loki and his alien army from enslaving humanity.', '2012-05-04', 143, 3),
    ('Joker', 'In Gotham City, mentally troubled comedian Arthur Fleck is disregarded and mistreated by society. He then embarks on a downward spiral of revolution and bloody crime.', '2019-10-04', 122, 4),
    ('The Social Network', 'As Harvard student Mark Zuckerberg creates the social networking site that would become known as Facebook, he is sued by the twins who claimed he stole their idea.', '2010-10-01', 120, 3),
    ('La La Land', 'While navigating their careers in Los Angeles, a pianist and an actress fall in love while attempting to reconcile their aspirations for the future.', '2016-12-09', 128, 3),
    ('Whiplash', 'A promising young drummer enrolls at a cut-throat music conservatory where his dreams of greatness are mentored by an instructor who will stop at nothing to realize a student''s potential.', '2014-10-10', 106, 3),
    ('Coco', 'Aspiring musician Miguel, confronted with his family''s ancestral ban on music, enters the Land of the Dead to find his great-great-grandfather, a legendary singer.', '2017-11-22', 105, 2),
    ('The Grand Budapest Hotel', 'The adventures of Gustave H, a legendary concierge at a famous hotel, and Zero Moustafa, the lobby boy who becomes his most trusted friend.', '2014-03-28', 99, 3);
*/
-- film_likes

/*DO $$
DECLARE
user_id INT;
    film_id INT;
    likes_count INT;
BEGIN
FOR user_id IN 1..20 LOOP
        likes_count := 3 + floor(random() * 8)::INT; -- От 3 до 10 лайков

FOR i IN 1..likes_count LOOP
SELECT f.id INTO film_id
FROM film f
WHERE NOT EXISTS (
    SELECT 1 FROM film_likes fl
    WHERE fl.user_id = user_id AND fl.film_id = f.id
)
ORDER BY random()
    LIMIT 1;


-- Если нашли фильм - добавляем лайк
IF film_id IS NOT NULL THEN
                INSERT INTO film_likes (user_id, film_id) VALUES (user_id, film_id);
END IF;
END LOOP;
END LOOP;
END $$;
*/

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