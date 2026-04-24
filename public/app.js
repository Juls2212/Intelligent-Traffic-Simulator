const POLL_INTERVAL_MS = 2500;
const POSITION_SCALE = 1.6;

const roadSceneElement = document.getElementById("road-scene");
const carsLayerElement = document.getElementById("cars-layer");
const accidentMarkerElement = document.getElementById("accident-marker");
const recoveryBannerElement = document.getElementById("recovery-banner");
const stateBadgeElement = document.getElementById("state-badge");
const stateBadgeDescriptionElement = document.getElementById("state-badge-description");
const stateNameElement = document.getElementById("state-name");
const stateClassElement = document.getElementById("state-class");
const averageSpeedElement = document.getElementById("average-speed");
const congestionLevelElement = document.getElementById("congestion-level");
const accidentActiveElement = document.getElementById("accident-active");
const carsCountElement = document.getElementById("cars-count");
const stateDescriptionElement = document.getElementById("state-description");
const logsListElement = document.getElementById("logs-list");
const actionButtons = Array.from(document.querySelectorAll("[data-action]"));

let pollTimerId = null;

document.addEventListener("DOMContentLoaded", () => {
    bindActions();
    refreshStatus();
    pollTimerId = window.setInterval(refreshStatus, POLL_INTERVAL_MS);
});

function bindActions() {
    actionButtons.forEach((button) => {
        button.addEventListener("click", async () => {
            setButtonsDisabled(true);

            try {
                const status = await postAction(button.dataset.action);
                renderStatus(status);
            } catch (error) {
                showLoadError("No fue posible actualizar la simulación.");
            } finally {
                setButtonsDisabled(false);
            }
        });
    });
}

async function refreshStatus() {
    try {
        const response = await fetch("/api/status");

        if (!response.ok) {
            throw new Error("Unable to fetch simulator status");
        }

        const status = await response.json();
        renderStatus(status);
    } catch (error) {
        showLoadError("No fue posible obtener el estado del simulador.");
    }
}

async function postAction(endpoint) {
    const response = await fetch(endpoint, { method: "POST" });

    if (!response.ok) {
        throw new Error(`Action request failed for ${endpoint}`);
    }

    return response.json();
}

function renderStatus(status) {
    const visualState = mapRoadStateClass(status.currentState);

    roadSceneElement.className = `road-scene ${visualState}`;
    accidentMarkerElement.hidden = !status.accidentActive;
    recoveryBannerElement.hidden = status.currentState !== "ClearedTrafficState";

    stateBadgeElement.textContent = status.spanishStateName;
    stateBadgeDescriptionElement.textContent = translateBadgeMessage(status.currentState);

    stateNameElement.textContent = status.spanishStateName;
    stateClassElement.textContent = status.currentState;
    averageSpeedElement.textContent = `${status.averageSpeed} km/h`;
    congestionLevelElement.textContent = translateCongestionLevel(status.congestionLevel);
    accidentActiveElement.textContent = status.accidentActive ? "Activo" : "Sin accidente";
    carsCountElement.textContent = `${status.cars.length} vehículos`;
    stateDescriptionElement.textContent = translateDescription(status.currentState, status.description);

    renderCars(status.cars, status.currentState);
    renderLogs(status.logs);
}

function renderCars(cars, currentState) {
    carsLayerElement.innerHTML = "";
    const sceneWidth = carsLayerElement.clientWidth || roadSceneElement.clientWidth || 900;
    const maxPosition = Math.max(...cars.map((car) => car.xPosition), 1);
    const usableWidth = Math.max(sceneWidth - 120, 220);

    cars.forEach((car, index) => {
        const carElement = document.createElement("div");
        const relativePosition = maxPosition === 0 ? 0 : car.xPosition / maxPosition;
        const scaledPosition = Math.min((car.xPosition * POSITION_SCALE), usableWidth);
        const normalizedPosition = Math.max(scaledPosition, relativePosition * usableWidth);
        const laneTop = resolveLaneTop(car.lane);

        carElement.className = buildCarClassName(car, currentState, index);
        carElement.style.left = `${Math.min(normalizedPosition + 60, sceneWidth - 30)}px`;
        carElement.style.top = `${laneTop}px`;
        carElement.innerHTML = `
            <span class="car-label">${car.id}</span>
            <span class="car-top"></span>
            <span class="car-light"></span>
        `;

        carsLayerElement.appendChild(carElement);
    });
}

function renderLogs(logs) {
    logsListElement.innerHTML = "";

    const visibleLogs = logs.slice().reverse();

    visibleLogs.forEach((log) => {
        const item = document.createElement("li");
        item.textContent = translateLog(log);
        logsListElement.appendChild(item);
    });
}

function buildCarClassName(car, currentState, index) {
    const classNames = ["car-visual", `car-color-${(index % 4) + 1}`];

    if (car.blocked) {
        classNames.push("car-blocked");
    } else if (currentState === "FluentTrafficState") {
        classNames.push("car-fast");
    } else if (currentState === "ClearedTrafficState") {
        classNames.push("car-recovery");
    } else {
        classNames.push("car-slow");
    }

    return classNames.join(" ");
}

function resolveLaneTop(lane) {
    const laneMap = {
        1: 78,
        2: 190,
        3: 302
    };

    return laneMap[lane] || 78;
}

function mapRoadStateClass(currentState) {
    const stateClassMap = {
        FluentTrafficState: "state-fluent",
        CongestedTrafficState: "state-congested",
        AccidentTrafficState: "state-accident",
        ClearedTrafficState: "state-cleared"
    };

    return stateClassMap[currentState] || "state-fluent";
}

function setButtonsDisabled(disabled) {
    actionButtons.forEach((button) => {
        button.disabled = disabled;
    });
}

function showLoadError(message) {
    stateBadgeElement.textContent = "Error";
    stateNameElement.textContent = "Error de conexión";
    stateDescriptionElement.textContent = message;
}

function translateBadgeMessage(currentState) {
    const messages = {
        FluentTrafficState: "El tráfico circula con alta velocidad y buena separación entre vehículos.",
        CongestedTrafficState: "La densidad vehicular aumentó y la circulación se hace más lenta.",
        AccidentTrafficState: "Existe un incidente activo que bloquea el flujo en la vía.",
        ClearedTrafficState: "La vía se recupera gradualmente después de la congestión o del accidente."
    };

    return messages[currentState] || "Estado visual del simulador.";
}

function translateDescription(currentState, fallbackDescription) {
    const descriptions = {
        FluentTrafficState: "Los vehículos circulan rápido y espaciados sobre la carretera.",
        CongestedTrafficState: "Los vehículos avanzan más juntos y con menor velocidad.",
        AccidentTrafficState: "Se muestra un punto de accidente y los vehículos quedan bloqueados o avanzan casi detenidos.",
        ClearedTrafficState: "La vía entra en recuperación y el flujo comienza a normalizarse."
    };

    return descriptions[currentState] || fallbackDescription;
}

function translateCongestionLevel(congestionLevel) {
    const levels = {
        Low: "Bajo",
        Medium: "Medio",
        High: "Alto",
        Critical: "Crítico"
    };

    return levels[congestionLevel] || congestionLevel;
}

function translateLog(log) {
    const translations = {
        "Simulator initialized in fluent traffic state.": "El simulador se inicializó en estado de tráfico fluido.",
        "Simulator reset to the initial fluent state.": "El simulador se reinició al estado inicial de tráfico fluido.",
        "Traffic volume increased. The road is now becoming congested.": "El volumen de tráfico aumentó. La vía comienza a congestionarse.",
        "Traffic reduction requested, but traffic is already fluent.": "Se solicitó reducir tráfico, pero ya se encuentra en estado fluido.",
        "An accident was reported. Traffic flow switches to accident mode.": "Se reportó un accidente. El flujo cambia al modo de accidente.",
        "No active accident exists to clear.": "No existe un accidente activo para despejar.",
        "Simulation advanced with high-speed traffic flow.": "La simulación avanzó con flujo de tráfico rápido.",
        "State changed to CongestedTrafficState.": "El estado cambió a CongestedTrafficState.",
        "Traffic volume increased further, but the system remains congested.": "El volumen de tráfico aumentó aún más, pero el sistema sigue congestionado.",
        "Traffic volume reduced. The road enters a recovery phase.": "El volumen de tráfico disminuyó. La vía entra en fase de recuperación.",
        "An accident occurred during congestion. Traffic switches to accident mode.": "Ocurrió un accidente durante la congestión. El tráfico cambia al modo de accidente.",
        "There is no accident to clear in the congested state.": "No hay accidente que despejar en el estado congestionado.",
        "Simulation advanced with slow vehicle movement due to congestion.": "La simulación avanzó con movimiento lento por congestión.",
        "State changed to AccidentTrafficState.": "El estado cambió a AccidentTrafficState.",
        "Traffic increased around the accident area, making congestion worse.": "El tráfico aumentó alrededor del accidente y empeoró la congestión.",
        "Reducing traffic volume does not solve the active accident.": "Reducir el volumen de tráfico no resuelve el accidente activo.",
        "The accident state is already active.": "El estado de accidente ya está activo.",
        "Emergency response cleared the accident. The road moves into recovery.": "La emergencia despejó el accidente. La vía entra en recuperación.",
        "Simulation advanced with blocked traffic near the accident area.": "La simulación avanzó con tráfico bloqueado cerca del accidente.",
        "State changed to ClearedTrafficState.": "El estado cambió a ClearedTrafficState.",
        "Traffic increased during recovery. The road becomes congested again.": "El tráfico aumentó durante la recuperación. La vía vuelve a congestionarse.",
        "Traffic reduction supports recovery and improves vehicle movement.": "La reducción del tráfico favorece la recuperación y mejora el movimiento.",
        "A new accident was reported during recovery.": "Se reportó un nuevo accidente durante la recuperación.",
        "The road is already being cleared and recovered.": "La vía ya está siendo despejada y recuperada.",
        "Simulation advanced in recovery mode. Traffic returns to fluent conditions.": "La simulación avanzó en recuperación. El tráfico vuelve a condiciones fluidas.",
        "State changed to FluentTrafficState.": "El estado cambió a FluentTrafficState."
    };

    return translations[log] || log;
}
