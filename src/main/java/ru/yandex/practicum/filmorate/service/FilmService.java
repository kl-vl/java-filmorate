package ru.yandex.practicum.filmorate.service;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.exception.FilmCreateFailed;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.GenreValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.DbDirectorRepository;
import ru.yandex.practicum.filmorate.repository.FilmRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmRepository filmRepository;
    private final DbDirectorRepository directorRepository;

    public Film getFilmById(Integer id) {
        return filmRepository.getById(id).orElseThrow(() -> new FilmNotFoundException("Film not found"));
    }


    public Film create(Film film) {
        log.info("Creating Film with name: {}", film.getName());

        film.setId(null);
        Film createdFilm = filmRepository.create(film)
                .orElseThrow(() -> new FilmCreateFailed("Film creation failed"));

        log.info("Film created successfully. ID : {}", createdFilm.getId());
        log.debug("Film created data: {}", createdFilm);

        return createdFilm;
    }

    public List<Film> getList() {
        log.debug("Getting films list");

        return filmRepository.findAll();
    }

    public Film update(Film film) throws FilmNotFoundException {
        log.info("Updating Film with ID: {}", film.getId());

        if (!filmRepository.existsById(film.getId())) {
            throw new FilmNotFoundException("The Film with %s not found".formatted(film.getId()));
        }

        Film updatedFilm = filmRepository.update(film).orElseThrow(() -> new FilmCreateFailed("Film update failed"));

        log.info("Film updated successfully. ID : {}", updatedFilm.getId());
        log.debug("Film updated data: {}", updatedFilm);

        return updatedFilm;
    }

    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        directorRepository.findById(directorId)
                .orElseThrow(() -> new DirectorNotFoundException("Director not found with id: " + directorId));

        return filmRepository.findFilmsByDirectorId(directorId, sortBy);
    }

    public void removeFilmById(Integer filmId) {
        log.info("Удаление фильма с ID: {}", filmId);

        if (filmId == null) {
            throw new FilmValidationException("ID фильма должно быть указано");
        }

        if (!filmRepository.removeFilmById(filmId)) {
            throw new FilmNotFoundException("Фильм с id { " + filmId + " } - не найден");
        }
        filmRepository.removeFilmById(filmId);

        log.info("Удаление фильма с ID: {} прошло успешно", filmId);
    }

    public Collection<Film> bestFilmsFromGenreAndYear(Integer year, Integer genreID) {
        log.info("Получены параметры запроса. Год релиза {}, ID жанра {}", year, genreID);

        if (year == null || year < 0) {
            throw new FilmValidationException("Год не может быть " + year);
        }

        if (genreID == null || genreID < 0) {
            throw new FilmValidationException("Жанр не может быть " + genreID);
        }


        Collection<Film> targetFilms = filmRepository.bestFilmsFromGenreAndYear(year, genreID);

        if (targetFilms.isEmpty()) {
            throw new FilmNotFoundException("Фильмы с указанными параметрами не найдены");
        }
        log.debug("Вернули {} фильмов", targetFilms.size());

        return targetFilms;
    }

    public Collection<Film> bestFilmsOfYear(Integer year) {
        log.info("Получили год {}", year);

        if (year == null || year < 0) {
            throw new FilmValidationException("Год не может быть " + year);
        }

        Collection<Film> films = filmRepository.bestFilmsOfYear(year);

        if (films.isEmpty()) {
            throw new FilmNotFoundException("За " + year + " год, фильмов нет");
        }
        log.debug("Вернули {} фильмов", films.size());

        return films;
    }

    public Collection<Film> bestFilmsOfGenre(Integer genreId) {
        if (genreId == null || genreId < 0) {
            throw new GenreValidationException("Жанр не может быть " + genreId);
        }

        Collection<Film> films = filmRepository.bestFilmsOfGenre(genreId);

        if (films.isEmpty()) {
            throw new FilmNotFoundException("Фильмы с id жанра " + genreId + " - не найдены");
        }
        log.debug("Вернули {} фильмов", films.size());

        return films;
    }

}
