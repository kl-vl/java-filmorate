package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.IllegalFilmException;
import ru.yandex.practicum.filmorate.exception.IllegalUserException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private final Map<Integer, User> storage = new HashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);

    @Override
    public List<User> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public User create(User user) {
        if (user.getId() != null) {
            throw new IllegalUserException("New User should not have ID");
        }
        user.setId(idCounter.getAndIncrement());
        storage.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == null) {
            throw new IllegalUserException("User ID must be provided for update");
        }
        if (!storage.containsKey(user.getId())) {
            throw new UserNotFoundException("The User with %s not found".formatted(user.getId()));
        }
        storage.put(user.getId(), user);
        return user;
    }

    @Override
    public boolean existsById(Integer id) {
        return storage.containsKey(id);
    }

}
