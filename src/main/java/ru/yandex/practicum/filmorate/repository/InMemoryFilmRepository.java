package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InMemoryFilmRepository implements FilmRepository {
    private final AtomicInteger idCounter = new AtomicInteger(1);
    private final Map<Integer, Film> films = new ConcurrentHashMap<>();
    private final Map<Integer, Set<Integer>> likes = new ConcurrentHashMap<>();
    private final UserRepository userRepository;

    @Override
    public Optional<Film> create(Film film) {
        if (film.getId() != null) {
            throw new FilmValidationException("New film should not have ID");
        }
        film.setId(idCounter.getAndIncrement());
        films.put(film.getId(), film);
        return Optional.of(film);
    }

    @Override
    public Optional<Film> update(Film film) {
        if (film.getId() == null) {
            throw new FilmValidationException("Film ID must be provided for update");
        }
        if (!existsById(film.getId())) {
            throw new FilmNotFoundException("The Film with ID=%s not found".formatted(film.getId()));
        }
        films.put(film.getId(), film);
        return Optional.of(film);
    }

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public boolean existsById(Integer id) {
        return films.containsKey(id);
    }

    @Override
    public Optional<Film> getById(Integer id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public boolean addLike(Integer filmId, Integer userId) {
        if (!films.containsKey(filmId)) {
            throw new FilmNotFoundException("Film with id " + filmId + " not found");
        }

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User with id " + userId + " not found");
        }

        likes.computeIfAbsent(filmId, k -> ConcurrentHashMap.newKeySet()).add(userId);
        return true;
    }

    @Override
    public boolean removeLike(Integer filmId, Integer userId) {
        if (!likes.containsKey(filmId)) {
            return false;
        }
        return likes.get(filmId).remove(userId);
    }

    public boolean hasLike(Integer filmId, Integer userId) {
        return likes.getOrDefault(filmId, Collections.emptySet()).contains(userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        return likes.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
                .limit(count)
                .map(entry -> films.get(entry.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        return List.of();
    }

    @Override
    public List<Film> findFilmsByDirectorId(int directorId, String sortBy) {
        throw new UnsupportedOperationException("This method is not implemented.");
    }

}