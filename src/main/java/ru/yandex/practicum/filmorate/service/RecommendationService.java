package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.DbFilmRepository;
import java.util.Collections;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final UserService userService;
    private final DbFilmRepository dbFilmRepository;

    public RecommendationService(UserService userService, DbFilmRepository filmRepository) {
        this.userService = userService;
        this.dbFilmRepository = filmRepository;
    }

    public List<Film> recommendFor(int userId) {
        userService.getUserById(userId);
        Optional<Integer> neighborOpt = dbFilmRepository.findBestNeighborId(userId);
        if (neighborOpt.isEmpty()) {
            return Collections.emptyList();
        }
        List<Film> raw = dbFilmRepository.recommendFromNeighbor(userId, neighborOpt.get(), 1);
        return raw.stream()
                .map(r -> dbFilmRepository.getById(r.getId()).orElse(r))
                .collect(Collectors.toList());
    }
}
