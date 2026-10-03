package models;

public class SmartThermostat extends Device{
    private double targetTeperature;
    public SmartThermostat(String id, String name, double powerConsumption, double targetTeperature){
        super(id,name,powerConsumption);
        this.targetTeperature = targetTeperature;
    }
    @Override
    public String getDetails(){
        return String.format("[Термостат] Id: %s, Имя: %s, Вкл: %s, Мощность: %1.f Вт, Температура: %1.f Градусов",
                id, name, status ? "Да" : "Нет", powerConsumption,targetTeperature);
    }
    public double getTargetTeperature() {
        return targetTeperature;
    }
    public void setTargetTeperature(double targetTeperature) {
        this.targetTeperature = targetTeperature;
    }
}