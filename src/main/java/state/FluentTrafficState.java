package state;

import context.TrafficSimulator;

public class FluentTrafficState implements TrafficState {
    private static final int HIGH_SPEED = 90;
    private static final int LOW_SPEED = 25;
    private static final int CRITICAL_SPEED = 5;

    @Override
    public void increaseTraffic(TrafficSimulator simulator) {
        simulator.addLog("Traffic volume increased. The road is now becoming congested.");
        simulator.setState(new CongestedTrafficState());
        simulator.updateCars(LOW_SPEED, false);
    }

    @Override
    public void reduceTraffic(TrafficSimulator simulator) {
        simulator.addLog("Traffic reduction requested, but traffic is already fluent.");
        simulator.updateCars(HIGH_SPEED, false);
    }

    @Override
    public void reportAccident(TrafficSimulator simulator) {
        simulator.addLog("An accident was reported. Traffic flow switches to accident mode.");
        simulator.setState(new AccidentTrafficState());
        simulator.updateCars(CRITICAL_SPEED, true);
    }

    @Override
    public void clearAccident(TrafficSimulator simulator) {
        simulator.addLog("No active accident exists to clear.");
    }

    @Override
    public void advanceSimulation(TrafficSimulator simulator) {
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
