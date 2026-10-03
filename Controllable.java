package interfaces;

/**
 * Базовый интерфейс для всех управляемых устройств умного дома.
 */
public interface Controllable {

    void turnOn();

    void turnOff();

    boolean isOn();
}
