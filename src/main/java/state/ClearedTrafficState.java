package state;

import context.TrafficSimulator;

public class ClearedTrafficState implements TrafficState {
    private static final int RECOVERY_SPEED = 50;
    private static final int LOW_SPEED = 25;
    private static final int HIGH_SPEED = 90;
    private static final int CRITICAL_SPEED = 5;

    @Override
    public void increaseTraffic(TrafficSimulator simulator) {
        simulator.addLog("Traffic increased during recovery. The road becomes congested again.");
        simulator.setState(new CongestedTrafficState());
        simulator.updateCars(LOW_SPEED, false);
    }

    @Override
    public void reduceTraffic(TrafficSimulator simulator) {
        simulator.addLog("Traffic reduction supports recovery and improves vehicle movement.");
        simulator.updateCars(RECOVERY_SPEED + 10, false);
    }

    @Override
    public void reportAccident(TrafficSimulator simulator) {
        simulator.addLog("A new accident was reported during recovery.");
        simulator.setState(new AccidentTrafficState());
        simulator.updateCars(CRITICAL_SPEED, true);
    }

    @Override
    public void clearAccident(TrafficSimulator simulator) {
        simulator.addLog("The road is already being cleared and recovered.");
    }

    @Override
    public void advanceSimulation(TrafficSimulator simulator) {
        simulator.addLog("Simulation advanced in recovery mode. Traffic returns to fluent conditions.");
        simulator.setState(new FluentTrafficState());
        simulator.updateCars(HIGH_SPEED, false);
    }

    @Override
    public String getStateName() {
        return "ClearedTrafficState";
    }

    @Override
    public String getSpanishStateName() {
        return "Via despejada";
    }

    @Override
    public String getDescription() {
        return "The road is recovering after congestion or an accident.";
    }
}
