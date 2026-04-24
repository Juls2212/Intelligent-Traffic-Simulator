import context.TrafficSimulator;
import server.TrafficConsoleServer;

/**
 * Optional console-only demonstration of the same Context and State collaboration.
 */
public class ConsoleDemo {
    public static void main(String[] args) {
        TrafficSimulator simulator = new TrafficSimulator();
        TrafficConsoleServer consoleServer = new TrafficConsoleServer();

        consoleServer.printSnapshot("Estado inicial", simulator);

        simulator.increaseTraffic();
        consoleServer.printSnapshot("Despues de increaseTraffic()", simulator);

        simulator.reportAccident();
        consoleServer.printSnapshot("Despues de reportAccident()", simulator);

        simulator.reduceTraffic();
        consoleServer.printSnapshot("Despues de reduceTraffic()", simulator);

        simulator.clearAccident();
        consoleServer.printSnapshot("Despues de clearAccident()", simulator);

        simulator.advanceSimulation();
        consoleServer.printSnapshot("Despues de advanceSimulation()", simulator);
    }
}
