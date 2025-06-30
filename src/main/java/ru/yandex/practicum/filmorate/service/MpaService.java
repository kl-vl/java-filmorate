package ru.yandex.practicum.filmorate.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.DbMpaRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaService {
    private final DbMpaRepository mpaRepository;

    public Mpa getMpaById(Integer id) {
        log.info("Getting mpa with id {}", id);

        return mpaRepository.getMpaById(id)
                .orElseThrow(() -> new MpaNotFoundException("Mpa with id=" + id + " not found"));
    }

    public List<Mpa> getAllMpa() {
        log.debug("Getting mpa list");

        return mpaRepository.getAllMpa();
    }
}
