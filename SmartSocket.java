package models;

public class SmartSocket extends Device{
    private double currentLoad;
    public SmartSocket(String id, String name, double powerConsumption, double currentLoad){
        super(id,name,powerConsumption);
        this.currentLoad = currentLoad;
    }
    @Override
    public String getDetails(){
        return String.format("[Лампа] ID: %s, Имя: %s, Вкл: %s, Мощность : %1.f Вт, Нагрузка: %1.f Вт",
                id, name, status ? "Да" : "Нет",powerConsumption,currentLoad );
    }
    public  double getCurrentLoad(){return currentLoad;}
    public  void  setCurrentLoad(double currentLoad){this.currentLoad = currentLoad;}
}
