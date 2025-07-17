package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.SearchCriteria;

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

    List<Film> getPopularFilms(Integer count, Integer year, Integer genreId);

    List<Film> findFilmsByDirectorId(int directorId, String sortBy);

    List<Film> getCommonFilms(Integer userId, Integer friendId);

    boolean removeFilmById(Integer id);

    default List<Film> searchFilms(SearchCriteria criteria) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    List<Integer> findLikedFilmIdsByUser(Integer userId);

    int countLikesByFilmId(Integer filmId);
}
