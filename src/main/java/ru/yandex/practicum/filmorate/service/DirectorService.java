package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DirectorCreateFailed;
import ru.yandex.practicum.filmorate.exception.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.repository.DbDirectorRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DbDirectorRepository directorRepository;

    public List<Director> getAllDirectors() {
        return directorRepository.findAll();
    }

    public Director getDirectorById(int id) {
        return directorRepository.findById(id)
                .orElseThrow(() -> new DirectorNotFoundException("Director with id=" + id + " not found"));
    }

    public Director createDirector(Director director) {
        log.debug("Creating director with name: {}", director.getName());

        director.setId(null);
        Director createdDirector = directorRepository.create(director).orElseThrow(() -> new DirectorCreateFailed("Director creation failed"));

        log.info("Successfully created Director with ID: {}", createdDirector.getId());
        log.debug("User created data: {}", createdDirector);

        return createdDirector;
    }

    public Director updateDirector(Director director) {
        log.info("Updating director with ID: {}", director.getId());

        if (!directorRepository.existsById(director.getId())) {
            throw new DirectorNotFoundException("The Director with ID %s does not exists".formatted(director.getId()));
        }
        Director updatedDirector = directorRepository.update(director).orElseThrow(() -> new DirectorCreateFailed("Director update failed"));

        log.info("Director updated successfully. ID : {}", updatedDirector.getId());
        log.debug("Director updated data: {}", updatedDirector);

        return updatedDirector;
    }

    public boolean deleteDirector(int id) {
        return directorRepository.deleteById(id);
    }
}
