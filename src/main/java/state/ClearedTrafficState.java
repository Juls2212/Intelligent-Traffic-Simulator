package state;

import context.TrafficSimulator;

/**
 * Concrete State representing traffic recovery after congestion or an accident.
 */
public class ClearedTrafficState implements TrafficState {
    private static final int RECOVERY_SPEED = 50;
    private static final int LOW_SPEED = 25;
    private static final int HIGH_SPEED = 90;
    private static final int CRITICAL_SPEED = 5;

    @Override
    public void increaseTraffic(TrafficSimulator simulator) {
        simulator.recordStateHandling("increaseTraffic", "ClearedTrafficState", "Transition to CongestedTrafficState");
        simulator.setState(new CongestedTrafficState());
        simulator.updateCars(LOW_SPEED, false);
    }

    @Override
    public void reduceTraffic(TrafficSimulator simulator) {
        simulator.recordStateHandling("reduceTraffic", "ClearedTrafficState", "Remains in ClearedTrafficState");
        simulator.updateCars(RECOVERY_SPEED + 10, false);
    }

    @Override
    public void reportAccident(TrafficSimulator simulator) {
        simulator.recordStateHandling("reportAccident", "ClearedTrafficState", "Transition to AccidentTrafficState");
        simulator.setState(new AccidentTrafficState());
        simulator.updateCars(CRITICAL_SPEED, true);
    }

    @Override
    public void clearAccident(TrafficSimulator simulator) {
        simulator.recordStateHandling("clearAccident", "ClearedTrafficState", "Remains in ClearedTrafficState");
    }

    @Override
    public void advanceSimulation(TrafficSimulator simulator) {
        simulator.recordStateHandling("advanceSimulation", "ClearedTrafficState", "Transition to FluentTrafficState");
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
