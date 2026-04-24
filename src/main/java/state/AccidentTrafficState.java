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
        simulator.recordStateHandling("increaseTraffic", "AccidentTrafficState", "Remains in AccidentTrafficState");
        simulator.updateCars(CRITICAL_SPEED, true);
    }

    @Override
    public void reduceTraffic(TrafficSimulator simulator) {
        simulator.recordStateHandling("reduceTraffic", "AccidentTrafficState", "Remains in AccidentTrafficState");
        simulator.updateCars(CRITICAL_SPEED, true);
    }

    @Override
    public void reportAccident(TrafficSimulator simulator) {
        simulator.recordStateHandling("reportAccident", "AccidentTrafficState", "Remains in AccidentTrafficState");
    }

    @Override
    public void clearAccident(TrafficSimulator simulator) {
        simulator.recordStateHandling("clearAccident", "AccidentTrafficState", "Transition to ClearedTrafficState");
        simulator.setState(new ClearedTrafficState());
        simulator.updateCars(RECOVERY_SPEED, false);
    }

    @Override
    public void advanceSimulation(TrafficSimulator simulator) {
        simulator.recordStateHandling("advanceSimulation", "AccidentTrafficState", "Remains in AccidentTrafficState");
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
