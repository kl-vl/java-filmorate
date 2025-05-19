package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;

    public User create(User user) {
        log.debug("Creating user: {}", user);

        user.setId(null);
        User createdUser = repository.create(user);

        log.debug("Successfully created User with ID: {}", createdUser.getId());

        return createdUser;
    }

    public List<User> getList() {
        log.debug("Getting users list");

        return repository.findAll();
    }

    public User update(User user) {
        log.debug("Updating user with ID: {}", user.getId());

        if (!repository.existsById(user.getId())) {
            throw new UserNotFoundException("The User with %s does not exists".formatted(user.getId()));
        }
        User updatedUser = repository.update(user);

        log.debug("Successfully updated User: {}", updatedUser);

        return updatedUser;
    }

}
