import context.TrafficSimulator;
import server.TrafficHttpServer;

/**
 * Application entry point.
 * It only wires the Context to the HTTP adapter.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        TrafficSimulator simulator = new TrafficSimulator();
        TrafficHttpServer server = new TrafficHttpServer(simulator, 8080);
        server.start();
        System.out.println("Server running at http://localhost:8080");
    }
}
