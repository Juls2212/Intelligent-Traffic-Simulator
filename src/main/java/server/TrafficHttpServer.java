package server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import context.TrafficSimulator;
import model.Car;
import model.RoadStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * HTTP adapter that exposes the TrafficSimulator context to the web frontend.
 * This class only handles transport concerns and delegates business actions to the Context.
 * No State-specific decision is implemented here.
 */
public class TrafficHttpServer {
    private final TrafficSimulator simulator;
    private final HttpServer server;
    private final Path publicDirectory;

    public TrafficHttpServer(TrafficSimulator simulator, int port) throws IOException {
        this.simulator = simulator;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.publicDirectory = Path.of("public").toAbsolutePath().normalize();
        registerContexts();
    }

    public void start() {
        server.setExecutor(null);
        server.start();
    }

    private void registerContexts() {
        server.createContext("/api/status", exchange -> {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange, "GET");
                return;
            }

            sendJson(exchange, buildStatusJson());
        });

        server.createContext("/api/increase-traffic", createActionHandler(() -> simulator.increaseTraffic()));
        server.createContext("/api/reduce-traffic", createActionHandler(() -> simulator.reduceTraffic()));
        server.createContext("/api/report-accident", createActionHandler(() -> simulator.reportAccident()));
        server.createContext("/api/clear-accident", createActionHandler(() -> simulator.clearAccident()));
        server.createContext("/api/advance", createActionHandler(() -> simulator.advanceSimulation()));
        server.createContext("/api/reset", createActionHandler(() -> simulator.reset()));
        server.createContext("/api/demo", createActionHandler(() -> simulator.runDemoSequence()));
        server.createContext("/", new StaticFileHandler());
    }

    private HttpHandler createActionHandler(SimulatorAction action) {
        return exchange -> {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange, "POST");
                return;
            }

            action.execute();
            sendJson(exchange, buildStatusJson());
        };
    }

    private String buildStatusJson() {
        RoadStatus status = simulator.getRoadStatus();
        List<Car> cars = simulator.getCars();
        List<String> logs = simulator.getLogs();
        List<String> stateTransitions = simulator.getStateTransitions();

        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append("\"currentState\":\"").append(escapeJson(status.getStateName())).append("\",");
        builder.append("\"spanishStateName\":\"").append(escapeJson(status.getSpanishStateName())).append("\",");
        builder.append("\"description\":\"").append(escapeJson(status.getDescription())).append("\",");
        builder.append("\"activeStateClass\":\"").append(escapeJson(status.getActiveStateClass())).append("\",");
        builder.append("\"lastActionTrace\":\"").append(escapeJson(status.getLastActionTrace())).append("\",");
        builder.append("\"averageSpeed\":").append(status.getAverageSpeed()).append(",");
        builder.append("\"congestionLevel\":\"").append(escapeJson(status.getCongestionLevel())).append("\",");
        builder.append("\"accidentActive\":").append(status.isAccidentActive()).append(",");
        builder.append("\"cars\":").append(buildCarsJson(cars)).append(",");
        builder.append("\"logs\":").append(buildLogsJson(logs)).append(",");
        builder.append("\"stateTransitions\":").append(buildLogsJson(stateTransitions));
        builder.append("}");
        return builder.toString();
    }

    private String buildCarsJson(List<Car> cars) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");

        for (int i = 0; i < cars.size(); i++) {
            Car car = cars.get(i);
            builder.append("{");
            builder.append("\"id\":\"").append(escapeJson(car.getId())).append("\",");
            builder.append("\"lane\":").append(car.getLane()).append(",");
            builder.append("\"xPosition\":").append(car.getXPosition()).append(",");
            builder.append("\"speed\":").append(car.getSpeed()).append(",");
            builder.append("\"blocked\":").append(car.isBlocked());
            builder.append("}");

            if (i < cars.size() - 1) {
                builder.append(",");
            }
        }

        builder.append("]");
        return builder.toString();
    }

    private String buildLogsJson(List<String> logs) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");

        for (int i = 0; i < logs.size(); i++) {
            builder.append("\"").append(escapeJson(logs.get(i))).append("\"");

            if (i < logs.size() - 1) {
                builder.append(",");
            }
        }

        builder.append("]");
        return builder.toString();
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private void sendJson(HttpExchange exchange, String body) throws IOException {
        byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, responseBytes.length);

        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(responseBytes);
        }
    }

    private void sendMethodNotAllowed(HttpExchange exchange, String allowedMethod) throws IOException {
        exchange.getResponseHeaders().set("Allow", allowedMethod);
        sendText(exchange, 405, "Method Not Allowed");
    }

    private void sendNotFound(HttpExchange exchange) throws IOException {
        sendText(exchange, 404, "Not Found");
    }

    private void sendText(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(responseBytes);
        }
    }

    private String resolveContentType(Path filePath) {
        String fileName = filePath.getFileName().toString();

        if (fileName.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        }

        if (fileName.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }

        if (fileName.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        }

        return "application/octet-stream";
    }

    private interface SimulatorAction {
        void execute();
    }

    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange, "GET");
                return;
            }

            String requestPath = exchange.getRequestURI().getPath();
            Path filePath = resolveStaticFile(requestPath);

            if (filePath == null || !Files.exists(filePath) || Files.isDirectory(filePath)) {
                sendNotFound(exchange);
                return;
            }

            byte[] responseBytes = Files.readAllBytes(filePath);
            exchange.getResponseHeaders().set("Content-Type", resolveContentType(filePath));
            exchange.sendResponseHeaders(200, responseBytes.length);

            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(responseBytes);
            }
        }

        private Path resolveStaticFile(String requestPath) {
            String normalizedPath = "/".equals(requestPath) ? "/index.html" : requestPath;
            String relativePath = normalizedPath.startsWith("/") ? normalizedPath.substring(1) : normalizedPath;
            Path resolvedPath = publicDirectory.resolve(relativePath).normalize();

            if (!resolvedPath.startsWith(publicDirectory)) {
                return null;
            }

            return resolvedPath;
        }
    }
}
