package logic;

import models.Device;
import java.util.*;
import java.util.stream.Stream;

public class SmartHomeManager {
    private Map<String, List<Device>> rooms;
    private List<Scenario<? extends Device>> scenarios;
    public SmartHomeManager(){
        this.rooms= new LinkedHashMap<>();
        this.scenarios = new ArrayList<>();
    }
    public void addDevice(String room,Device device){
        rooms.computeIfAbsent(room,k->new ArrayList<>()).add(device);
    }
    public void addScenario(Scenario<?> scenario){scenarios.add(scenario);}
    public Device getDeviceById(String id){
        for(List<Device> devicesInRoom : rooms.values()){
            for( Device device : devicesInRoom){
                if(device.getId().equals(id)){
                    return device;
                }
            }
        }
        return null;
    }
    public void executeAllScanarios(){
        for(List<Device> devicesInRoom : rooms.values()){
            for( Device device : devicesInRoom){
                for ( Scenario<? extends Device> scenario : scenarios ){
                    applyIfCompatible(scenario,device);
                }
            }
        }
    }
    @SuppressWarnings("unchecked")
    private <T extends Device> void applyIfCompatible(Scenario<T> scenario, Device device){
        try{
            T typedDevice = (T) device;
            scenario.apply(typedDevice);
        }catch (ClassCastException e ){

        }
    }
    public  Stream<Device> getAnalyticsStream(){return rooms.values().stream().flatMap(List ::stream);}
    public Map<String, List<Device>> getRooms(){return rooms;}
    public List<Scenario<? extends  Device>> getScenarios(){return scenarios;}

}