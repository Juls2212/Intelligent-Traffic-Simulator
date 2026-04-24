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
        if ("TrafficSimulator initialized the Context with FluentTrafficState as the starting State object.".equals(log)) {
            return "TrafficSimulator inicializo el Contexto con FluentTrafficState como objeto State inicial.";
        }

        if ("TrafficSimulator reset the simulation and restored FluentTrafficState as the initial academic baseline.".equals(log)) {
            return "TrafficSimulator reinicio la simulacion y restauro FluentTrafficState como linea base academica inicial.";
        }

        if ("TrafficSimulator started the automatic demonstration sequence to expose dynamic State-pattern behavior.".equals(log)) {
            return "TrafficSimulator inicio la secuencia automatica para exponer el comportamiento dinamico del patron State.";
        }

        if ("TrafficSimulator rejected a new demo request because the previous demonstration sequence is still running.".equals(log)) {
            return "TrafficSimulator rechazo una nueva solicitud de demostracion porque la secuencia anterior sigue en ejecucion.";
        }

        if ("TrafficSimulator finished the automatic demonstration sequence.".equals(log)) {
            return "TrafficSimulator finalizo la secuencia automatica de demostracion.";
        }

        if (log.startsWith("TrafficSimulator delegated ")) {
            return translateDelegationLog(log);
        }

        if (log.startsWith("TrafficSimulator updated its active state reference to ")) {
            return log
                    .replace("TrafficSimulator updated its active state reference to ", "TrafficSimulator actualizo su referencia de estado activo a ")
                    .replace(".", ".");
        }

        if (log.startsWith("State changed to ")) {
            return log.replace("State changed to ", "El estado cambio a ");
        }

        return log;
    }

    private String translateDelegationLog(String log) {
        String translated = log
                .replace("TrafficSimulator delegated ", "TrafficSimulator delego ")
                .replace(", which transitioned to ", ", y este realizo una transicion a ")
                .replace(", which remained in ", ", y este permanecio en ");

        if (translated.endsWith(".")) {
            return translated;
        }

        return translated + ".";
    }
}
