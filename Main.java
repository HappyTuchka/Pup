package main;

import logic.PrintQueue;
import models.DoubleSidedJob;
import models.PrintJob;
import models.SingleSidedJob;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Main {

    static Integer N = null;
    static PrintQueue<PrintJob> queue = null;
    static int stage = 0;            // 0 — не начали, 1..3 — завершён последний этап
    static Integer variantA = null;
    static Integer variantB = null;
    static Integer variantC = null;

    static List<Integer> stage1Ids = null;
    static int stage1Sheets = 0;
    static List<Integer> stage2Ids = null;
    static int stage2Paper = 0;
    static List<Integer> stage3Ids = null;
    static int stage3Paper = 0;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            printMenu();
            int choice = readInt(sc, "Выбор: ");
            switch (choice) {
                case 1 -> doStage1(sc);
                case 2 -> doStage2();
                case 3 -> doStage3();
                case 4 -> showQueue();
                case 5 -> { System.out.println("Выход."); return; }
                default -> System.out.println("Неверный пункт меню.");
            }
        }
    }

    static void printMenu() {
        System.out.println("\n=== Меню ===");
        System.out.println("1. Выполнить этап 1 (сформировать очередь)");
        System.out.println("2. Выполнить этап 2 (первая обработка)");
        System.out.println("3. Выполнить этап 3 (пополнение + отчёт)");
        System.out.println("4. Показать очередь");
        System.out.println("5. Выход");
        System.out.printf("Текущий этап: %d; N=%s; A=%s; Б=%s; В=%s%n",
                stage, N, variantA, variantB, variantC);
    }

    static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try { return Integer.parseInt(line); }
            catch (NumberFormatException e) { System.out.println("Введите целое число."); }
        }
    }

    // ================= Этап 1 =================
    static void doStage1(Scanner sc) {
        if (stage >= 1) { System.out.println("Этап 1 уже выполнен."); return; }

        if (N == null) {
            N = readInt(sc, "Введите N (1..36): ");
            if (N < 1 || N > 36) { N = null; System.out.println("N вне диапазона 1..36."); return; }
        }
        variantA = StageAccess.initial(N);

        // Создание исходных объектов
        List<PrintJob> all = new ArrayList<>();
        all.add(new SingleSidedJob(101, 11, 5, 1, 5));
        all.add(new SingleSidedJob(103, 12, 4, 2, 5));
        all.add(new SingleSidedJob(105, 13, 3, 2, 2));
        all.add(new SingleSidedJob(108, 14, 6, 1, 3));
        all.add(new SingleSidedJob(110, 15, 8, 1, 2));

        all.add(new DoubleSidedJob(102, 11, 5, 2, 4));
        all.add(new DoubleSidedJob(104, 12, 7, 1, 3));
        all.add(new DoubleSidedJob(106, 13, 9, 1, 4));
        all.add(new DoubleSidedJob(107, 14, 2, 3, 1));
        all.add(new DoubleSidedJob(109, 12, 1, 2, 4));

        // Условие A1 (вариант A=1): priority >= 3 и потребность <= 8
        Predicate<PrintJob> selectA1 = j ->
                j.getPriority() >= 3 && j.getRequiredSheets() <= 8;

        queue = new PrintQueue<>(14);
        List<Integer> ids = new ArrayList<>();
        int sheets = 0;
        for (PrintJob j : all) {
            if (selectA1.test(j)) {
                if (queue.add(j)) {
                    ids.add(j.getId());
                    sheets += j.getRequiredSheets();
                }
            }
        }
        Collections.sort(ids);

        System.out.println("Выбранные ID (по возрастанию): " + ids);
        System.out.println("Сумма потребностей: " + sheets);

        try {
            variantB = StageAccess.second(N, ids, sheets);
            System.out.println("Назначен вариант этапа 2: Б" + variantB);
        } catch (IllegalStateException e) {
            System.out.println("Ошибка служебного кода: " + e.getMessage());
            queue = null;
            return;
        }

        stage1Ids = ids;
        stage1Sheets = sheets;
        stage = 1;
    }

    // ================= Этап 2 =================
    static void doStage2() {
        if (stage < 1) { System.out.println("Сначала выполните этап 1."); return; }
        if (stage >= 2) { System.out.println("Этап 2 уже выполнен."); return; }
        if (variantB == null) { System.out.println("Вариант Б не назначен."); return; }
        if (variantB != 1) {
            System.out.println("Внимание: реализован только вариант Б1 (N=1). Текущий Б=" + variantB);
            return;
        }

        // Б1: priority по убыванию, потребность по возрастанию, id по возрастанию
        Comparator<PrintJob> order = Comparator
                .comparingInt(PrintJob::getPriority).reversed()
                .thenComparingInt(PrintJob::getRequiredSheets)
                .thenComparingInt(PrintJob::getId);

        Predicate<PrintJob> condition = j -> true;      // доп. ограничений нет
        Consumer<PrintJob> action = PrintJob::markPrinted;

        List<PrintJob> done = queue.process(condition, order, action);
        List<Integer> ids = done.stream().map(PrintJob::getId).collect(Collectors.toList());
        int paper = queue.getPaper();

        System.out.println("Выполненные ID (в порядке выполнения): " + ids);
        System.out.println("Остаток бумаги: " + paper);

        try {
            variantC = StageAccess.third(N, ids, paper);
            System.out.println("Назначен вариант этапа 3: В" + variantC);
        } catch (IllegalStateException e) {
            System.out.println("Ошибка служебного кода: " + e.getMessage());
            return;
        }

        stage2Ids = ids;
        stage2Paper = paper;
        stage = 2;
    }

    // ================= Этап 3 =================
    static void doStage3() {
        if (stage < 2) { System.out.println("Сначала выполните этап 2."); return; }
        if (stage >= 3) { System.out.println("Этап 3 уже выполнен."); return; }
        if (variantC == null) { System.out.println("Вариант В не назначен."); return; }
        if (variantC != 1) {
            System.out.println("Внимание: реализован только вариант В1 (N=1). Текущий В=" + variantC);
            return;
        }

        // В1: добавить 4 листа и повторить обработку Б1
        if (!queue.restock(4)) {
            System.out.println("Не удалось пополнить бумагу.");
            return;
        }
        System.out.println("Бумага пополнена на 4. Остаток: " + queue.getPaper());

        Comparator<PrintJob> order = Comparator
                .comparingInt(PrintJob::getPriority).reversed()
                .thenComparingInt(PrintJob::getRequiredSheets)
                .thenComparingInt(PrintJob::getId);
        Predicate<PrintJob> condition = j -> true;
        Consumer<PrintJob> action = PrintJob::markPrinted;

        List<PrintJob> done = queue.process(condition, order, action);
        List<Integer> ids = done.stream().map(PrintJob::getId).collect(Collectors.toList());
        int paper = queue.getPaper();

        System.out.println("Новые выполненные ID (в порядке выполнения): " + ids);
        System.out.println("Остаток бумаги: " + paper);

        // Отчёт В1: Map<Integer, Long> ownerId → суммарное число листов
        // по напечатанным заданиям. Только владельцы с напечатанными заданиями.
        Map<Integer, Long> report = queue.getAnalyticsStream()
                .filter(PrintJob::isPrinted)
                .collect(Collectors.groupingBy(
                        PrintJob::getOwnerId,
                        Collectors.summingLong(PrintJob::getRequiredSheets)
                ));

        System.out.println("Отчёт В1 (ownerId -> суммарные листы напечатанного): " + report);

        stage3Ids = ids;
        stage3Paper = paper;
        stage = 3;
    }

    // ================= Показать очередь =================
    static void showQueue() {
        if (queue == null) { System.out.println("Очередь не создана."); return; }
        System.out.println("Остаток бумаги: " + queue.getPaper());
        for (PrintJob j : queue.getJobs()) System.out.println("  " + j.getDetails());
    }
}

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
        if (n < 1 || n > 36)
            throw new IllegalArgumentException("N: 1..36");
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
        if (actual != expected)
            throw new IllegalStateException("Check stage result");
        long key = previous == 0 ? actual
                : (actual >>> 6) + 0L * previous
                + (1 - 1L) * ((n - 1) % 6) + S[initial(n) - 1];
        return 1 + Math.floorMod(key + (n - 1) % 6, 6);
    }
}