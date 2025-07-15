package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmRepository {
    Optional<Film> create(Film film);

    Optional<Film> update(Film film);

    List<Film> findAll();

    boolean existsById(Integer id);

    Optional<Film> getById(Integer id);

    boolean addLike(Integer filmId, Integer userId);

    boolean removeLike(Integer filmId, Integer userId);

    List<Film> getPopularFilms(int count);

    List<Film> findFilmsByDirectorId(int directorId, String sortBy);

    List<Film> getCommonFilms(Integer userId, Integer friendId);

    boolean removeFilmById(Integer id);

    List<Integer> findLikedFilmIdsByUser(Integer userId);

    int countLikesByFilmId(Integer filmId);
}
