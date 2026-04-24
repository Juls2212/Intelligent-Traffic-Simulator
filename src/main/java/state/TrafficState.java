package state;

import context.TrafficSimulator;

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
