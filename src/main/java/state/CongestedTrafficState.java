package state;

import context.TrafficSimulator;

/**
 * Concrete State representing slow traffic caused by congestion.
 */
public class CongestedTrafficState implements TrafficState {
    @Override
    public void increaseTraffic(TrafficSimulator simulator) {
        simulator.recordStateHandling("increaseTraffic", "CongestedTrafficState", "Remains in CongestedTrafficState");
        int priorityLane = simulator.findLaneWithHighestVehicleCount();
        simulator.enableAllLanes();
        simulator.assignPriorityLane(priorityLane);
        simulator.prioritizeTrafficLightForLane(priorityLane);
        simulator.addDecisionLog("CongestedTrafficState gave priority to lane " + priorityLane + ".");
        simulator.updateCars(simulator.getModerateSpeed(), false);
    }

    @Override
    public void reduceTraffic(TrafficSimulator simulator) {
        simulator.recordStateHandling("reduceTraffic", "CongestedTrafficState", "Transition to ClearedTrafficState");
        simulator.setState(new ClearedTrafficState());
        simulator.enableAllLanes();
        simulator.clearLanePriorities();
        simulator.restoreRecoveryTrafficLights();
        simulator.addDecisionLog("ClearedTrafficState restored normal circulation.");
        simulator.updateCars(simulator.getRecoverySpeed(), false);
    }

    @Override
    public void reportAccident(TrafficSimulator simulator) {
        simulator.recordStateHandling("reportAccident", "CongestedTrafficState", "Transition to AccidentTrafficState");
        simulator.setState(new AccidentTrafficState());
        simulator.createReportedAccidentScene();
    }

    @Override
    public void provokeAccident(TrafficSimulator simulator) {
        simulator.recordStateHandling("provokeAccident", "CongestedTrafficState", "Transition to AccidentTrafficState");
        simulator.setState(new AccidentTrafficState());
        simulator.createProvokedAccidentScene();
    }

    @Override
    public void clearAccident(TrafficSimulator simulator) {
        simulator.recordStateHandling("clearAccident", "CongestedTrafficState", "Remains in CongestedTrafficState");
        int priorityLane = simulator.findLaneWithHighestVehicleCount();
        simulator.assignPriorityLane(priorityLane);
        simulator.prioritizeTrafficLightForLane(priorityLane);
        simulator.addDecisionLog("CongestedTrafficState maintained adaptive congestion priority on lane " + priorityLane + ".");
    }

    @Override
    public void advanceSimulation(TrafficSimulator simulator) {
        simulator.recordStateHandling("advanceSimulation", "CongestedTrafficState", "Remains in CongestedTrafficState");
        int priorityLane = simulator.findLaneWithHighestVehicleCount();
        simulator.enableAllLanes();
        simulator.assignPriorityLane(priorityLane);
        simulator.prioritizeTrafficLightForLane(priorityLane);
        simulator.addDecisionLog("CongestedTrafficState gave priority to lane " + priorityLane + ".");
        simulator.updateCars(simulator.getModerateSpeed(), false);
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
