package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendsService {
    private final UserRepository userRepository;


    public boolean addFriend(Integer userId, Integer friendId) {
        if (userId.equals(friendId)) {
            throw new IllegalStateException("User cannot add themselves as a friend");
        }

        User user = userRepository.get(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));

        User friend = userRepository.get(friendId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + friendId + " not found"));

        if (userRepository.areFriends(userId, friendId)) {
            throw new IllegalStateException("Users are already friends");
        }

        userRepository.addFriend(userId, friendId);

        return true;
    }

    public boolean removeFriend(Integer userId, Integer friendId) {
        if (userId.equals(friendId)) {
            throw new IllegalStateException("User cannot delete themselves from friend");
        }

        User user = userRepository.get(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));

        User friend = userRepository.get(friendId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + friendId + " not found"));

        if (!userRepository.areFriends(user.getId(), friend.getId())) {
            return true;
        }

        userRepository.removeFriend(userId, friendId);
        userRepository.removeFriend(friendId, userId);
        return true;
    }

    public List<User> getFriends(Integer userId) {
        User user = userRepository.get(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));

        return userRepository.getFriends(user.getId()).stream()
                .map(userRepository::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Integer userId, Integer otherId) {
        if (userId.equals(otherId)) {
            return Collections.emptyList();
        }
        User user = userRepository.get(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));
        User otherUser = userRepository.get(otherId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + otherId + " not found"));

        return userRepository.getCommonFriends(user.getId(), otherUser.getId()).stream()
                .map(userRepository::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

}