package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.FilmRepository;

import java.util.List;


@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmRepository repository;

    public Film create(Film film) {
        film.setId(null);
        return repository.create(film);
    }

    public List<Film> getList() {
        return repository.findAll();
    }

    public Film update(Film film) throws FilmNotFoundException {
        if (!repository.existsById(film.getId())) {
            throw new FilmNotFoundException("The Film with %s not found".formatted(film.getId()));
        }
        return repository.update(film);
    }
}
