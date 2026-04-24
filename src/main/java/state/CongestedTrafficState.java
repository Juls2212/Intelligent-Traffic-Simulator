package state;

import context.TrafficSimulator;

/**
 * Concrete State representing slow traffic caused by congestion.
 */
public class CongestedTrafficState implements TrafficState {
    private static final int LOW_SPEED = 25;
    private static final int RECOVERY_SPEED = 50;
    private static final int CRITICAL_SPEED = 5;

    @Override
    public void increaseTraffic(TrafficSimulator simulator) {
        simulator.recordStateHandling("increaseTraffic", "CongestedTrafficState", "Remains in CongestedTrafficState");
        simulator.updateCars(LOW_SPEED, false);
    }

    @Override
    public void reduceTraffic(TrafficSimulator simulator) {
        simulator.recordStateHandling("reduceTraffic", "CongestedTrafficState", "Transition to ClearedTrafficState");
        simulator.setState(new ClearedTrafficState());
        simulator.updateCars(RECOVERY_SPEED, false);
    }

    @Override
    public void reportAccident(TrafficSimulator simulator) {
        simulator.recordStateHandling("reportAccident", "CongestedTrafficState", "Transition to AccidentTrafficState");
        simulator.setState(new AccidentTrafficState());
        simulator.updateCars(CRITICAL_SPEED, true);
    }

    @Override
    public void clearAccident(TrafficSimulator simulator) {
        simulator.recordStateHandling("clearAccident", "CongestedTrafficState", "Remains in CongestedTrafficState");
    }

    @Override
    public void advanceSimulation(TrafficSimulator simulator) {
        simulator.recordStateHandling("advanceSimulation", "CongestedTrafficState", "Remains in CongestedTrafficState");
        simulator.updateCars(LOW_SPEED, false);
    }

    @Override
    public String getStateName() {
        return "CongestedTrafficState";
    }

    @Override
    public String getSpanishStateName() {
        return "Trafico congestionado";
    }

    @Override
    public String getDescription() {
        return "Cars move slowly because the road is heavily occupied.";
    }
}
