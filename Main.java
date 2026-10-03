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

public class Main{
    private static final Scanner scanner = new Scanner(System.in);
    private static final SmartHomeManager manager = new SmartHomeManager();
    public static void main (String[] args){
        boolean running = true;
        while (running){
            printMenu();
            String choise = scanner.nextLine().trim();

            switch (choise){
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
                    manager.executeAllScanarios();
                    System.out.println("all scenarios complete");
                    break;
                case"6":
                    runAnalytics();
                    break;
                case "7":
                    running = false;
                    System.out.println("выход");
                    break;
                default:
                    System.out.println("некоректный ввод");
            }
        }
        scanner.close();
    }
    private static void printMenu(){
        System.out.println();
        System.out.println("1");
        System.out.println("2");
        System.out.println("3");
        System.out.println("4");
        System.out.println("5");
        System.out.println("6");
        System.out.println("7");
        System.out.print("/");
    }
    private static void addDevice(){
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
        switch (type){
            case "light" :
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
    private static void addPhantomResetScenario() {
        Scenario<Device> resetScenario = new Scenario<>(
                "Сброс фантомного потребления",
                device -> !device.isOn() && device.getPowerConsumption() > 0,
                device -> device.setPowerConsumption(0.0)
        );
        manager.addScenario(resetScenario);
        System.out.println("Базовый сценарий \"Сброс фантомного потребления\" добавлен.");
    }
    private static void addVariantScenario() {
        Scenario<SmartSocket> energySavingScenario = new Scenario<>(
                "Энергосбережение",
                socket -> socket.isOn() && socket.getCurrentLoad() > 2000,
                socket -> socket.setStatus(false)
        );
        manager.addScenario(energySavingScenario);
        System.out.println("Сценарий \"Энергосбережение\" (Вариант 1) добавлен.");
    }
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
    private static double readDouble(){
        while (true){
            try{
                return Integer.parseInt(scanner.nextLine().trim());
            }catch (NumberFormatException e ){
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