package ru.yandex.practicum.filmorate.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        return service.getList();
    }

    @PostMapping
    public User create(@RequestBody @Valid User user, HttpServletResponse response) {
        if (user.getId() != null) {
            response.addHeader("Warning", "Server ignored client-provided ID");
        }

        return service.create(user);
    }

    @PutMapping
    public User update(@RequestBody @Valid User user) {
        return service.update(user);
    }

}
