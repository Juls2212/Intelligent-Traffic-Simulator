package state;

import context.TrafficSimulator;

/**
 * Concrete State representing traffic recovery after congestion or an accident.
 */
public class ClearedTrafficState implements TrafficState {
    @Override
    public void increaseTraffic(TrafficSimulator simulator) {
        simulator.recordStateHandling("increaseTraffic", "ClearedTrafficState", "Transition to CongestedTrafficState");
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
        simulator.recordStateHandling("reduceTraffic", "ClearedTrafficState", "Remains in ClearedTrafficState");
        simulator.enableAllLanes();
        simulator.clearLanePriorities();
        simulator.restoreRecoveryTrafficLights();
        simulator.addDecisionLog("ClearedTrafficState restored normal circulation.");
        simulator.updateCars(simulator.getRecoverySpeed() + 10, false);
    }

    @Override
    public void reportAccident(TrafficSimulator simulator) {
        simulator.recordStateHandling("reportAccident", "ClearedTrafficState", "Transition to AccidentTrafficState");
        simulator.setState(new AccidentTrafficState());
        simulator.createReportedAccidentScene();
    }

    @Override
    public void provokeAccident(TrafficSimulator simulator) {
        simulator.recordStateHandling("provokeAccident", "ClearedTrafficState", "Transition to AccidentTrafficState");
        simulator.setState(new AccidentTrafficState());
        simulator.createProvokedAccidentScene();
    }

    @Override
    public void clearAccident(TrafficSimulator simulator) {
        simulator.recordStateHandling("clearAccident", "ClearedTrafficState", "Remains in ClearedTrafficState");
        simulator.enableAllLanes();
        simulator.restoreRecoveryTrafficLights();
        simulator.addDecisionLog("ClearedTrafficState kept recovery controls active.");
    }

    @Override
    public void advanceSimulation(TrafficSimulator simulator) {
        simulator.recordStateHandling("advanceSimulation", "ClearedTrafficState", "Transition to FluentTrafficState");
        simulator.setState(new FluentTrafficState());
        simulator.enableAllLanes();
        simulator.clearLanePriorities();
        simulator.restoreNormalTrafficLights();
        simulator.addDecisionLog("FluentTrafficState maintained normal circulation.");
        simulator.updateCars(simulator.getDefaultSpeed(), false);
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
