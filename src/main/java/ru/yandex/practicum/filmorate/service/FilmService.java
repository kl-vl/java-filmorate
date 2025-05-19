package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.FilmRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmRepository repository;

    public Film create(Film film) {
        log.debug("Creating Film: {}", film);

        film.setId(null);
        Film createdFilm = repository.create(film);

        log.debug("Successfully created Film with ID: {}", createdFilm.getId());

        return createdFilm;
    }

    public List<Film> getList() {
        log.debug("Getting films list");

        return repository.findAll();
    }

    public Film update(Film film) throws FilmNotFoundException {
        log.debug("Updating Film with ID: {}", film.getId());

        if (!repository.existsById(film.getId())) {
            log.debug("Film with {} not found", film.getId());
            throw new FilmNotFoundException("The Film with %s not found".formatted(film.getId()));
        }

        Film updatedFilm = repository.update(film);

        log.debug("Successfully updated film: {}", updatedFilm);

        return updatedFilm;
    }
}
