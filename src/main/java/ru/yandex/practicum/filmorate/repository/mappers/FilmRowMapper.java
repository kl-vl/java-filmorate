package ru.yandex.practicum.filmorate.repository.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
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

    private final RowMapper<Genre> genreRowMapper;
    private final RowMapper<Mpa> mpaRowMapper;

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
                .build();

        if (rs.getObject("genre_id") != null) {
            Genre genre = genreRowMapper.mapRow(rs, rowNum);
            film.getGenres().add(genre);
        }

        return film;
    }
}