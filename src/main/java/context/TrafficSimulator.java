package context;

import model.Car;
import model.RoadStatus;
import state.FluentTrafficState;
import state.TrafficState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Context of the State design pattern.
 * It stores the active TrafficState and delegates every traffic action to it.
 * The Context never decides the concrete behavior of traffic actions.
 */
public class TrafficSimulator {
    private static final int DEFAULT_SPEED = 90;
    private static final int MAX_LOG_ENTRIES = 20;
    private static final long DEMO_STEP_DELAY_MS = 1200;

    private TrafficState currentState;
    private final List<Car> cars;
    private final RoadStatus roadStatus;
    private final List<String> logs;
    private final List<String> stateTransitions;
    private String lastActionTrace;
    private boolean demoRunning;

    public TrafficSimulator() {
        this.cars = new ArrayList<>();
        this.logs = new ArrayList<>();
        this.stateTransitions = new ArrayList<>();
        this.lastActionTrace = "No action has been handled yet.";
        this.demoRunning = false;
        this.roadStatus = new RoadStatus("", "", "", "", lastActionTrace, 0, "", false);
        initializeSimulator(true);
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
        stateTransitions.clear();
        lastActionTrace = "Action: reset handled by TrafficSimulator \u2192 Transition to FluentTrafficState";
        initializeSimulator(false);
        addLog("TrafficSimulator reset the simulation and restored FluentTrafficState as the initial academic baseline.");
    }

    public synchronized void runDemoSequence() {
        if (demoRunning) {
            addLog("TrafficSimulator rejected a new demo request because the previous demonstration sequence is still running.");
            return;
        }

        demoRunning = true;
        addLog("TrafficSimulator started the automatic demonstration sequence to expose dynamic State-pattern behavior.");

        Thread demoThread = new Thread(() -> {
            try {
                increaseTraffic();
                pauseDemoStep();
                reportAccident();
                pauseDemoStep();
                clearAccident();
                pauseDemoStep();
                reduceTraffic();
                pauseDemoStep();
                advanceSimulation();
            } finally {
                synchronized (TrafficSimulator.this) {
                    demoRunning = false;
                    addLog("TrafficSimulator finished the automatic demonstration sequence.");
                }
            }
        }, "traffic-demo-sequence");

        demoThread.setDaemon(true);
        demoThread.start();
    }

    public synchronized void setState(TrafficState state) {
        String previousStateName = currentState == null ? "None" : currentState.getStateName();
        this.currentState = state;
        logStateTransition(previousStateName, state.getStateName());
        refreshRoadStatus();
        addLog("TrafficSimulator updated its active state reference to " + state.getStateName() + ".");
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
                roadStatus.getActiveStateClass(),
                roadStatus.getLastActionTrace(),
                roadStatus.getAverageSpeed(),
                roadStatus.getCongestionLevel(),
                roadStatus.isAccidentActive()
        );
    }

    public synchronized List<String> getLogs() {
        return Collections.unmodifiableList(new ArrayList<>(logs));
    }

    public synchronized List<String> getStateTransitions() {
        return Collections.unmodifiableList(new ArrayList<>(stateTransitions));
    }

    public synchronized TrafficState getCurrentState() {
        return currentState;
    }

    public synchronized String getLastActionTrace() {
        return lastActionTrace;
    }

    public synchronized void setLastActionTrace(String lastActionTrace) {
        this.lastActionTrace = lastActionTrace;
        roadStatus.setLastActionTrace(lastActionTrace);
    }

    public synchronized void recordStateHandling(String actionMethodName, String handlingStateName, String traceOutcome) {
        String actionTrace = "Action: " + actionMethodName + " handled by " + handlingStateName + " \u2192 " + traceOutcome;
        setLastActionTrace(actionTrace);
        addLog(buildAcademicDelegationLog(actionMethodName, handlingStateName, traceOutcome));
    }

    public synchronized void addLog(String message) {
        logs.add(message);

        if (logs.size() > MAX_LOG_ENTRIES) {
            logs.remove(0);
        }
    }

    public synchronized void logStateTransition(String from, String to) {
        stateTransitions.add(from + " -> " + to);
    }

    private void pauseDemoStep() {
        try {
            Thread.sleep(DEMO_STEP_DELAY_MS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private void initializeSimulator(boolean addInitializationLog) {
        loadDefaultCars();
        currentState = new FluentTrafficState();
        refreshRoadStatus();
        updateCars(DEFAULT_SPEED, false);
        roadStatus.setLastActionTrace(lastActionTrace);

        if (addInitializationLog) {
            addLog("TrafficSimulator initialized the Context with FluentTrafficState as the starting State object.");
        }
    }

    private void refreshRoadStatus() {
        roadStatus.setStateName(currentState.getStateName());
        roadStatus.setSpanishStateName(currentState.getSpanishStateName());
        roadStatus.setDescription(currentState.getDescription());
        roadStatus.setActiveStateClass(currentState.getClass().getSimpleName());
        roadStatus.setLastActionTrace(lastActionTrace);
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

    private String buildAcademicDelegationLog(String actionMethodName, String handlingStateName, String traceOutcome) {
        if (traceOutcome.startsWith("Transition to ")) {
            String targetState = traceOutcome.replace("Transition to ", "");
            return "TrafficSimulator delegated " + actionMethodName + "() to " + handlingStateName
                    + ", which transitioned to " + targetState + ".";
        }

        if (traceOutcome.startsWith("Remains in ")) {
            String targetState = traceOutcome.replace("Remains in ", "");
            return "TrafficSimulator delegated " + actionMethodName + "() to " + handlingStateName
                    + ", which remained in " + targetState + ".";
        }

        return "TrafficSimulator delegated " + actionMethodName + "() to " + handlingStateName
                + ", which produced the result: " + traceOutcome + ".";
    }
}
