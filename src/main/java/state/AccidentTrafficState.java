package state;

import context.TrafficSimulator;

/**
 * Concrete State representing an active accident that blocks traffic flow.
 */
public class AccidentTrafficState implements TrafficState {
    private static final int CRITICAL_SPEED = 5;
    private static final int RECOVERY_SPEED = 50;

    @Override
    public void increaseTraffic(TrafficSimulator simulator) {
        simulator.addLog("Traffic increased around the accident area, making congestion worse.");
        simulator.updateCars(CRITICAL_SPEED, true);
    }

    @Override
    public void reduceTraffic(TrafficSimulator simulator) {
        simulator.addLog("Reducing traffic volume does not solve the active accident.");
        simulator.updateCars(CRITICAL_SPEED, true);
    }

    @Override
    public void reportAccident(TrafficSimulator simulator) {
        simulator.addLog("The accident state is already active.");
    }

    @Override
    public void clearAccident(TrafficSimulator simulator) {
        simulator.addLog("Emergency response cleared the accident. The road moves into recovery.");
        simulator.setState(new ClearedTrafficState());
        simulator.updateCars(RECOVERY_SPEED, false);
    }

    @Override
    public void advanceSimulation(TrafficSimulator simulator) {
        simulator.addLog("Simulation advanced with blocked traffic near the accident area.");
        simulator.updateCars(CRITICAL_SPEED, true);
    }

    @Override
    public String getStateName() {
        return "AccidentTrafficState";
    }

    @Override
    public String getSpanishStateName() {
        return "Accidente en la via";
    }

    @Override
    public String getDescription() {
        return "Cars stop or move extremely slowly because an accident blocks the road.";
    }
}
