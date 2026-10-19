package main;

import logic.ScholarshipManager;
import models.AcademicApplication;
import models.ResearchApplication;
import models.ScholarshipApplication;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Система распределения стипендий. Вариант 21.
 *   Конкурс   - "Два направления": есть учебная и научная заявки с баллом >= 60,
 *               рейтинг - полусумма лучших баллов по двум типам.
 *   Аналитика - Map<String, Long>: имя класса заявки -> число различных студентов
 *               с заявкой этого типа с баллом не ниже 60 (оба типа, при отсутствии - 0).
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final ScholarshipManager<ScholarshipApplication> manager = createManagerWithInitialData();

    public static void main(String[] args) {
        boolean running = true;
        try {
            while (running) {
                printMenu();
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        addApplication();
                        break;
                    case "2":
                        showAllApplications();
                        break;
                    case "3":
                        runContest();
                        break;
                    case "4":
                        runAnalytics();
                        break;
                    case "5":
                        running = false;
                        System.out.println("Выход из программы.");
                        break;
                    default:
                        System.out.println("Некорректный выбор, попробуйте снова.");
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("Ввод завершён. Выход из программы.");
        }
        scanner.close();
    }

    private static ScholarshipManager<ScholarshipApplication> createManagerWithInitialData() {
        ScholarshipManager<ScholarshipApplication> m = new ScholarshipManager<>();

        m.add(new AcademicApplication(1, 1, 100, 100));
        m.add(new AcademicApplication(3, 2, 85, 85));
        m.add(new AcademicApplication(4, 2, 85, 85));
        m.add(new AcademicApplication(5, 3, 80, 80));
        m.add(new AcademicApplication(7, 4, 60, 60));
        m.add(new AcademicApplication(9, 5, 59, 59));
        m.add(new AcademicApplication(11, 6, 90, 90));
        m.add(new AcademicApplication(13, 7, 100, 100));

        m.add(new ResearchApplication(2, 1, 3, 0));
        m.add(new ResearchApplication(6, 3, 4, 0));
        m.add(new ResearchApplication(8, 4, 3, 0));
        m.add(new ResearchApplication(10, 5, 5, 0));
        m.add(new ResearchApplication(12, 6, 4, 1));

        return m;
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("===== Распределение стипендий =====");
        System.out.println("1. Добавить заявку");
        System.out.println("2. Показать все заявки");
        System.out.println("3. Провести конкурс");
        System.out.println("4. Аналитический запрос");
        System.out.println("5. Выход");
        System.out.print("Выберите пункт: ");
    }

    // ---------- пункт 1 ----------
    private static void addApplication() {
        System.out.print("Тип заявки (1 - учебная, 2 - научная): ");
        String type = scanner.nextLine().trim();
        if (!type.equals("1") && !type.equals("2")) {
            System.out.println("Ошибка: неизвестный тип заявки. Заявка не добавлена.");
            return;
        }

        int id = readInt("ID заявки: ");
        int studentId = readInt("ID студента: ");

        try {
            ScholarshipApplication application;
            if (type.equals("1")) {
                int exam1 = readInt("Оценка exam1 (0-100): ");
                int exam2 = readInt("Оценка exam2 (0-100): ");
                application = new AcademicApplication(id, studentId, exam1, exam2);
            } else {
                int articles = readInt("Количество статей articles (0-5): ");
                int talks = readInt("Количество докладов talks (0-10): ");
                application = new ResearchApplication(id, studentId, articles, talks);
            }

            if (manager.add(application)) {
                System.out.println("Заявка добавлена: " + application.getDetails());
            } else {
                System.out.println("Ошибка: заявка с таким id уже существует. Заявка не добавлена.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage() + ". Заявка не добавлена.");
        }
    }

    // ---------- пункт 2 ----------
    private static void showAllApplications() {
        System.out.println("Все заявки:");
        manager.getAnalyticsStream()
                .forEach(application -> System.out.println("  " + application.getDetails()));
    }

    // ---------- пункт 3 ----------
    private static void runContest() {
        int limit = readInt("Введите limit (число стипендий): ");
        try {
            List<ScholarshipApplication> winners = manager.allocate(limit);
            if (winners.isEmpty()) {
                System.out.println("Победителей нет.");
                return;
            }
            System.out.println("Победители конкурса (в порядке рейтинга):");
            int place = 1;
            for (ScholarshipApplication winner : winners) {
                double rating = manager.getStudentRating(winner.getStudentId()).orElse(0.0);
                System.out.printf("%d. studentId=%d, рейтинг=%.1f, заявка id=%d%n",
                        place, winner.getStudentId(), rating, winner.getId());
                place++;
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage() + ". Конкурс не проведён.");
        }
    }

    // ---------- пункт 4 (вариант 21) ----------
    private static void runAnalytics() {
        Map<String, Long> counts = Stream.of(AcademicApplication.class, ResearchApplication.class)
                .collect(Collectors.toMap(
                        Class::getSimpleName,
                        type -> manager.getAnalyticsStream()
                                .filter(type::isInstance)
                                .filter(application -> application.calculateScore() >= 60)
                                .map(ScholarshipApplication::getStudentId)
                                .distinct()
                                .count(),
                        (first, second) -> first,
                        LinkedHashMap::new));

        System.out.println("Число различных студентов с заявкой типа и баллом не ниже 60:");
        counts.forEach((typeName, count) -> System.out.println("  " + typeName + ": " + count));
    }

    // ---------- ввод ----------
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число.");
            }
        }
    }
}
