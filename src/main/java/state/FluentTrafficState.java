package state;

import context.TrafficSimulator;

/**
 * Concrete State representing normal high-speed traffic flow.
 */
public class FluentTrafficState implements TrafficState {
    private static final int HIGH_SPEED = 90;
    private static final int LOW_SPEED = 25;
    private static final int CRITICAL_SPEED = 5;

    @Override
    public void increaseTraffic(TrafficSimulator simulator) {
        // Concrete State decides how the Context reacts to the same request.
        simulator.recordStateHandling("increaseTraffic", "FluentTrafficState", "Transition to CongestedTrafficState");
        simulator.setState(new CongestedTrafficState());
        simulator.updateCars(LOW_SPEED, false);
    }

    @Override
    public void reduceTraffic(TrafficSimulator simulator) {
        simulator.recordStateHandling("reduceTraffic", "FluentTrafficState", "Remains in FluentTrafficState");
        simulator.updateCars(HIGH_SPEED, false);
    }

    @Override
    public void reportAccident(TrafficSimulator simulator) {
        simulator.recordStateHandling("reportAccident", "FluentTrafficState", "Transition to AccidentTrafficState");
        simulator.setState(new AccidentTrafficState());
        simulator.updateCars(CRITICAL_SPEED, true);
    }

    @Override
    public void clearAccident(TrafficSimulator simulator) {
        simulator.recordStateHandling("clearAccident", "FluentTrafficState", "Remains in FluentTrafficState");
    }

    @Override
    public void advanceSimulation(TrafficSimulator simulator) {
        simulator.recordStateHandling("advanceSimulation", "FluentTrafficState", "Remains in FluentTrafficState");
        simulator.updateCars(HIGH_SPEED, false);
    }

    @Override
    public String getStateName() {
        return "FluentTrafficState";
    }

    @Override
    public String getSpanishStateName() {
        return "Trafico fluido";
    }

    @Override
    public String getDescription() {
        return "Cars move at high speed and the road operates normally.";
    }
}
