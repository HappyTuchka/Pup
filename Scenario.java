package logic;
import models.Device;
import java.util.function.Consumer;
import java.util.function.Predicate;
/**
 * Обобщенный класс сценария автоматизации.
 * T - конкретный тип устройства, к которому применяется сценарий.
 */
public class Scenario<T extends Device> {
    private String name;
    private Predicate<T> condition;
    private Consumer<T> action;
    public Scenario(String name, Predicate<T> condition, Consumer<T> action) {
        this.name = name;
        this.condition = condition;
        this.action = action;
    }
    /**
     * Проверяет условие и, если оно истинно, выполняет действие над устройством.
     */
    public void apply(T device) {
        if (condition.test(device)) {
            action.accept(device);
        }
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Predicate<T> getCondition() {
        return condition;
    }
    public void setCondition(Predicate<T> condition) {
        this.condition = condition;
    }
    public Consumer<T> getAction() {
        return action;
    }
    public void setAction(Consumer<T> action) {
        this.action = action;
    }
}
