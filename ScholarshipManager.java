package logic;

import models.AcademicApplication;
import models.ResearchApplication;
import models.ScholarshipApplication;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Менеджер распределения стипендий.
 *
 * Вариант 21 - "Два направления":
 *   у студента есть учебная И научная заявки с баллом не ниже 60;
 *   рейтинг - полусумма лучших баллов по двум типам.
 *   Побеждают не более limit студентов с наибольшим рейтингом;
 *   при равенстве - меньший studentId.
 */
public class ScholarshipManager<T extends ScholarshipApplication> {

    private static final double PASS_SCORE = 60.0;

    // Predicate: условие допуска для одной заявки (балл не ниже 60)
    private final Predicate<ScholarshipApplication> passing =
            application -> application.calculateScore() >= PASS_SCORE;

    // Comparator участников (студент -> рейтинг): рейтинг по убыванию, затем studentId по возрастанию
    private static final Comparator<Map.Entry<Integer, Double>> PARTICIPANT_ORDER = (a, b) -> {
        int byRating = Double.compare(b.getValue(), a.getValue());
        return byRating != 0 ? byRating : Integer.compare(a.getKey(), b.getKey());
    };

    // Comparator заявок одного студента: балл по убыванию, затем id по возрастанию
    private static final Comparator<ScholarshipApplication> APPLICATION_ORDER =
            Comparator.comparingDouble(ScholarshipApplication::calculateScore).reversed()
                    .thenComparingInt(ScholarshipApplication::getId);

    private final List<T> applications = new ArrayList<>();

    /**
     * Добавляет заявку. При null или повторном id возвращает false без изменений.
     * При успехе сбрасывает предыдущие результаты назначения.
     */
    public boolean add(T application) {
        if (application == null) {
            return false;
        }
        boolean duplicate = applications.stream().anyMatch(a -> a.getId() == application.getId());
        if (duplicate) {
            return false;
        }
        applications.add(application);
        applications.forEach(a -> a.setAwarded(false));
        return true;
    }

    /**
     * Проводит конкурс. Возвращает победившие заявки в порядке рейтинга студентов.
     */
    public List<T> allocate(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit не может быть отрицательным: " + limit);
        }

        // повторный конкурс полностью заменяет предыдущий результат
        applications.forEach(a -> a.setAwarded(false));

        Map<Integer, List<T>> byStudent = groupByStudent();

        List<T> winners = eligibleRatings(byStudent).entrySet().stream()
                .sorted(PARTICIPANT_ORDER)
                .limit(limit)
                .map(entry -> bestApplication(byStudent.get(entry.getKey())))
                .collect(Collectors.toList());

        winners.forEach(a -> a.setAwarded(true));
        return winners;
    }

    /**
     * Конкурсный рейтинг допущенного студента по текущим заявкам.
     * Для отсутствующего или недопущенного студента - OptionalDouble.empty().
     */
    public OptionalDouble getStudentRating(int studentId) {
        if (studentId <= 0) {
            throw new IllegalArgumentException("ID студента должен быть положительным: " + studentId);
        }
        List<T> studentApplications = applications.stream()
                .filter(a -> a.getStudentId() == studentId)
                .collect(Collectors.toList());
        return ratingOf(studentApplications);
    }

    public Stream<T> getAnalyticsStream() {
        return applications.stream();
    }

    // ---------- вспомогательные методы ----------

    private Map<Integer, List<T>> groupByStudent() {
        return applications.stream()
                .collect(Collectors.groupingBy(ScholarshipApplication::getStudentId));
    }

    /** studentId -> рейтинг, только для допущенных студентов. */
    private Map<Integer, Double> eligibleRatings(Map<Integer, List<T>> byStudent) {
        Map<Integer, Double> ratings = new HashMap<>();
        byStudent.forEach((studentId, studentApplications) ->
                ratingOf(studentApplications).ifPresent(rating -> ratings.put(studentId, rating)));
        return ratings;
    }

    /**
     * Правило варианта 21: нужны и учебная, и научная заявки с баллом >= 60.
     * Рейтинг - полусумма лучших баллов по двум типам.
     */
    private OptionalDouble ratingOf(List<T> studentApplications) {
        OptionalDouble bestAcademic = bestScore(studentApplications, AcademicApplication.class);
        OptionalDouble bestResearch = bestScore(studentApplications, ResearchApplication.class);
        if (bestAcademic.isPresent() && bestResearch.isPresent()) {
            return OptionalDouble.of((bestAcademic.getAsDouble() + bestResearch.getAsDouble()) / 2.0);
        }
        return OptionalDouble.empty();
    }

    /** Лучший балл среди заявок данного типа, прошедших порог. */
    private OptionalDouble bestScore(List<T> studentApplications, Class<? extends ScholarshipApplication> type) {
        return studentApplications.stream()
                .filter(type::isInstance)
                .filter(passing)
                .mapToDouble(ScholarshipApplication::calculateScore)
                .max();
    }

    /** Заявка с наибольшим баллом; при равенстве - с меньшим id. */
    private T bestApplication(List<T> studentApplications) {
        return studentApplications.stream()
                .min(APPLICATION_ORDER)
                .orElseThrow(IllegalStateException::new);
    }
}
