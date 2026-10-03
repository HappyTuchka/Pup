package models;
public class SmartSocket extends Device {
    private double currentLoad; // подключенная нагрузка, Вт
    public SmartSocket(String id, String name, double powerConsumption, double currentLoad) {
        super(id, name, powerConsumption);
        this.currentLoad = currentLoad;
    }
    @Override
    public String getDetails() {
        return String.format("[Розетка] ID: %s, Имя: %s, Вкл: %s, Мощность: %.1f Вт, Нагрузка: %.1f Вт",
                id, name, status ? "Да" : "Нет", powerConsumption, currentLoad);
    }
    public double getCurrentLoad() {
        return currentLoad;
    }
    public void setCurrentLoad(double currentLoad) {
        this.currentLoad = currentLoad;
    }
}
