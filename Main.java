package main;
import logic.Scenario;
import logic.SmartHomeManager;
import models.Device;
import models.SmartLight;
import models.SmartSocket;
import models.SmartThermostat;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
/**
 * Консольное приложение "Умный дом" (реализация на оценку "Хорошо",
 * без бонусной части с сериализацией в файл).
 * Вариант 1:
 *   Сценарий   - Энергосбережение: если SmartSocket включена и currentLoad > 2000,
 *                перевести её статус в "Выключено".
 *   Аналитика  - ТОП-3 самых энергозатратных устройств во всем доме
 *                (сортировка по убыванию powerConsumption).
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final SmartHomeManager manager = new SmartHomeManager();

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    addDevice();
                    break;
                case "2":
                    toggleDevice();
                    break;
                case "3":
                    addPhantomResetScenario();
                    break;
                case "4":
                    addVariantScenario();
                    break;
                case "5":
                    manager.executeAllScenarios();
                    System.out.println("Все сценарии выполнены.");
                    break;
                case "6":
                    runAnalytics();
                    break;
                case "7":
                    running = false;
                    System.out.println("Выход из программы.");
                    break;
                default:
                    System.out.println("Некорректный выбор, попробуйте снова.");
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("===== Умный дом: главное меню =====");
        System.out.println("1. Добавить новое устройство в комнату");
        System.out.println("2. Включить/выключить устройство");
        System.out.println("3. Добавить базовый сценарий \"Сброс фантомного потребления\"");
        System.out.println("4. Добавить сценарий автоматизации (индивидуальный вариант)");
        System.out.println("5. Запустить проверку всех сценариев");
        System.out.println("6. Выполнить аналитический запрос");
        System.out.println("7. Выход");
        System.out.print("Выберите пункт: ");
    }

    private static void addDevice() {
        System.out.print("Тип устройства (light / thermostat / socket): ");
        String type = scanner.nextLine().trim().toLowerCase();

        System.out.print("ID устройства: ");
        String id = scanner.nextLine().trim();

        System.out.print("Название устройства: ");
        String name = scanner.nextLine().trim();

        System.out.print("Комната: ");
        String room = scanner.nextLine().trim();

        System.out.print("Текущее энергопотребление (Вт): ");
        double power = readDouble();

        Device device;
        switch (type) {
            case "light":
                System.out.print("Яркость (0-100): ");
                int brightness = readInt();
                System.out.print("Цвет: ");
                String color = scanner.nextLine().trim();
                device = new SmartLight(id, name, power, brightness, color);
                break;
            case "thermostat":
                System.out.print("Целевая температура: ");
                double targetTemp = readDouble();
                device = new SmartThermostat(id, name, power, targetTemp);
                break;
            case "socket":
                System.out.print("Текущая нагрузка (Вт): ");
                double load = readDouble();
                device = new SmartSocket(id, name, power, load);
                break;
            default:
                System.out.println("Неизвестный тип устройства. Добавление отменено.");
                return;
        }

        manager.addDevice(room, device);
        System.out.println("Устройство добавлено: " + device.getDetails());
    }

    private static void toggleDevice() {
        System.out.print("Введите ID устройства: ");
        String id = scanner.nextLine().trim();
        Device device = manager.getDeviceById(id);
        if (device == null) {
            System.out.println("Устройство с ID " + id + " не найдено.");
            return;
        }
        if (device.isOn()) {
            device.turnOff();
            System.out.println("Устройство " + device.getName() + " выключено.");
        } else {
            device.turnOn();
            System.out.println("Устройство " + device.getName() + " включено.");
        }
    }

    /**
     * Пункт 3: базовый сценарий, общий для всех вариантов.
     * Если устройство выключено, но powerConsumption > 0 - обнулить потребление.
     */
    private static void addPhantomResetScenario() {
        Scenario<Device> resetScenario = new Scenario<>(
                "Сброс фантомного потребления",
                device -> !device.isOn() && device.getPowerConsumption() > 0,
                device -> device.setPowerConsumption(0.0)
        );
        manager.addScenario(resetScenario);
        System.out.println("Базовый сценарий \"Сброс фантомного потребления\" добавлен.");
    }

    /**
     * Пункт 4 (Вариант 1): Энергосбережение.
     * Если розетка включена и currentLoad > 2000 Вт, выключить её.
     */
    private static void addVariantScenario() {
        Scenario<SmartSocket> energySavingScenario = new Scenario<>(
                "Энергосбережение",
                socket -> socket.isOn() && socket.getCurrentLoad() > 2000,
                socket -> socket.setStatus(false)
        );
        manager.addScenario(energySavingScenario);
        System.out.println("Сценарий \"Энергосбережение\" (Вариант 1) добавлен.");
    }

    /**
     * Пункт 6 (Вариант 1): ТОП-3 самых энергозатратных устройств
     * (сортировка по убыванию powerConsumption), только через Stream API.
     */
    private static void runAnalytics() {
        List<Device> top3 = manager.getAnalyticsStream()
                .sorted(Comparator.comparingDouble(Device::getPowerConsumption).reversed())
                .limit(3)
                .collect(Collectors.toList());

        System.out.println("ТОП-3 самых энергозатратных устройств:");
        if (top3.isEmpty()) {
            System.out.println("В доме нет устройств.");
        } else {
            int rank = 1;
            for (Device device : top3) {
                System.out.println(rank + ". " + device.getDetails());
                rank++;
            }
        }
    }

    private static double readDouble() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Введите корректное число: ");
            }
        }
    }

    private static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Введите корректное целое число: ");
            }
        }
    }
}
