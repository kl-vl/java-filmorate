package ru.yandex.practicum.filmorate.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.enums.DirectorSortBy;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.SearchCriteria;
import ru.yandex.practicum.filmorate.service.FilmLikeService;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;
    private final FilmLikeService likeService;

    @GetMapping
    public Collection<Film> getList() {
        return filmService.getList();
    }

    @GetMapping("/common")
    public Collection<Film> getCommonFilms(
            @RequestParam("userId") Integer userId,
            @RequestParam("friendId") Integer friendId) {
        return likeService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Integer id) {
        return filmService.getFilmById(id);
    }

    @PostMapping
    public Film create(@RequestBody @Valid Film film, HttpServletResponse response) {
        if (film.getId() != null) {
            response.addHeader("Warning", "Server ignored client-provided ID");
        }
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody @Valid Film film) {
        return filmService.update(film);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public boolean addLike(@PathVariable int filmId, @PathVariable int userId) {
        return likeService.addLike(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void removeLike(@PathVariable int filmId, @PathVariable int userId) {
        likeService.removeLike(filmId, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(@RequestParam(defaultValue = "10") Integer count,
                                            @RequestParam(required = false) Integer year,
                                            @RequestParam(required = false) Integer genreId) {
        return likeService.getPopularFilms(count, year, genreId);
    }

    @DeleteMapping("/{filmId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFilmById(@PathVariable(name = "filmId") Integer filmId) {
        filmService.removeFilmById(filmId);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getFilmsByDirector(
            @PathVariable int directorId,
            @RequestParam(defaultValue = "year") String sortBy) {

        DirectorSortBy directorSortBy = DirectorSortBy.fromString(sortBy)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid sort parameter. Allowed values: " + Arrays.toString(DirectorSortBy.values())
                ));

        return filmService.getFilmsByDirector(directorId, directorSortBy.name().toLowerCase());
    }

    @GetMapping("/search")
    public List<Film> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "title,director") String[] by) {

        return filmService.searchFilms(SearchCriteria.from(query, by));
    }

    @GetMapping("/popular?year={year}&genreId={genreId}")
    public Collection<Film> bestFilmsFromGenreAndYear(@PathVariable Integer year, @PathVariable Integer genreID) {
        return filmService.bestFilmsFromGenreAndYear(year, genreID);
    }

    @GetMapping("/popular?year={year}")
    public Collection<Film> bestFilmsOfYear(@PathVariable Integer year) {
        return filmService.bestFilmsOfYear(year);
    }

    @GetMapping("popular?genreId={genreId}")
    public Collection<Film> bestFilmsOfGenre(@PathVariable Integer genreId) {
        return filmService.bestFilmsOfGenre(genreId);
    }

}