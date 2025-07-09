package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmCreateFailed;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.FilmRepository;

import java.util.*;
import javax.crypto.spec.OAEPParameterSpec;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmRepository repository;

    public Film getFilmById(Integer id) {
        return repository.getById(id).orElseThrow(() -> new FilmNotFoundException("Film not found"));
    }


    public Film create(Film film) {
        log.info("Creating Film with name: {}", film.getName());

        film.setId(null);
        Film createdFilm = repository.create(film)
                .orElseThrow(() -> new FilmCreateFailed("Film creation failed"));

        log.info("Film created successfully. ID : {}", createdFilm.getId());
        log.debug("Film created data: {}", createdFilm);

        return createdFilm;
    }

    public List<Film> getList() {
        log.debug("Getting films list");

        return repository.findAll();
    }

    public Film update(Film film) throws FilmNotFoundException {
        log.info("Updating Film with ID: {}", film.getId());

        if (!repository.existsById(film.getId())) {
            throw new FilmNotFoundException("The Film with %s not found".formatted(film.getId()));
        }

        Film updatedFilm = repository.update(film).orElseThrow(() -> new FilmCreateFailed("Film update failed"));;

        log.info("Film updated successfully. ID : {}", updatedFilm.getId());
        log.debug("Film updated data: {}", updatedFilm);

        return updatedFilm;
    }

    public void removeFilmById(Integer filmId) {
        if (filmId == null) {
            throw new ValidationException("ID фильма должно быть указано");
        }

        if (!repository.removeFilmById(filmId)) {
            throw new FilmNotFoundException("Фильм с id { " + filmId + " } - не найден");
        }
    }
}
