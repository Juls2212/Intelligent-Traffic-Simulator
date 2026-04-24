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
        simulator.setLastActionTrace("Action: increaseTraffic handled by FluentTrafficState \u2192 Transition to CongestedTrafficState");
        simulator.addLog("Traffic volume increased. The road is now becoming congested.");
        simulator.setState(new CongestedTrafficState());
        simulator.updateCars(LOW_SPEED, false);
    }

    @Override
    public void reduceTraffic(TrafficSimulator simulator) {
        simulator.setLastActionTrace("Action: reduceTraffic handled by FluentTrafficState \u2192 Remains in FluentTrafficState");
        simulator.addLog("Traffic reduction requested, but traffic is already fluent.");
        simulator.updateCars(HIGH_SPEED, false);
    }

    @Override
    public void reportAccident(TrafficSimulator simulator) {
        simulator.setLastActionTrace("Action: reportAccident handled by FluentTrafficState \u2192 Transition to AccidentTrafficState");
        simulator.addLog("An accident was reported. Traffic flow switches to accident mode.");
        simulator.setState(new AccidentTrafficState());
        simulator.updateCars(CRITICAL_SPEED, true);
    }

    @Override
    public void clearAccident(TrafficSimulator simulator) {
        simulator.setLastActionTrace("Action: clearAccident handled by FluentTrafficState \u2192 Remains in FluentTrafficState");
        simulator.addLog("No active accident exists to clear.");
    }

    @Override
    public void advanceSimulation(TrafficSimulator simulator) {
        simulator.setLastActionTrace("Action: advanceSimulation handled by FluentTrafficState \u2192 Remains in FluentTrafficState");
        simulator.addLog("Simulation advanced with high-speed traffic flow.");
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
