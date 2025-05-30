package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private final AtomicInteger idCounter = new AtomicInteger(1);
    private final Map<Integer, User> users = new HashMap<>();
    private final Map<Integer, Set<Integer>> friendsStorage = new ConcurrentHashMap<>();

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User create(User user) {
        if (user.getId() != null) {
            throw new UserValidationException("New User should not have ID");
        }
        user.setId(idCounter.getAndIncrement());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == null) {
            throw new UserValidationException("User ID must be provided for update");
        }
        if (!users.containsKey(user.getId())) {
            throw new UserNotFoundException("The User with %s not found".formatted(user.getId()));
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public boolean existsById(Integer id) {
        return users.containsKey(id);
    }

    @Override
    public Optional<User> getById(Integer id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> getAllById(List<Integer> ids) {
        return ids.stream().map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public boolean addFriend(Integer userId, Integer friendId) {
        friendsStorage.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        return true;
    }

    @Override
    public boolean removeFriend(Integer userId, Integer friendId) {
        if (!friendsStorage.containsKey(userId)) {
            return false;
        }
        return friendsStorage.get(userId).remove(friendId);
    }

    @Override
    public List<Integer> getFriends(Integer userId) {
        return new ArrayList<>(friendsStorage.getOrDefault(userId, Collections.emptySet()));
    }

    @Override
    public List<Integer> getCommonFriends(Integer userId, Integer otherId) {
        Set<Integer> userFriends = new HashSet<>(getFriends(userId));
        Set<Integer> otherFriends = new HashSet<>(getFriends(otherId));

        userFriends.retainAll(otherFriends);
        return new ArrayList<>(userFriends);
    }

    @Override
    public boolean areFriends(Integer userId, Integer friendId) {
        return friendsStorage.getOrDefault(userId, Collections.emptySet()).contains(friendId);
    }

    @Override
    public Optional<User> get(Integer id) {
        return Optional.ofNullable(users.get(id));
    }

}
