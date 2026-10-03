package models;

import interfaces.Controllable;

public abstract class Device implements Controllable{
    protected String id;
    protected String name;
    protected boolean status;
    protected double powerConsumption;

    public Device(String id, String name, double powerConsumption){
        this.id = id;
        this.name = name;
        this.status = false;
        this.powerConsumption = powerConsumption;
    }
    public abstract String getDetails();
    @Override
    public void turnOn(){this.status = true;}
    @Override
    public void turnOff(){this.status = false;}
    @Override
    public boolean isOn(){return status;}

    public String getId(){return id;}
    public void setId(String id){this.id = id;}
    public String getName(){return name;}
    public void setName(String name){this.name = name;}
    public void  setStatus(boolean status){this.status = status;}
    public double getPowerConsumption(){return powerConsumption;}
    public void setPowerConsumption (double powerConsumption){this.powerConsumption = powerConsumption;}
}