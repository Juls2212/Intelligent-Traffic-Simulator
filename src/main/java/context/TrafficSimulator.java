package context;

import model.Car;
import model.LaneStatus;
import model.RoadStatus;
import model.TrafficLight;
import state.FluentTrafficState;
import state.TrafficState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Context of the State design pattern.
 * It stores the active TrafficState and delegates every traffic action to it.
 * The Context executes infrastructure mechanics, while each concrete State decides the policy.
 */
public class TrafficSimulator {
    private static final int DEFAULT_SPEED = 90;
    private static final int MODERATE_SPEED = 35;
    private static final int RECOVERY_SPEED = 55;
    private static final int CRITICAL_SPEED = 8;
    private static final int LANE_COUNT = 3;
    private static final int MAX_LOG_ENTRIES = 24;
    private static final int MAX_DECISION_LOGS = 12;
    private static final long DEMO_STEP_DELAY_MS = 1200;
    private static final double ROAD_LENGTH = 760.0;
    private static final double MOVEMENT_SCALE = 1.8;
    private static final double ACCIDENT_POSITION = 540.0;
    private static final double ACCIDENT_APPROACH_DISTANCE = 120.0;
    private static final int ACCIDENT_AFTER_ZONE_SPEED = 14;

    private TrafficState currentState;
    private final List<Car> cars;
    private final RoadStatus roadStatus;
    private final List<String> logs;
    private final List<String> stateTransitions;
    private final List<TrafficLight> trafficLights;
    private final List<LaneStatus> laneStatuses;
    private final List<String> decisionLogs;
    private String lastActionTrace;
    private boolean demoRunning;
    private long lastMovementTimestamp;
    private int blockedLane;

    public TrafficSimulator() {
        this.cars = new ArrayList<>();
        this.logs = new ArrayList<>();
        this.stateTransitions = new ArrayList<>();
        this.trafficLights = new ArrayList<>();
        this.laneStatuses = new ArrayList<>();
        this.decisionLogs = new ArrayList<>();
        this.lastActionTrace = "No action has been handled yet.";
        this.demoRunning = false;
        this.lastMovementTimestamp = System.currentTimeMillis();
        this.blockedLane = -1;
        this.roadStatus = new RoadStatus("", "", "", "", lastActionTrace, 0, "", false);
        initializeSimulator(true);
    }

    public synchronized void increaseTraffic() {
        synchronizeTrafficFlow();
        currentState.increaseTraffic(this);
    }

    public synchronized void reduceTraffic() {
        synchronizeTrafficFlow();
        currentState.reduceTraffic(this);
    }

    public synchronized void reportAccident() {
        synchronizeTrafficFlow();
        currentState.reportAccident(this);
    }

    public synchronized void clearAccident() {
        synchronizeTrafficFlow();
        currentState.clearAccident(this);
    }

    public synchronized void advanceSimulation() {
        synchronizeTrafficFlow();
        currentState.advanceSimulation(this);
    }

    public synchronized void reset() {
        cars.clear();
        logs.clear();
        stateTransitions.clear();
        decisionLogs.clear();
        trafficLights.clear();
        laneStatuses.clear();
        blockedLane = -1;
        lastActionTrace = "Action: reset handled by TrafficSimulator \u2192 Transition to FluentTrafficState";
        lastMovementTimestamp = System.currentTimeMillis();
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

    public synchronized void updateCars(int speed, boolean accidentActive) {
        synchronizeTrafficFlow();

        for (Car car : cars) {
            car.setBlocked(false);
            car.setSpeed(speed);
        }

        if (accidentActive) {
            applyAccidentTrafficPattern(speed);
        } else if (hasPriorityLane()) {
            applyPriorityLaneTrafficPattern(speed);
        } else {
            applyNormalTrafficPattern(speed);
        }

        roadStatus.setAccidentActive(accidentActive);
        updateLaneStatuses();
        refreshAverageSpeedAndCongestionLevel();
        lastMovementTimestamp = System.currentTimeMillis();
    }

    public synchronized List<Car> getCars() {
        synchronizeTrafficFlow();
        return Collections.unmodifiableList(new ArrayList<>(cars));
    }

    public synchronized RoadStatus getRoadStatus() {
        synchronizeTrafficFlow();
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

    public synchronized List<TrafficLight> getTrafficLights() {
        List<TrafficLight> copies = new ArrayList<>();

        for (TrafficLight trafficLight : trafficLights) {
            copies.add(new TrafficLight(
                    trafficLight.getId(),
                    trafficLight.getLane(),
                    trafficLight.getColor(),
                    trafficLight.getRemainingSeconds()
            ));
        }

        return Collections.unmodifiableList(copies);
    }

    public synchronized List<LaneStatus> getLaneStatuses() {
        List<LaneStatus> copies = new ArrayList<>();

        for (LaneStatus laneStatus : laneStatuses) {
            copies.add(new LaneStatus(
                    laneStatus.getLaneNumber(),
                    laneStatus.isBlocked(),
                    laneStatus.isPriority(),
                    laneStatus.getVehicleCount(),
                    laneStatus.getAverageSpeed()
            ));
        }

        return Collections.unmodifiableList(copies);
    }

    public synchronized List<String> getDecisionLogs() {
        return Collections.unmodifiableList(new ArrayList<>(decisionLogs));
    }

    public synchronized int getBlockedLane() {
        return blockedLane;
    }

    public synchronized TrafficState getCurrentState() {
        synchronizeTrafficFlow();
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

    public synchronized void addDecisionLog(String decision) {
        decisionLogs.add(decision);

        if (decisionLogs.size() > MAX_DECISION_LOGS) {
            decisionLogs.remove(0);
        }
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

    public synchronized int findLaneWithHighestVehicleCount() {
        updateLaneStatuses();

        int selectedLane = 1;
        int highestVehicleCount = -1;

        for (LaneStatus laneStatus : laneStatuses) {
            if (laneStatus.getVehicleCount() > highestVehicleCount) {
                highestVehicleCount = laneStatus.getVehicleCount();
                selectedLane = laneStatus.getLaneNumber();
            }
        }

        return selectedLane;
    }

    public synchronized void enableAllLanes() {
        blockedLane = -1;

        for (LaneStatus laneStatus : laneStatuses) {
            laneStatus.setBlocked(false);
        }
    }

    public synchronized void clearLanePriorities() {
        for (LaneStatus laneStatus : laneStatuses) {
            laneStatus.setPriority(false);
        }
    }

    public synchronized void assignPriorityLane(int laneNumber) {
        clearLanePriorities();

        for (LaneStatus laneStatus : laneStatuses) {
            laneStatus.setPriority(laneStatus.getLaneNumber() == laneNumber);
        }
    }

    public synchronized void blockLane(int laneNumber) {
        blockedLane = laneNumber;

        for (LaneStatus laneStatus : laneStatuses) {
            laneStatus.setBlocked(laneStatus.getLaneNumber() == laneNumber);
        }
    }

    public synchronized void restoreNormalTrafficLights() {
        for (TrafficLight trafficLight : trafficLights) {
            trafficLight.setColor("GREEN");
            trafficLight.setRemainingSeconds(18);
        }
    }

    public synchronized void prioritizeTrafficLightForLane(int laneNumber) {
        for (TrafficLight trafficLight : trafficLights) {
            if (trafficLight.getLane() == laneNumber) {
                trafficLight.setColor("GREEN");
                trafficLight.setRemainingSeconds(26);
            } else {
                trafficLight.setColor("YELLOW");
                trafficLight.setRemainingSeconds(9);
            }
        }
    }

    public synchronized void restrictBlockedLaneTrafficLight(int laneNumber) {
        for (TrafficLight trafficLight : trafficLights) {
            if (trafficLight.getLane() == laneNumber) {
                trafficLight.setColor("RED");
                trafficLight.setRemainingSeconds(30);
            } else {
                trafficLight.setColor("GREEN");
                trafficLight.setRemainingSeconds(14);
            }
        }
    }

    public synchronized void restoreRecoveryTrafficLights() {
        for (TrafficLight trafficLight : trafficLights) {
            trafficLight.setColor("GREEN");
            trafficLight.setRemainingSeconds(16);
        }
    }

    public synchronized void updateLaneStatuses() {
        ensureLaneStatusInfrastructure();

        for (LaneStatus laneStatus : laneStatuses) {
            int vehicleCount = 0;
            int accumulatedSpeed = 0;

            for (Car car : cars) {
                if (car.getLane() == laneStatus.getLaneNumber()) {
                    vehicleCount++;
                    accumulatedSpeed += car.getSpeed();
                }
            }

            laneStatus.setVehicleCount(vehicleCount);
            laneStatus.setAverageSpeed(vehicleCount == 0 ? 0 : accumulatedSpeed / vehicleCount);
            laneStatus.setBlocked(laneStatus.getLaneNumber() == blockedLane);
        }
    }

    private void pauseDemoStep() {
        try {
            Thread.sleep(DEMO_STEP_DELAY_MS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private void initializeSimulator(boolean addInitializationLog) {
        initializeLaneStatuses();
        initializeTrafficLights();
        loadDefaultCars();
        currentState = new FluentTrafficState();
        refreshRoadStatus();
        updateCars(DEFAULT_SPEED, false);
        lastMovementTimestamp = System.currentTimeMillis();
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

    private void refreshAverageSpeedAndCongestionLevel() {
        int accumulatedSpeed = 0;
        boolean anyBlockedCars = false;

        for (Car car : cars) {
            accumulatedSpeed += car.getSpeed();
            anyBlockedCars = anyBlockedCars || car.isBlocked();
        }

        int averageSpeed = cars.isEmpty() ? 0 : accumulatedSpeed / cars.size();
        roadStatus.setAverageSpeed(averageSpeed);
        roadStatus.setCongestionLevel(resolveCongestionLevel(averageSpeed, anyBlockedCars));
    }

    private String resolveCongestionLevel(int speed, boolean blocked) {
        if (blocked) {
            return "Critical";
        }

        if (speed >= 80) {
            return "Low";
        }

        if (speed >= 45) {
            return "Medium";
        }

        return "High";
    }

    private void initializeTrafficLights() {
        for (int lane = 1; lane <= LANE_COUNT; lane++) {
            trafficLights.add(new TrafficLight("TL-" + lane, lane, "GREEN", 18));
        }
    }

    private void initializeLaneStatuses() {
        for (int lane = 1; lane <= LANE_COUNT; lane++) {
            laneStatuses.add(new LaneStatus(lane, false, false, 0, DEFAULT_SPEED));
        }
    }

    private void ensureLaneStatusInfrastructure() {
        if (laneStatuses.isEmpty()) {
            initializeLaneStatuses();
        }
    }

    private void loadDefaultCars() {
        cars.add(new Car("CAR-001", 1, 0, DEFAULT_SPEED, false));
        cars.add(new Car("CAR-002", 2, 140, DEFAULT_SPEED, false));
        cars.add(new Car("CAR-003", 3, 280, DEFAULT_SPEED, false));
        cars.add(new Car("CAR-004", 1, 420, DEFAULT_SPEED, false));
        cars.add(new Car("CAR-005", 2, 560, DEFAULT_SPEED, false));
        cars.add(new Car("CAR-006", 3, 680, DEFAULT_SPEED, false));
    }

    private void synchronizeTrafficFlow() {
        long currentTimestamp = System.currentTimeMillis();
        long elapsedMilliseconds = currentTimestamp - lastMovementTimestamp;

        if (elapsedMilliseconds <= 0) {
            return;
        }

        double elapsedSeconds = elapsedMilliseconds / 1000.0;

        for (Car car : cars) {
            if (car.getSpeed() <= 0 || car.isBlocked()) {
                continue;
            }

            double advancedPosition = car.getXPosition() + (car.getSpeed() * elapsedSeconds * MOVEMENT_SCALE);
            int wrappedPosition = (int) Math.round(advancedPosition % ROAD_LENGTH);
            car.setXPosition(wrappedPosition);
        }

        decreaseTrafficLightTimers(elapsedSeconds);
        updateLaneStatuses();
        refreshAverageSpeedAndCongestionLevel();
        lastMovementTimestamp = currentTimestamp;
    }

    private void decreaseTrafficLightTimers(double elapsedSeconds) {
        int elapsedWholeSeconds = (int) Math.floor(elapsedSeconds);

        if (elapsedWholeSeconds <= 0) {
            return;
        }

        for (TrafficLight trafficLight : trafficLights) {
            int remaining = trafficLight.getRemainingSeconds() - elapsedWholeSeconds;
            trafficLight.setRemainingSeconds(Math.max(1, remaining));
        }
    }

    private void applyNormalTrafficPattern(int speed) {
        for (Car car : cars) {
            car.setBlocked(false);

            if (car.getLane() == 1) {
                car.setSpeed(speed);
            } else if (car.getLane() == 2) {
                car.setSpeed(Math.max(speed - 8, MODERATE_SPEED));
            } else {
                car.setSpeed(Math.max(speed - 4, MODERATE_SPEED));
            }
        }
    }

    private void applyPriorityLaneTrafficPattern(int speed) {
        int priorityLane = getPriorityLaneNumber();

        for (Car car : cars) {
            car.setBlocked(false);

            if (car.getLane() == priorityLane) {
                car.setSpeed(Math.max(speed + 8, MODERATE_SPEED + 10));
            } else {
                car.setSpeed(Math.max(speed - 6, 18));
            }
        }
    }

    private void applyAccidentTrafficPattern(int speed) {
        for (Car car : cars) {
            car.setBlocked(false);

            if (car.getLane() == blockedLane && isCarNearAccident(car)) {
                if (tryRedirectCarFromBlockedLane(car)) {
                    car.setBlocked(false);
                    car.setSpeed(Math.max(speed + 6, 16));
                } else {
                    car.setSpeed(0);
                    car.setBlocked(true);
                }
                continue;
            }

            if (car.getLane() == blockedLane && car.getXPosition() < (ACCIDENT_POSITION - ACCIDENT_APPROACH_DISTANCE)) {
                car.setSpeed(Math.max(speed, 6));
                continue;
            }

            if (car.getXPosition() > ACCIDENT_POSITION) {
                car.setSpeed(ACCIDENT_AFTER_ZONE_SPEED);
            } else {
                car.setSpeed(Math.max(speed, 10));
            }
        }
    }

    private boolean tryRedirectCarFromBlockedLane(Car car) {
        int targetLane = blockedLane == 2 ? 3 : 2;

        if (isLaneAvailableForRedirect(targetLane, car.getXPosition())) {
            car.setLane(targetLane);
            return true;
        }

        targetLane = blockedLane == 1 ? 2 : 1;

        if (isLaneAvailableForRedirect(targetLane, car.getXPosition())) {
            car.setLane(targetLane);
            return true;
        }

        return false;
    }

    private boolean isLaneAvailableForRedirect(int laneNumber, int position) {
        for (Car car : cars) {
            if (car.getLane() == laneNumber && Math.abs(car.getXPosition() - position) < 80) {
                return false;
            }
        }

        return true;
    }

    private boolean isCarNearAccident(Car car) {
        double position = car.getXPosition();
        return position >= (ACCIDENT_POSITION - ACCIDENT_APPROACH_DISTANCE) && position <= ACCIDENT_POSITION;
    }

    private boolean hasPriorityLane() {
        for (LaneStatus laneStatus : laneStatuses) {
            if (laneStatus.isPriority()) {
                return true;
            }
        }

        return false;
    }

    private int getPriorityLaneNumber() {
        for (LaneStatus laneStatus : laneStatuses) {
            if (laneStatus.isPriority()) {
                return laneStatus.getLaneNumber();
            }
        }

        return 1;
    }

    public int getDefaultSpeed() {
        return DEFAULT_SPEED;
    }

    public int getModerateSpeed() {
        return MODERATE_SPEED;
    }

    public int getRecoverySpeed() {
        return RECOVERY_SPEED;
    }

    public int getCriticalSpeed() {
        return CRITICAL_SPEED;
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
