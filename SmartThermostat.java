package models;
public class SmartThermostat extends Device {
    private double targetTemperature;
    public SmartThermostat(String id, String name, double powerConsumption, double targetTemperature) {
        super(id, name, powerConsumption);
        this.targetTemperature = targetTemperature;
    }
    @Override
    public String getDetails() {
        return String.format("[Термостат] ID: %s, Имя: %s, Вкл: %s, Мощность: %.1f Вт, Температура: %.1f Градусов",
                id, name, status ? "Да" : "Нет", powerConsumption, targetTemperature);
    }
    public double getTargetTemperature() {
        return targetTemperature;
    }
    public void setTargetTemperature(double targetTemperature) {
        this.targetTemperature = targetTemperature;
    }
}
