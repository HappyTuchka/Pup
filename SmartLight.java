package models;
public class SmartLight extends Device {
    private int brightness; // 0..100
    private String color;
    public SmartLight(String id, String name, double powerConsumption, int brightness, String color) {
        super(id, name, powerConsumption);
        setBrightness(brightness);
        this.color = color;
    }
    @Override
    public String getDetails() {
        return String.format("[Лампа] ID: %s, Имя: %s, Вкл: %s, Мощность: %.1f Вт, Яркость: %d, Цвет: %s",
                id, name, status ? "Да" : "Нет", powerConsumption, brightness, color);
    }
    public int getBrightness() {
        return brightness;
    }
    public void setBrightness(int brightness) {
        if (brightness < 0) brightness = 0;
        if (brightness > 100) brightness = 100;
        this.brightness = brightness;
    }
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }
}
