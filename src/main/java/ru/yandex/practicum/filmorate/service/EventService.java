package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.repository.DbEventRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    private final DbEventRepository eventRepository;
    private final UserRepository userRepository;

    public List<Event> getAll(Integer userId) {
        log.debug("getAll, userId = {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Юзер с id = " + userId + " не найден");
        }
        return eventRepository.findAllByUserId(userId, "asc");

    }
}
