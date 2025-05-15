package ru.yandex.practicum.filmorate.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService service;

    @GetMapping
    public Collection<Film> getList() {
        log.debug("Getting films list");

        return service.getList();
    }

    @PostMapping
    public ResponseEntity<Film> create(@RequestBody @Valid Film film, HttpServletResponse response) {
        log.info("Creating film: {}", film);

        if (film.getId() != null) {
            response.addHeader("Warning", "Server ignored client-provided ID");
        }
        Film createdFilm = service.create(film);

        log.debug("Successfully created film ID: {}", film.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdFilm);
    }

    @PutMapping
    public Film update(@RequestBody @Valid Film film) {
        log.info("Updating film: {}", film);

        try {
            Film updatedFilm = service.update(film);

            log.debug("Successfully updated film ID: {}", film.getId());

            return updatedFilm;
        } catch (FilmNotFoundException e) {
            log.warn("Attempt to update non-existent film ID: {}", film.getId());

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Film not found", e);
        }
    }
}
