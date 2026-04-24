package state;

import context.TrafficSimulator;

/**
 * State interface of the State design pattern.
 * Each implementation defines how the simulator reacts to the same actions.
 */
public interface TrafficState {
    void increaseTraffic(TrafficSimulator simulator);

    void reduceTraffic(TrafficSimulator simulator);

    void reportAccident(TrafficSimulator simulator);

    void clearAccident(TrafficSimulator simulator);

    void advanceSimulation(TrafficSimulator simulator);

    String getStateName();

    String getSpanishStateName();

    String getDescription();
}
