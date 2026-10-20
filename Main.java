package main;

import logic.PrintQueue;
import models.DoubleSidedJob;
import models.PrintJob;
import models.SingleSidedJob;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Main {
    private static final String FILE_NAME = "print_state.dat";
    private static final Scanner scanner = new Scanner(System.in);

    private static PrintQueue<PrintJob> queue = new PrintQueue<>(14);
    private static int N = 0;
    private static int completedStage = 0;
    private static int stage2Variant = 0;
    private static int stage3Variant = 0;

    public static void main(String[] args) {
        if (!loadState()) {
            initDefaultData();
        }

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Выберите пункт: ");
            switch (choice) {
                case 1 -> executeStage1();
                case 2 -> executeStage2();
                case 3 -> executeStage3();
                case 4 -> showQueue();
                case 5 -> {
                    saveState();
                    running = false;
                    System.out.println("Выход.");
                }
                default -> System.out.println("Неверный пункт меню.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n=== МЕНЮ ===");
        System.out.println("1. Выполнить задание 1 (Этап 1)");
        System.out.println("2. Выполнить задание 2 (Этап 2)");
        System.out.println("3. Выполнить задание 3 (Этап 3)");
        System.out.println("4. Показать очередь");
        System.out.println("5. Выход");
        System.out.println("Текущий этап: " + completedStage + "; N=" + (N == 0 ? "null" : N) +
                "; A=" + (completedStage >= 1 ? 1 : "null") +
                "; Б=" + (completedStage >= 1 ? stage2Variant : "null") +
                "; В=" + (completedStage >= 2 ? stage3Variant : "null"));
    }

    private static void initDefaultData() {
        queue = new PrintQueue<>(14);
        queue.add(new SingleSidedJob(101, 11, 5, 1, 5));
        queue.add(new SingleSidedJob(103, 12, 4, 2, 5));
        queue.add(new SingleSidedJob(105, 13, 3, 2, 2));
        queue.add(new SingleSidedJob(108, 14, 6, 1, 3));
        queue.add(new SingleSidedJob(110, 15, 8, 1, 2));

        queue.add(new DoubleSidedJob(102, 11, 5, 2, 4));
        queue.add(new DoubleSidedJob(104, 12, 7, 1, 3));
        queue.add(new DoubleSidedJob(106, 13, 9, 1, 4));
        queue.add(new DoubleSidedJob(107, 14, 2, 3, 1));
        queue.add(new DoubleSidedJob(109, 12, 1, 2, 4));
    }

    // ================= ЭТАП 1 (A1: Приоритетные короткие) =================
    private static void executeStage1() {
        if (completedStage >= 1) {
            System.out.println("Этап 1 уже выполнен.");
            return;
        }

        N = readInt("Введите начальный номер N (1-36): ");
        if (N != 1) {
            System.out.println("Внимание: этот код написан строго для N=1.");
            N = 0;
            return;
        }

        // A1: priority >= 3 и потребность <= 8.
        Predicate<PrintJob> condition1 = job ->
                job.getPriority() >= 3 && job.getRequiredSheets() <= 8;

        PrintQueue<PrintJob> newQueue = new PrintQueue<>(14);
        List<PrintJob> selected = queue.getAnalyticsStream()
                .filter(condition1)
                .sorted(Comparator.comparingInt(PrintJob::getId))
                .collect(Collectors.toList());

        for (PrintJob pj : selected) {
            newQueue.add(pj);
        }
        queue = newQueue;

        List<Integer> ids = selected.stream().map(PrintJob::getId).collect(Collectors.toList());
        int sumSheets = selected.stream().mapToInt(PrintJob::getRequiredSheets).sum();

        System.out.println("Добавленные ID: " + ids);
        System.out.println("Сумма потребностей: " + sumSheets);

        try {
            stage2Variant = StageAccess.second(N, ids, sumSheets);
            System.out.println("Назначен вариант этапа 2: Б" + stage2Variant);
            completedStage = 1;
        } catch (IllegalStateException e) {
            System.out.println("Ошибка служебного кода: " + e.getMessage());
        }
    }

    // ================= ЭТАП 2 (Б2: Сначала больший расход) =================
    private static void executeStage2() {
        if (completedStage == 0) {
            System.out.println("Сначала выполните Этап 1.");
            return;
        }
        if (completedStage >= 2) {
            System.out.println("Этап 2 уже выполнен.");
            return;
        }

        // Б2: priority по убыванию, потребность по убыванию, id по возрастанию.
        Comparator<PrintJob> order = Comparator
                .comparingInt(PrintJob::getPriority).reversed()
                .thenComparing(Comparator.comparingInt(PrintJob::getRequiredSheets).reversed())
                .thenComparingInt(PrintJob::getId);

        Predicate<PrintJob> condition = job -> true;
        Consumer<PrintJob> action = PrintJob::markPrinted;

        List<PrintJob> executed = queue.process(condition, order, action);

        List<Integer> executedIds = executed.stream().map(PrintJob::getId).collect(Collectors.toList());
        int currentPaper = queue.getPaper();

        System.out.println("Выполненные ID (в порядке выполнения): " + executedIds);
        System.out.println("Остаток бумаги: " + currentPaper);

        try {
            stage3Variant = StageAccess.third(N, executedIds, currentPaper);
            System.out.println("Назначен вариант этапа 3: В" + stage3Variant);
            completedStage = 2;
        } catch (IllegalStateException e) {
            System.out.println("Ошибка служебного кода: " + e.getMessage());
        }
    }

    // ================= ЭТАП 3 (В5: +8 листов, отчет Optional<Integer>) =================
    private static void executeStage3() {
        if (completedStage < 2) {
            System.out.println("Сначала выполните Этапы 1 и 2.");
            return;
        }
        if (completedStage >= 3) {
            System.out.println("Этап 3 уже выполнен.");
            return;
        }

        // В5: Добавить 8 листов
        queue.restock(8);

        // Повторяем обработку по правилу Б2
        Comparator<PrintJob> order = Comparator
                .comparingInt(PrintJob::getPriority).reversed()
                .thenComparing(Comparator.comparingInt(PrintJob::getRequiredSheets).reversed())
                .thenComparingInt(PrintJob::getId);
        Predicate<PrintJob> condition = job -> true;
        Consumer<PrintJob> action = PrintJob::markPrinted;

        List<PrintJob> executed = queue.process(condition, order, action);
        List<Integer> executedIds = executed.stream().map(PrintJob::getId).collect(Collectors.toList());
        int currentPaper = queue.getPaper();

        System.out.println("Новые выполненные ID: " + executedIds);
        System.out.println("Остаток бумаги: " + currentPaper);

        // Отчёт В5 (Stream API): ownerId владельца с наибольшей общей потребностью ожидающих заданий.
        // При равенстве — меньший ownerId.
        Optional<Integer> report = queue.getAnalyticsStream()
                .filter(job -> !job.isPrinted())
                .collect(Collectors.groupingBy(
                        PrintJob::getOwnerId,
                        Collectors.summingInt(PrintJob::getRequiredSheets)
                ))
                .entrySet().stream()
                .max(Comparator
                        .comparingInt(Map.Entry<Integer, Integer>::getValue)
                        .thenComparing(Comparator.comparingInt(Map.Entry<Integer, Integer>::getKey).reversed())
                )
                .map(Map.Entry::getKey);

        System.out.println("--- Отчёт ---");
        if (report.isPresent()) {
            System.out.println("OwnerId с наибольшей потребностью: " + report.get());
        } else {
            System.out.println("Нет ожидающих заданий.");
        }

        completedStage = 3;
    }

    private static void showQueue() {
        System.out.println("Остаток бумаги: " + queue.getPaper());
        queue.getAnalyticsStream().forEach(j -> System.out.println(j.getDetails()));
    }

    // ================= Сериализация =================
    private static void saveState() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            Map<String, Object> state = new HashMap<>();
            state.put("queue", queue);
            state.put("N", N);
            state.put("s2", stage2Variant);
            state.put("s3", stage3Variant);
            state.put("completed", completedStage);
            oos.writeObject(state);
            System.out.println("Состояние сохранено.");
        } catch (IOException e) {
            System.out.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean loadState() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return false;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Map<String, Object> state = (Map<String, Object>) ois.readObject();
            queue = (PrintQueue<PrintJob>) state.get("queue");
            N = (Integer) state.get("N");
            stage2Variant = (Integer) state.get("s2");
            stage3Variant = (Integer) state.get("s3");
            completedStage = (int) state.getOrDefault("completed", 0);
            System.out.println("Состояние загружено.");
            return true;
        } catch (Exception e) {
            System.out.println("Ошибка чтения файла: " + e.getMessage() + ". Запуск новой цепочки.");
            return false;
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Введите целое число.");
            }
        }
    }
}

// ================= ГОТОВЫЙ КОД StageAccess =================
final class StageAccess {
    private static final int[] S = {5, 3, 2, 4, 2, 1};
    private static final int[] A = {
            0xcb89387d, 0x4626b7fe, 0xacac2431, 0x4997263f, 0x6b20cc2b,
            0x209a21ae
    };
    private static final int[] B = {
            0x6128024d, 0x38a00d09, 0x38a2c749, 0x2ad3a538, 0x38a00d09,
            0x38a00d09, 0x612876ab, 0x612ba548, 0xe7a1669a, 0x612876ab,
            0xc4f9d0bb, 0x612876ab, 0xf3601d34, 0x013dc91f, 0xf3601d34,
            0x4471d939, 0x38a2c749, 0x2ad3a538, 0x6128024d, 0x38a00d09,
            0x38a16a29, 0x04b3adf2, 0x38a00d09, 0x38a00d09, 0x38a00d09,
            0x38a00d09, 0x6128024d, 0x38a00d09, 0x38a16a29, 0x04b3adf2,
            0x013dc91f, 0x4471d939, 0x4471d939, 0x013e3d80, 0x0d1d0f0f,
            0x4471d939
    };
    static int initial(int n) {
        if (n < 1 || n > 36) throw new IllegalArgumentException("N: 1..36");
        return (n - 1) / 6 + 1;
    }
    static int second(int n, java.util.List<Integer> ids, int sheets) {
        int a = initial(n);
        return check(n, ids, sheets, A[a - 1], 0);
    }
    static int third(int n, java.util.List<Integer> ids, int paper) {
        int a = initial(n);
        int b = 1 + Math.floorMod((long) A[a - 1] + (n - 1) % 6, 6);
        return check(n, ids, paper, B[n - 1], b);
    }
    private static int check(int n, java.util.List<Integer> ids,
                             int value, int expected, int previous) {
        if (ids == null) throw new IllegalArgumentException("ids: null");
        int actual = (ids + "|" + value).hashCode();
        if (actual != expected) throw new IllegalStateException("Check stage result");
        long key = previous == 0 ? actual
                : (actual >>> 6) + 0L * previous
                + (1 - 1L) * ((n - 1) % 6) + S[initial(n) - 1];
        return 1 + Math.floorMod(key + (n - 1) % 6, 6);
    }
}