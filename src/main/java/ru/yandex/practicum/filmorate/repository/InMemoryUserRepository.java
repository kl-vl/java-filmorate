package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Repository

public class InMemoryUserRepository implements UserRepository {
    private final AtomicInteger idCounter = new AtomicInteger(1);
    private final Map<Integer, User> users = new HashMap<>();
    private final Map<Integer, Set<Friendship>> friendships = new ConcurrentHashMap<>();

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> create(User user) {
        if (user.getId() != null) {
            throw new UserValidationException("New User should not have ID");
        }
        user.setId(idCounter.getAndIncrement());
        users.put(user.getId(), user);
        return Optional.of(user);
    }

    @Override
    public Optional<User> update(User user) {
        if (user.getId() == null) {
            throw new UserValidationException("User ID must be provided for update");
        }
        if (!users.containsKey(user.getId())) {
            throw new UserNotFoundException("The User with %s not found".formatted(user.getId()));
        }
        users.put(user.getId(), user);
        return Optional.of(user);
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
    public boolean addFriend(Integer userId, Integer friendId) {
        Friendship friendship1 = new Friendship(userId, friendId);
        Friendship friendship2 = new Friendship(friendId, userId);

        friendships.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(friendship1);
        friendships.computeIfAbsent(friendId, k -> ConcurrentHashMap.newKeySet()).add(friendship2);

        return true;
    }

    @Override
    public boolean removeFriend(Integer userId, Integer friendId) {
        boolean removed1 = Optional.ofNullable(friendships.get(userId))
                .map(set -> set.removeIf(f -> f.getFriendId().equals(friendId)))
                .orElse(false);

        boolean removed2 = Optional.ofNullable(friendships.get(friendId))
                .map(set -> set.removeIf(f -> f.getFriendId().equals(userId)))
                .orElse(false);
        return removed1 || removed2;
    }

    public boolean acceptFriendship(Integer userId, Integer friendId) {
        boolean accepted1 = Optional.ofNullable(friendships.get(userId))
                .flatMap(set -> set.stream()
                        .filter(f -> f.getFriendId().equals(friendId))
                        .findFirst()
                        .map(f -> {
                            f.setAccepted(true);
                            return true;
                        }))
                .orElse(false);

        boolean accepted2 = Optional.ofNullable(friendships.get(friendId))
                .flatMap(set -> set.stream()
                        .filter(f -> f.getFriendId().equals(userId))
                        .findFirst()
                        .map(f -> {
                            f.setAccepted(true);
                            return true;
                        }))
                .orElse(false);

        return accepted1 && accepted2;
    }

    @Override
    public List<Integer> getFriends(Integer userId) {
        return Optional.ofNullable(friendships.get(userId))
                .orElse(Collections.emptySet())
                .stream()
                .filter(Friendship::getAccepted)
                .map(Friendship::getFriendId)
                .collect(Collectors.toList());
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
        return Optional.ofNullable(friendships.get(userId))
                .orElse(Collections.emptySet())
                .stream()
                .anyMatch(f -> f.getFriendId().equals(friendId) && f.getAccepted());
    }

    @Override
    public Optional<User> get(Integer id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public boolean removeUserById(Integer userId) {
        throw new UnsupportedOperationException();
    }
}
