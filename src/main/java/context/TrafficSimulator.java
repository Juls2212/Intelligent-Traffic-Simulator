package context;

import model.Car;
import model.RoadStatus;
import state.FluentTrafficState;
import state.TrafficState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrafficSimulator {
    private static final int DEFAULT_SPEED = 90;
    private static final int MAX_LOG_ENTRIES = 20;

    private TrafficState currentState;
    private final List<Car> cars;
    private final RoadStatus roadStatus;
    private final List<String> logs;

    public TrafficSimulator() {
        this.cars = new ArrayList<>();
        this.logs = new ArrayList<>();
        this.roadStatus = new RoadStatus("", "", "", 0, "", false);
        loadDefaultCars();
        this.currentState = new FluentTrafficState();
        refreshRoadStatus();
        addLog("Simulator initialized in fluent traffic state.");
        updateCars(DEFAULT_SPEED, false);
    }

    public synchronized void increaseTraffic() {
        currentState.increaseTraffic(this);
    }

    public synchronized void reduceTraffic() {
        currentState.reduceTraffic(this);
    }

    public synchronized void reportAccident() {
        currentState.reportAccident(this);
    }

    public synchronized void clearAccident() {
        currentState.clearAccident(this);
    }

    public synchronized void advanceSimulation() {
        currentState.advanceSimulation(this);
    }

    public synchronized void reset() {
        cars.clear();
        logs.clear();
        loadDefaultCars();
        currentState = new FluentTrafficState();
        refreshRoadStatus();
        addLog("Simulator reset to the initial fluent state.");
        updateCars(DEFAULT_SPEED, false);
    }

    public synchronized void setState(TrafficState state) {
        this.currentState = state;
        refreshRoadStatus();
        addLog("State changed to " + state.getStateName() + ".");
    }

    public synchronized void updateCars(int speed, boolean blocked) {
        for (Car car : cars) {
            car.setSpeed(speed);
            car.setBlocked(blocked);

            if (!blocked) {
                car.setXPosition(car.getXPosition() + speed);
            }
        }

        roadStatus.setAverageSpeed(speed);
        roadStatus.setAccidentActive(blocked);
        roadStatus.setCongestionLevel(resolveCongestionLevel(speed, blocked));
    }

    public synchronized List<Car> getCars() {
        return Collections.unmodifiableList(new ArrayList<>(cars));
    }

    public synchronized RoadStatus getRoadStatus() {
        return new RoadStatus(
                roadStatus.getStateName(),
                roadStatus.getSpanishStateName(),
                roadStatus.getDescription(),
                roadStatus.getAverageSpeed(),
                roadStatus.getCongestionLevel(),
                roadStatus.isAccidentActive()
        );
    }

    public synchronized List<String> getLogs() {
        return Collections.unmodifiableList(new ArrayList<>(logs));
    }

    public synchronized TrafficState getCurrentState() {
        return currentState;
    }

    public synchronized void addLog(String message) {
        logs.add(message);

        if (logs.size() > MAX_LOG_ENTRIES) {
            logs.remove(0);
        }
    }

    private void refreshRoadStatus() {
        roadStatus.setStateName(currentState.getStateName());
        roadStatus.setSpanishStateName(currentState.getSpanishStateName());
        roadStatus.setDescription(currentState.getDescription());
        roadStatus.setAccidentActive("AccidentTrafficState".equals(currentState.getStateName()));
    }

    private String resolveCongestionLevel(int speed, boolean blocked) {
        if (blocked) {
            return "Critical";
        }

        if (speed >= 80) {
            return "Low";
        }

        if (speed >= 40) {
            return "Medium";
        }

        return "High";
    }

    private void loadDefaultCars() {
        cars.add(new Car("CAR-001", 1, 0, DEFAULT_SPEED, false));
        cars.add(new Car("CAR-002", 2, 20, DEFAULT_SPEED, false));
        cars.add(new Car("CAR-003", 1, 40, DEFAULT_SPEED, false));
        cars.add(new Car("CAR-004", 3, 60, DEFAULT_SPEED, false));
    }
}
