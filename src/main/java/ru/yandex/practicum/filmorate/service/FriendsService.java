package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.enums.EventOperation;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.DbEventRepository;
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
    private final DbEventRepository eventRepository;


    public boolean addFriend(Integer userId, Integer friendId) {
        if (userId.equals(friendId)) {
            throw new IllegalStateException("User cannot add themselves as a friend");
        }

        User user = userRepository.getById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));

        User friend = userRepository.getById(friendId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + friendId + " not found"));

        if (userRepository.areFriends(userId, friendId)) {
            throw new IllegalStateException("Users are already friends");
        }

        userRepository.addFriend(userId, friendId);

        Event newEvent = Event.builder()
                .eventType(EventType.FRIEND)
                .operation(EventOperation.ADD)
                .userId(userId)
                .entityId(friendId)
                .build();
        Optional<Event> optEvent = eventRepository.addEvent(newEvent);

        return true;
    }

    public boolean removeFriend(Integer userId, Integer friendId) {
        if (userId.equals(friendId)) {
            throw new IllegalStateException("User cannot delete themselves from friend");
        }

        User user = userRepository.getById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));

        User friend = userRepository.getById(friendId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + friendId + " not found"));

        if (!userRepository.areFriends(user.getId(), friend.getId())) {
            return true;
        }

        userRepository.removeFriend(userId, friendId);
        userRepository.removeFriend(friendId, userId);

        Event newEvent = Event.builder()
                .eventType(EventType.FRIEND)
                .operation(EventOperation.REMOVE)
                .userId(userId)
                .entityId(friendId)
                .build();
        Optional<Event> optEvent = eventRepository.addEvent(newEvent);

        return true;
    }

    public List<User> getFriends(Integer userId) {
        User user = userRepository.getById(userId)
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
        User user = userRepository.getById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));
        User otherUser = userRepository.getById(otherId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + otherId + " not found"));

        return userRepository.getCommonFriends(user.getId(), otherUser.getId()).stream()
                .map(userRepository::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

}