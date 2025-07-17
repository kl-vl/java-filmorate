package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.enums.FilmSearchBy;

import java.util.Arrays;

@Data
@NoArgsConstructor
public class SearchCriteria {
    private String query;
    private FilmSearchBy filmSearchBy;

    public SearchCriteria(String query, FilmSearchBy filmSearchBy) {
        this.query = query.toLowerCase();
        this.filmSearchBy = filmSearchBy;
    }

    public static SearchCriteria from(String query, String[] by) {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setQuery(query.toLowerCase());
        criteria.setFilmSearchBy(parseParams(by));
        return criteria;
    }

    private static FilmSearchBy parseParams(String[] params) {
        boolean searchTitle = Arrays.stream(params)
                .anyMatch(p -> p.equalsIgnoreCase("title"));
        boolean searchDirector = Arrays.stream(params)
                .anyMatch(p -> p.equalsIgnoreCase("director"));

        return searchTitle && searchDirector ? FilmSearchBy.BOTH :
                searchDirector ? FilmSearchBy.DIRECTOR :
                        FilmSearchBy.TITLE;
    }

}
