package ru.yandex.practicum.filmorate.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping
    public Collection<User> getList() {
        log.debug("Getting users list");

        return service.getList();
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody @Valid User user, HttpServletResponse response) {
        log.info("Creating user: {}", user);

        if (user.getId() != null) {
            response.addHeader("Warning", "Server ignored client-provided ID");
        }

        User createdUser = service.create(user);

        log.debug("Successfully created user ID: {}", user.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping
    public User update(@RequestBody @Valid User user) {
        log.info("Updating user: {}", user);

        User updatedUser = service.update(user);

        log.debug("Successfully updated user ID: {}", user.getId());

        return updatedUser;
    }

}
