package state;

import context.TrafficSimulator;

/**
 * Concrete State representing normal high-speed traffic flow.
 */
public class FluentTrafficState implements TrafficState {
    @Override
    public void increaseTraffic(TrafficSimulator simulator) {
        // Concrete State decides how the Context reacts to the same request.
        simulator.recordStateHandling("increaseTraffic", "FluentTrafficState", "Transition to CongestedTrafficState");
        simulator.setState(new CongestedTrafficState());
        int priorityLane = simulator.findLaneWithHighestVehicleCount();
        simulator.enableAllLanes();
        simulator.assignPriorityLane(priorityLane);
        simulator.prioritizeTrafficLightForLane(priorityLane);
        simulator.addDecisionLog("CongestedTrafficState gave priority to lane " + priorityLane + ".");
        simulator.updateCars(simulator.getModerateSpeed(), false);
    }

    @Override
    public void reduceTraffic(TrafficSimulator simulator) {
        simulator.recordStateHandling("reduceTraffic", "FluentTrafficState", "Remains in FluentTrafficState");
        simulator.enableAllLanes();
        simulator.clearLanePriorities();
        simulator.restoreNormalTrafficLights();
        simulator.addDecisionLog("FluentTrafficState maintained normal circulation.");
        simulator.updateCars(simulator.getDefaultSpeed(), false);
    }

    @Override
    public void reportAccident(TrafficSimulator simulator) {
        simulator.recordStateHandling("reportAccident", "FluentTrafficState", "Transition to AccidentTrafficState");
        simulator.setState(new AccidentTrafficState());
        simulator.createReportedAccidentScene();
    }

    @Override
    public void provokeAccident(TrafficSimulator simulator) {
        simulator.recordStateHandling("provokeAccident", "FluentTrafficState", "Transition to AccidentTrafficState");
        simulator.setState(new AccidentTrafficState());
        simulator.createProvokedAccidentScene();
    }

    @Override
    public void clearAccident(TrafficSimulator simulator) {
        simulator.recordStateHandling("clearAccident", "FluentTrafficState", "Remains in FluentTrafficState");
        simulator.enableAllLanes();
        simulator.restoreNormalTrafficLights();
        simulator.addDecisionLog("FluentTrafficState confirmed that all lanes were already available.");
    }

    @Override
    public void advanceSimulation(TrafficSimulator simulator) {
        simulator.recordStateHandling("advanceSimulation", "FluentTrafficState", "Remains in FluentTrafficState");
        simulator.enableAllLanes();
        simulator.clearLanePriorities();
        simulator.restoreNormalTrafficLights();
        simulator.addDecisionLog("FluentTrafficState maintained normal circulation.");
        simulator.updateCars(simulator.getDefaultSpeed(), false);
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
