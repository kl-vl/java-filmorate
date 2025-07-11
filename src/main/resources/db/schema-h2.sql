--- user
CREATE TABLE IF NOT EXISTS "user" (
    id SERIAL PRIMARY KEY,
    login VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    birthday DATE
);

-- friendship
CREATE TABLE IF NOT EXISTS "friendship" (
    user_id INTEGER REFERENCES "user"(id),
    friend_id INTEGER REFERENCES "user"(id),
    accepted BOOLEAN DEFAULT true,
    PRIMARY KEY (user_id, friend_id),
    CONSTRAINT chk_user_friend_order CHECK (user_id <> friend_id)
);

CREATE INDEX IF NOT EXISTS idx_friendship_accepted ON "friendship"(accepted);

-- mpa
CREATE TABLE IF NOT EXISTS "mpa" (
    id SERIAL PRIMARY KEY,
    name VARCHAR(5) NOT NULL UNIQUE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_mpa_name_unique ON "mpa"(name);

-- genre
CREATE TABLE IF NOT EXISTS "genre" (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_genre_name_unique ON "genre"(name);


-- film
CREATE TABLE IF NOT EXISTS "film" (
    id SERIAL PRIMARY KEY,
    mpa_id INTEGER REFERENCES "mpa"(id),
    name VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    release_date DATE NOT NULL,
    duration INTEGER NOT NULL CHECK (duration > 0),
    --likes_count INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_film_mpa FOREIGN KEY (mpa_id) REFERENCES "mpa"(id)
);

-- film_likes
CREATE TABLE IF NOT EXISTS "film_like" (
    user_id INTEGER REFERENCES "user"(id),
    film_id INTEGER REFERENCES "film"(id),
    PRIMARY KEY (user_id, film_id)
);

-- film_genre
CREATE TABLE IF NOT EXISTS "film_genre" (
    film_id INTEGER REFERENCES "film"(id),
    genre_id INTEGER REFERENCES "genre"(id),
    PRIMARY KEY (film_id, genre_id)
);


