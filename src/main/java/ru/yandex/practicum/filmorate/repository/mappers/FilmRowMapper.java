package ru.yandex.practicum.filmorate.repository.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.LinkedHashSet;

@Component
@RequiredArgsConstructor
public class FilmRowMapper implements RowMapper<Film> {

    private final RowMapper<Mpa> mpaRowMapper;
    private final RowMapper<Genre> genreRowMapper;
    private final RowMapper<Director> directorRowMapper;

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = Film.builder()
                .id(rs.getInt("film_id"))
                .name(rs.getString("film_name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(Duration.ofMinutes(rs.getInt("duration")))
                .mpa(mpaRowMapper.mapRow(rs, rowNum))
                .genres(new LinkedHashSet<>())
                .directors(new LinkedHashSet<>())
                .build();

        addRelatedEntities(rs, rowNum, film);

        return film;
    }

    public void addRelatedEntities(ResultSet rs, int rowNum, Film film) throws SQLException {
        addGenreIfPresent(rs, rowNum, film);
        addDirectorIfPresent(rs, rowNum, film);
    }

    private void addGenreIfPresent(ResultSet rs, int rowNum, Film film) throws SQLException {
        if (rs.getObject("genre_id") != null) {
            film.getGenres().add(genreRowMapper.mapRow(rs, rowNum));
        }
    }

    private void addDirectorIfPresent(ResultSet rs, int rowNum, Film film) throws SQLException {
        if (rs.getObject("director_id") != null) {
            film.getDirectors().add(directorRowMapper.mapRow(rs, rowNum));
        }
    }

}