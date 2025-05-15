package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;

    public User create(User user) {
        user.setId(null);
        return repository.create(user);
    }

    public List<User> getList() {
        return repository.findAll();
    }

    public User update(User user) {
        if (!repository.existsById(user.getId())) {
            throw new UserNotFoundException("The User with %s does not exists".formatted(user.getId()));
        }
        return repository.update(user);
    }

}
