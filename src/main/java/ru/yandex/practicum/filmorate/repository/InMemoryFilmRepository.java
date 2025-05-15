package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.IllegalFilmException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class InMemoryFilmRepository implements FilmRepository {
    private final Map<Integer, Film> storage = new HashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);


    @Override
    public Film create(Film film) {
        if (film.getId() != null) {
            throw new IllegalFilmException("New film should not have ID");
        }
        film.setId(idCounter.getAndIncrement());
        storage.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == null) {
            throw new IllegalFilmException("Film ID must be provided for update");
        }
        if (!storage.containsKey(film.getId())) {
            throw new FilmNotFoundException("The Film with %s not found".formatted(film.getId()));
        }
        storage.put(film.getId(), film);
        return film;
    }

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean existsById(Integer id) {
        return storage.containsKey(id);
    }

}
