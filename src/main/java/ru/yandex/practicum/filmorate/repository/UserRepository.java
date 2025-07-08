package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> create(User user);

    Optional<User> update(User user);

    List<User> findAll();

    boolean existsById(Integer id);

    Optional<User> getById(Integer id);

    boolean addFriend(Integer userId, Integer friendId);

    boolean removeFriend(Integer userId, Integer friendId);

    List<Integer> getFriends(Integer userId);

    List<Integer> getCommonFriends(Integer userId1, Integer userId2);

    boolean areFriends(Integer userId, Integer friendId);

    Optional<User> get(Integer id);
}
