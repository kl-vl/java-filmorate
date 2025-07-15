package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.DbFilmRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для формирования рекомендаций фильмов на основе
 * простого бинарного коллаборативного фильтра (лайк/не лайк).
 * <p>
 * Алгоритм работы:
 * <ol>
 *     <li>Получаем все понравившиеся фильмы пользователя (лайки).</li>
 *     <li>Находим среди всех пользователей того, у кого максимальное число общих лайков (наиболее похожий пользователь).</li>
 *     <li>Формируем список кандидатов: фильмы, которые лайкнул похожий пользователь, но не лайкнул текущий.</li>
 *     <li>Сортируем кандидатов по общей популярности (числу лайков в базе данных).</li>
 * </ol>
 */
@Service
public class RecommendationService {

    private final UserService userService;
    private final DbFilmRepository dbFilmRepository;
    private final FilmService filmService;

    public RecommendationService(UserService userService,
                                 DbFilmRepository filmRepository,
                                 FilmService filmService) {
        this.userService = userService;
        this.dbFilmRepository = filmRepository;
        this.filmService = filmService;
    }

    /**
     * Возвращает список рекомендованных фильмов для просмотра.
     * <p>
     * Рекомендации строятся на основе пользователя, у которого максимальное
     * пересечение лайков с целевым пользователем. Фильмы, которые он уже лайкнул,
     * а целевой пользователь нет, сортируются по популярности (числу лайков)
     * и возвращаются.
     *
     * @param userId идентификатор пользователя, для которого нужны рекомендации
     * @return список рекомендованных {@link Film}; пустой список, если рекомендаций нет
     */
    public List<Film> recommendFor(Integer userId) {
        // Шаг 0: проверяем существование пользователя
        User me = userService.getUserById(userId);

        // Шаг 1: получаем список ID фильмов, которые лайкнул пользователь
        Set<Integer> myLikes = new HashSet<>(
                dbFilmRepository.findLikedFilmIdsByUser(userId)
        );

        // Шаг 2: ищем "соседа" с максимальным числом общих лайков
        int bestNeighborId = -1;
        int maxCommon = 0;
        for (User other : userService.getList()) {
            if (other.getId().equals(userId)) {
                continue;
            }
            Set<Integer> otherLikes = new HashSet<>(
                    dbFilmRepository.findLikedFilmIdsByUser(other.getId())
            );
            otherLikes.retainAll(myLikes);
            if (otherLikes.size() > maxCommon) {
                maxCommon = otherLikes.size();
                bestNeighborId = other.getId();
            }
        }

        // Если нет ни одного совпадения — возвращаем пустой список
        if (bestNeighborId < 0 || maxCommon == 0) {
            return Collections.emptyList();
        }

        // Шаг 3: формируем кандидатов для рекомендаций
        Set<Integer> neighborLikes = new HashSet<>(
                dbFilmRepository.findLikedFilmIdsByUser(bestNeighborId)
        );
        neighborLikes.removeAll(myLikes);

        // Шаг 4: сортируем кандидатов по убыванию популярности и конвертируем в объекты Film
        return neighborLikes.stream()
                .map(filmService::getFilmById)
                .sorted(Comparator.comparingInt(
                        f -> -dbFilmRepository.countLikesByFilmId(f.getId())
                ))
                .collect(Collectors.toList());
    }
}
