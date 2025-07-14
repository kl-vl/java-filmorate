package ru.yandex.practicum.filmorate.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.EventService;
import ru.yandex.practicum.filmorate.service.FriendsService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;


@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FriendsService friendsService;
    private final EventService eventService;

    public record FriendshipResponse(Integer user1Id, Integer user2Id, String status) {
    }

    @GetMapping
    public Collection<User> getList() {
        return userService.getList();
    }

    @PostMapping
    public User create(@RequestBody @Valid User user, HttpServletResponse response) {
        if (user.getId() != null) {
            response.addHeader("Warning", "Server ignored client-provided ID");
        }
        return userService.create(user);
    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable Integer userId) {
        return userService.getUserById(userId);
    }

    @PutMapping
    public User update(@RequestBody @Valid User user) {
        return userService.update(user);
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public FriendshipResponse addFriend(@PathVariable Integer userId, @PathVariable Integer friendId) {
        friendsService.addFriend(userId, friendId);
        return new FriendshipResponse(userId, friendId, "FRIENDS");
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFriend(@PathVariable Integer userId, @PathVariable Integer friendId) {
        friendsService.removeFriend(userId, friendId);
    }

    @GetMapping("/{userId}/friends")
    public Collection<User> getFriends(@PathVariable Integer userId) {
        return friendsService.getFriends(userId);
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable Integer userId, @PathVariable Integer otherId) {
        return friendsService.getCommonFriends(userId, otherId);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUserById(@PathVariable Integer userId) {
        userService.removeUserById(userId);
    }

    @GetMapping("/{userId}/feed")
    public Collection<Event> getEvents(@PathVariable Integer userId) {
        return eventService.getAll(userId);
    }

}