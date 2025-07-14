package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.enums.EventOperation;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.DbEventRepository;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmLikeService {
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final DbEventRepository eventRepository;

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

        Event newEvent = Event.builder()
                .eventType(EventType.LIKE)
                .operation(EventOperation.ADD)
                .userId(userId)
                .entityId(filmId)
                .build();
        Optional<Event> optEvent = eventRepository.addEvent(newEvent);

        return true;
    }

    public boolean removeLike(Integer filmId, Integer userId) {
        if (!filmRepository.existsById(filmId)) {
            throw new FilmNotFoundException("Film with ID %s not found to remove like".formatted(filmId));
        }

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User with ID %s not found to remove like".formatted(userId));
        }

        boolean res = filmRepository.removeLike(filmId, userId);

        Event newEvent = Event.builder()
                .eventType(EventType.LIKE)
                .operation(EventOperation.REMOVE)
                .userId(userId)
                .entityId(filmId)
                .build();
        Optional<Event> optEvent = eventRepository.addEvent(newEvent);

        return res;
    }

    public List<Film> getPopularFilms(int count) {
        return filmRepository.getPopularFilms(count);
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
