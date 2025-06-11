--- user
CREATE TABLE IF NOT EXISTS "user" (
    id SERIAL PRIMARY KEY,
    login VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    birthday DATE,
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_user_lower_login ON user (LOWER(login));
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_lower_email ON user (LOWER(email));

-- friendship
CREATE TABLE IF NOT EXISTS friendship (
    user_id INTEGER REFERENCES user(id),
    friend_id INTEGER REFERENCES user(id),
    accepted BOOLEAN DEFAULT false,
    PRIMARY KEY (user_id, friend_id),
    CONSTRAINT chk_user_friend_order CHECK (user_id <> friend_id)
);

CREATE INDEX IF NOT EXISTS idx_friendship_accepted ON friendship(accepted);

-- symmetric friendship on accept
CREATE OR REPLACE FUNCTION create_symmetric_friendship()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.accepted AND (OLD.accepted IS DISTINCT FROM NEW.accepted) THEN
        INSERT INTO friendship (user_id, friend_id, accepted)
        VALUES (NEW.friend_id, NEW.user_id, true)
        ON CONFLICT (user_id, friend_id) DO NOTHING;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_symmetric_friendship
    AFTER UPDATE ON friendship
    FOR EACH ROW EXECUTE FUNCTION create_symmetric_friendship();

CREATE OR REPLACE FUNCTION delete_symmetric_friendship()
RETURNS TRIGGER AS $$
BEGIN

-- symmetric friendship delete
DELETE FROM friendship
WHERE user_id = OLD.friend_id AND friend_id = OLD.user_id;

RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_delete_symmetric_friendship
    AFTER DELETE ON friendship
    FOR EACH ROW EXECUTE FUNCTION delete_symmetric_friendship();

-- film
CREATE TABLE IF NOT EXISTS film (
    id SERIAL PRIMARY KEY,
    rate_id INTEGER REFERENCES rate(id),
    name VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    release_date DATE NOT NULL,
    duration INTEGER NOT NULL CHECK (duration > 0),
    likes_count INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_film_rate FOREIGN KEY (rate_id) REFERENCES rate(id)
);

CREATE INDEX idx_film_likes_count ON film(likes_count);

-- update likes count for top
CREATE OR REPLACE FUNCTION update_film_likes_count()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
UPDATE film SET likes_count = likes_count + 1 WHERE id = NEW.film_id;
ELSIF (TG_OP = 'DELETE') THEN
UPDATE film SET likes_count = likes_count - 1 WHERE id = OLD.film_id;
END IF;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_film_likes_count
    AFTER INSERT OR DELETE ON film_likes
FOR EACH ROW EXECUTE FUNCTION update_film_likes_count();

-- film_likes
CREATE TABLE film_likes (
    user_id INTEGER REFERENCES user(id),
    film_id INTEGER REFERENCES film(id),
    PRIMARY KEY (user_id, film_id)
);

-- rate
CREATE TABLE IF NOT EXISTS rate (
    id SERIAL PRIMARY KEY,
    code VARCHAR(5) NOT NULL UNIQUE,
    name VARCHAR(50),
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_rate_code_unique ON genre (UPPER(code));

-- genre
CREATE TABLE IF NOT EXISTS genre (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_genre_name_unique ON genre (LOWER(name));

-- film_genre
CREATE TABLE IF NOT EXISTS film_genre (
    film_id INTEGER REFERENCES film(id),
    genre_id INTEGER REFERENCES genre(id),
    PRIMARY KEY (film_id, genre_id)
);