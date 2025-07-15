package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmLikeService {
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;

    public boolean addLike(Integer filmId, Integer userId) {
        if (!filmRepository.existsById(filmId)) {
            throw new FilmNotFoundException("The Film with %s not found to like".formatted(filmId));
        }

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("The User with %s does not exists to like film".formatted(userId));
        }

        if (!filmRepository.addLike(filmId, userId)) {
            throw new IllegalStateException("User %s already liked the film with ID %s".formatted(userId, filmId));
        }
        return true;
    }

    public boolean removeLike(Integer filmId, Integer userId) {
        if (!filmRepository.existsById(filmId)) {
            throw new FilmNotFoundException("Film with ID %s not found to remove like".formatted(filmId));
        }

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User with ID %s not found to remove like".formatted(userId));
        }

        return filmRepository.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(Integer count, Integer year, Integer genreId) {
        return filmRepository.getPopularFilms(count, year, genreId);
    }

    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Пользователь с ID=" + userId + " не найден");
        }
        if (!userRepository.existsById(friendId)) {
            throw new UserNotFoundException("Пользователь с ID=" + friendId + " не найден");
        }
        return filmRepository.getCommonFilms(userId, friendId);
    }
}
