package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.DbGenreRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {

    private final DbGenreRepository genreRepository;

    public Genre getGenreById(Integer id) {
        log.info("Getting genre with id {}", id);

        return genreRepository.findById(id)
                .orElseThrow(() -> new GenreNotFoundException("Genre with id=" + id + " not found"));
    }

    public List<Genre> getAllGenres() {
        log.debug("Getting genres list");

        return genreRepository.findAll();
    }
}
