package server;

import context.TrafficSimulator;
import model.RoadStatus;

/**
 * Console view used to demonstrate the simulator without the web interface.
 */
public class TrafficConsoleServer {
    public void printSnapshot(String title, TrafficSimulator simulator) {
        RoadStatus roadStatus = simulator.getRoadStatus();

        System.out.println("==============================================");
        System.out.println(title);
        System.out.println("Estado actual: " + roadStatus.getSpanishStateName());
        System.out.println("Clase del estado: " + roadStatus.getStateName());
        System.out.println("Descripcion: " + translateDescription(roadStatus));
        System.out.println("Velocidad promedio: " + roadStatus.getAverageSpeed() + " km/h");
        System.out.println("Nivel de congestion: " + translateCongestionLevel(roadStatus.getCongestionLevel()));
        System.out.println("Accidente activo: " + (roadStatus.isAccidentActive() ? "Si" : "No"));
        System.out.println("Registros recientes:");

        for (String log : simulator.getLogs()) {
            System.out.println("- " + translateLog(log));
        }

        System.out.println();
    }

    private String translateDescription(RoadStatus roadStatus) {
        String stateName = roadStatus.getStateName();

        if ("FluentTrafficState".equals(stateName)) {
            return "Los vehiculos circulan rapido y la via opera con normalidad.";
        }

        if ("CongestedTrafficState".equals(stateName)) {
            return "Los vehiculos avanzan lentamente por la alta ocupacion de la via.";
        }

        if ("AccidentTrafficState".equals(stateName)) {
            return "Los vehiculos se detienen o avanzan muy lentamente por un accidente.";
        }

        return "La via se encuentra en proceso de recuperacion despues de la congestion o un accidente.";
    }

    private String translateCongestionLevel(String congestionLevel) {
        if ("Low".equals(congestionLevel)) {
            return "Bajo";
        }

        if ("Medium".equals(congestionLevel)) {
            return "Medio";
        }

        if ("High".equals(congestionLevel)) {
            return "Alto";
        }

        return "Critico";
    }

    private String translateLog(String log) {
        if ("Simulator initialized in fluent traffic state.".equals(log)) {
            return "El simulador se inicializo en estado de trafico fluido.";
        }

        if ("Traffic volume increased. The road is now becoming congested.".equals(log)) {
            return "El volumen de trafico aumento. La via comienza a congestionarse.";
        }

        if ("State changed to CongestedTrafficState.".equals(log)) {
            return "El estado cambio a CongestedTrafficState.";
        }

        if ("An accident occurred during congestion. Traffic switches to accident mode.".equals(log)) {
            return "Ocurrio un accidente durante la congestion. El trafico cambia al modo de accidente.";
        }

        if ("State changed to AccidentTrafficState.".equals(log)) {
            return "El estado cambio a AccidentTrafficState.";
        }

        if ("Reducing traffic volume does not solve the active accident.".equals(log)) {
            return "Reducir el volumen de trafico no resuelve el accidente activo.";
        }

        if ("Emergency response cleared the accident. The road moves into recovery.".equals(log)) {
            return "La emergencia despejo el accidente. La via entra en recuperacion.";
        }

        if ("State changed to ClearedTrafficState.".equals(log)) {
            return "El estado cambio a ClearedTrafficState.";
        }

        if ("Simulation advanced in recovery mode. Traffic returns to fluent conditions.".equals(log)) {
            return "La simulacion avanzo en modo de recuperacion. El trafico vuelve a condiciones fluidas.";
        }

        if ("State changed to FluentTrafficState.".equals(log)) {
            return "El estado cambio a FluentTrafficState.";
        }

        return log;
    }
}
