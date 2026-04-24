package state;

import context.TrafficSimulator;

/**
 * Concrete State representing an active accident that blocks traffic flow.
 */
public class AccidentTrafficState implements TrafficState {
    @Override
    public void increaseTraffic(TrafficSimulator simulator) {
        simulator.recordStateHandling("increaseTraffic", "AccidentTrafficState", "Remains in AccidentTrafficState");
        int accidentLane = simulator.getBlockedLane() > 0 ? simulator.getBlockedLane() : 2;
        simulator.blockLane(accidentLane);
        simulator.restrictBlockedLaneTrafficLight(accidentLane);
        simulator.addDecisionLog("AccidentTrafficState blocked lane " + accidentLane + " and reduced speed.");
        simulator.updateCars(simulator.getCriticalSpeed(), true);
    }

    @Override
    public void reduceTraffic(TrafficSimulator simulator) {
        simulator.recordStateHandling("reduceTraffic", "AccidentTrafficState", "Remains in AccidentTrafficState");
        int accidentLane = simulator.getBlockedLane() > 0 ? simulator.getBlockedLane() : 2;
        simulator.blockLane(accidentLane);
        simulator.restrictBlockedLaneTrafficLight(accidentLane);
        simulator.addDecisionLog("AccidentTrafficState kept lane " + accidentLane + " blocked while emergency flow control remained active.");
        simulator.updateCars(simulator.getCriticalSpeed(), true);
    }

    @Override
    public void reportAccident(TrafficSimulator simulator) {
        simulator.recordStateHandling("reportAccident", "AccidentTrafficState", "Remains in AccidentTrafficState");
        int accidentLane = simulator.getBlockedLane() > 0 ? simulator.getBlockedLane() : 2;
        simulator.blockLane(accidentLane);
        simulator.restrictBlockedLaneTrafficLight(accidentLane);
        simulator.addDecisionLog("AccidentTrafficState confirmed that lane " + accidentLane + " remains blocked.");
    }

    @Override
    public void clearAccident(TrafficSimulator simulator) {
        simulator.recordStateHandling("clearAccident", "AccidentTrafficState", "Transition to ClearedTrafficState");
        simulator.setState(new ClearedTrafficState());
        simulator.enableAllLanes();
        simulator.clearLanePriorities();
        simulator.restoreRecoveryTrafficLights();
        simulator.addDecisionLog("ClearedTrafficState restored normal circulation.");
        simulator.updateCars(simulator.getRecoverySpeed(), false);
    }

    @Override
    public void advanceSimulation(TrafficSimulator simulator) {
        simulator.recordStateHandling("advanceSimulation", "AccidentTrafficState", "Remains in AccidentTrafficState");
        int accidentLane = simulator.getBlockedLane() > 0 ? simulator.getBlockedLane() : 2;
        simulator.blockLane(accidentLane);
        simulator.restrictBlockedLaneTrafficLight(accidentLane);
        simulator.addDecisionLog("AccidentTrafficState blocked lane " + accidentLane + " and redirected vehicles.");
        simulator.updateCars(simulator.getCriticalSpeed(), true);
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
