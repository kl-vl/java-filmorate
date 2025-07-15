package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.exception.FilmCreateFailed;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
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

}
