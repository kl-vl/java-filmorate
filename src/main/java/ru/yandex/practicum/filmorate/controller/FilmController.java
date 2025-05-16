package ru.yandex.practicum.filmorate.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
        return service.getList();
    }

    @PostMapping
    public Film create(@RequestBody @Valid Film film, HttpServletResponse response) {
        if (film.getId() != null) {
            response.addHeader("Warning", "Server ignored client-provided ID");
        }
        return service.create(film);
    }

    @PutMapping
    public Film update(@RequestBody @Valid Film film) {
        return service.update(film);
    }
}
