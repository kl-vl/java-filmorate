package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.enums.SearchBy;

import java.util.Arrays;

@Data
@NoArgsConstructor
public class SearchCriteria {
    private String query;
    private SearchBy searchBy;

    public SearchCriteria(String query, SearchBy searchBy) {
        this.query = query.toLowerCase();
        this.searchBy = searchBy;
    }

    public static SearchCriteria from(String query, String[] by) {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setQuery(query.toLowerCase());
        criteria.setSearchBy(parseParams(by));
        return criteria;
    }

    private static SearchBy parseParams(String[] params) {
        boolean searchTitle = Arrays.stream(params)
                .anyMatch(p -> p.equalsIgnoreCase("title"));
        boolean searchDirector = Arrays.stream(params)
                .anyMatch(p -> p.equalsIgnoreCase("director"));

        return searchTitle && searchDirector ? SearchBy.BOTH :
                searchDirector ? SearchBy.DIRECTOR :
                        SearchBy.TITLE;
    }

}
