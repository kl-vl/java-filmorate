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

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User with ID " + userId + " not found");
        }

        if (!userRepository.existsById(friendId)) {
            throw new UserNotFoundException("User with id " + friendId + " not found");
        }

        if (userRepository.areFriends(userId, friendId)) {
            throw new IllegalStateException("Users are already friends");
        }

        userRepository.addFriend(userId, friendId);
        userRepository.addFriend(friendId, userId);
        return true;
    }

    public boolean removeFriend(Integer userId, Integer friendId) {
        if (userId.equals(friendId)) {
            throw new IllegalStateException("User cannot delete themselves from friend");
        }

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User with ID " + userId + " not found");
        }

        if (!userRepository.existsById(friendId)) {
            throw new UserNotFoundException("User with ID " + friendId + " not found");
        }

        if (!userRepository.areFriends(userId, friendId)) {
            return true;
        }

        userRepository.removeFriend(userId, friendId);
        userRepository.removeFriend(friendId, userId);
        return true;
    }

    public List<User> getFriends(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User with id " + userId + " not found");
        }

        return userRepository.getFriends(userId).stream()
                .map(userRepository::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Integer userId, Integer otherId) {
        if (userId.equals(otherId)) {
            return Collections.emptyList();
        }

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User with id " + userId + " not found");
        }

        if (!userRepository.existsById(otherId)) {
            throw new UserNotFoundException("User with id " + otherId + " not found");
        }

        return userRepository.getCommonFriends(userId, otherId).stream()
                .map(userRepository::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

}