const stateNameElement = document.getElementById("state-name");
const stateClassElement = document.getElementById("state-class");
const averageSpeedElement = document.getElementById("average-speed");
const congestionLevelElement = document.getElementById("congestion-level");
const accidentActiveElement = document.getElementById("accident-active");
const stateDescriptionElement = document.getElementById("state-description");
const carsContainerElement = document.getElementById("cars-container");
const logsListElement = document.getElementById("logs-list");
const actionButtons = Array.from(document.querySelectorAll("[data-action]"));

document.addEventListener("DOMContentLoaded", () => {
    bindActions();
    loadStatus();
});

function bindActions() {
    actionButtons.forEach((button) => {
        button.addEventListener("click", async () => {
            setButtonsDisabled(true);

            try {
                const response = await fetch(button.dataset.action, { method: "POST" });

                if (!response.ok) {
                    throw new Error("Request failed");
                }

                const status = await response.json();
                renderStatus(status);
            } catch (error) {
                alert("No se pudo actualizar la simulacion.");
            } finally {
                setButtonsDisabled(false);
            }
        });
    });
}

async function loadStatus() {
    try {
        const response = await fetch("/api/status");

        if (!response.ok) {
            throw new Error("Request failed");
        }

        const status = await response.json();
        renderStatus(status);
    } catch (error) {
        stateNameElement.textContent = "Error de carga";
        stateDescriptionElement.textContent = "No fue posible obtener el estado del simulador.";
    }
}

function renderStatus(status) {
    stateNameElement.textContent = status.spanishStateName;
    stateClassElement.textContent = status.currentState;
    averageSpeedElement.textContent = `${status.averageSpeed} km/h`;
    congestionLevelElement.textContent = translateCongestionLevel(status.congestionLevel);
    accidentActiveElement.textContent = status.accidentActive ? "Activo" : "No activo";
    stateDescriptionElement.textContent = translateDescription(status.currentState, status.description);

    renderCars(status.cars);
    renderLogs(status.logs);
}

function renderCars(cars) {
    carsContainerElement.innerHTML = "";

    cars.forEach((car) => {
        const card = document.createElement("article");
        card.className = `car-card${car.blocked ? " blocked" : ""}`;
        card.innerHTML = `
            <span class="label">${car.id}</span>
            <strong>Carril ${car.lane}</strong>
            <p>Posicion: ${car.xPosition}</p>
            <p>Velocidad: ${car.speed} km/h</p>
            <p>Bloqueado: ${car.blocked ? "Si" : "No"}</p>
        `;
        carsContainerElement.appendChild(card);
    });
}

function renderLogs(logs) {
    logsListElement.innerHTML = "";

    logs.forEach((log) => {
        const item = document.createElement("li");
        item.textContent = translateLog(log);
        logsListElement.appendChild(item);
    });
}

function setButtonsDisabled(disabled) {
    actionButtons.forEach((button) => {
        button.disabled = disabled;
    });
}

function translateDescription(currentState, fallbackDescription) {
    const descriptions = {
        FluentTrafficState: "Los vehiculos circulan rapido y la via opera con normalidad.",
        CongestedTrafficState: "Los vehiculos avanzan lentamente por la alta ocupacion de la via.",
        AccidentTrafficState: "Los vehiculos se detienen o avanzan muy lentamente por un accidente.",
        ClearedTrafficState: "La via se encuentra en proceso de recuperacion despues de la congestion o un accidente."
    };

    return descriptions[currentState] || fallbackDescription;
}

function translateCongestionLevel(congestionLevel) {
    const levels = {
        Low: "Bajo",
        Medium: "Medio",
        High: "Alto",
        Critical: "Critico"
    };

    return levels[congestionLevel] || congestionLevel;
}

function translateLog(log) {
    const translations = {
        "Simulator initialized in fluent traffic state.": "El simulador se inicializo en estado de trafico fluido.",
        "Simulator reset to the initial fluent state.": "El simulador se reinicio al estado inicial de trafico fluido.",
        "Traffic volume increased. The road is now becoming congested.": "El volumen de trafico aumento. La via comienza a congestionarse.",
        "Traffic reduction requested, but traffic is already fluent.": "Se solicito reducir trafico, pero ya se encuentra en estado fluido.",
        "An accident was reported. Traffic flow switches to accident mode.": "Se reporto un accidente. El flujo cambia al modo de accidente.",
        "No active accident exists to clear.": "No existe un accidente activo para despejar.",
        "Simulation advanced with high-speed traffic flow.": "La simulacion avanzo con flujo de trafico rapido.",
        "State changed to CongestedTrafficState.": "El estado cambio a CongestedTrafficState.",
        "Traffic volume increased further, but the system remains congested.": "El volumen de trafico aumento aun mas, pero el sistema sigue congestionado.",
        "Traffic volume reduced. The road enters a recovery phase.": "El volumen de trafico disminuyo. La via entra en fase de recuperacion.",
        "An accident occurred during congestion. Traffic switches to accident mode.": "Ocurrio un accidente durante la congestion. El trafico cambia al modo de accidente.",
        "There is no accident to clear in the congested state.": "No hay accidente que despejar en el estado congestionado.",
        "Simulation advanced with slow vehicle movement due to congestion.": "La simulacion avanzo con movimiento lento por congestion.",
        "State changed to AccidentTrafficState.": "El estado cambio a AccidentTrafficState.",
        "Traffic increased around the accident area, making congestion worse.": "El trafico aumento alrededor del accidente y empeoro la congestion.",
        "Reducing traffic volume does not solve the active accident.": "Reducir el volumen de trafico no resuelve el accidente activo.",
        "The accident state is already active.": "El estado de accidente ya esta activo.",
        "Emergency response cleared the accident. The road moves into recovery.": "La emergencia despejo el accidente. La via entra en recuperacion.",
        "Simulation advanced with blocked traffic near the accident area.": "La simulacion avanzo con trafico bloqueado cerca del accidente.",
        "State changed to ClearedTrafficState.": "El estado cambio a ClearedTrafficState.",
        "Traffic increased during recovery. The road becomes congested again.": "El trafico aumento durante la recuperacion. La via vuelve a congestionarse.",
        "Traffic reduction supports recovery and improves vehicle movement.": "La reduccion del trafico favorece la recuperacion y mejora el movimiento.",
        "A new accident was reported during recovery.": "Se reporto un nuevo accidente durante la recuperacion.",
        "The road is already being cleared and recovered.": "La via ya esta siendo despejada y recuperada.",
        "Simulation advanced in recovery mode. Traffic returns to fluent conditions.": "La simulacion avanzo en recuperacion. El trafico vuelve a condiciones fluidas.",
        "State changed to FluentTrafficState.": "El estado cambio a FluentTrafficState."
    };

    return translations[log] || log;
}
