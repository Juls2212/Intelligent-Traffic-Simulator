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
        simulator.setLastActionTrace("Action: increaseTraffic handled by CongestedTrafficState \u2192 Remains in CongestedTrafficState");
        simulator.addLog("Traffic volume increased further, but the system remains congested.");
        simulator.updateCars(LOW_SPEED, false);
    }

    @Override
    public void reduceTraffic(TrafficSimulator simulator) {
        simulator.setLastActionTrace("Action: reduceTraffic handled by CongestedTrafficState \u2192 Transition to ClearedTrafficState");
        simulator.addLog("Traffic volume reduced. The road enters a recovery phase.");
        simulator.setState(new ClearedTrafficState());
        simulator.updateCars(RECOVERY_SPEED, false);
    }

    @Override
    public void reportAccident(TrafficSimulator simulator) {
        simulator.setLastActionTrace("Action: reportAccident handled by CongestedTrafficState \u2192 Transition to AccidentTrafficState");
        simulator.addLog("An accident occurred during congestion. Traffic switches to accident mode.");
        simulator.setState(new AccidentTrafficState());
        simulator.updateCars(CRITICAL_SPEED, true);
    }

    @Override
    public void clearAccident(TrafficSimulator simulator) {
        simulator.setLastActionTrace("Action: clearAccident handled by CongestedTrafficState \u2192 Remains in CongestedTrafficState");
        simulator.addLog("There is no accident to clear in the congested state.");
    }

    @Override
    public void advanceSimulation(TrafficSimulator simulator) {
        simulator.setLastActionTrace("Action: advanceSimulation handled by CongestedTrafficState \u2192 Remains in CongestedTrafficState");
        simulator.addLog("Simulation advanced with slow vehicle movement due to congestion.");
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
