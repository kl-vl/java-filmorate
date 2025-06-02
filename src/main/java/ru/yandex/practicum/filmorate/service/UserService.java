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
        log.debug("Creating user wil Login: {}", user.getLogin());

        user.setId(null);
        User createdUser = repository.create(user);

        log.info("Successfully created User with ID: {}", createdUser.getId());
        log.debug("User created data: {}", createdUser);

        return createdUser;
    }

    public List<User> getList() {
        log.debug("Getting users list");

        return repository.findAll();
    }

    public User update(User user) {
        log.info("Updating user with ID: {}", user.getId());

        if (!repository.existsById(user.getId())) {
            throw new UserNotFoundException("The User with ID %s does not exists".formatted(user.getId()));
        }
        User updatedUser = repository.update(user);

        log.info("User updated successfully. ID : {}", updatedUser.getId());
        log.debug("User updated data: {}", updatedUser);

        return updatedUser;
    }

}
