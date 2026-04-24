const DEFAULT_POLL_INTERVAL_MS = 800;
const DEMO_POLL_INTERVAL_MS = 1000;
const POSITION_SCALE = 1.6;
const ROAD_LENGTH = 760;
const CAR_START_OFFSET = 42;

const roadSceneElement = document.getElementById("road-scene");
const carsLayerElement = document.getElementById("cars-layer");
const accidentMarkerElement = document.getElementById("accident-marker");
const trafficLightsLayerElement = document.getElementById("traffic-lights-layer");
const blockedLaneBannerElement = document.getElementById("blocked-lane-banner");
const laneOverlayElements = [
    document.getElementById("lane-overlay-1"),
    document.getElementById("lane-overlay-2"),
    document.getElementById("lane-overlay-3")
];
const recoveryBannerElement = document.getElementById("recovery-banner");
const delegationBannerElement = document.getElementById("delegation-banner");
const stateHeroCardElement = document.getElementById("state-hero-card");
const stateSummaryCardElement = document.getElementById("state-summary-card");
const evidencePanelElement = document.getElementById("evidence-panel");
const stateBadgeElement = document.getElementById("state-badge");
const stateBadgeDescriptionElement = document.getElementById("state-badge-description");
const stateNameElement = document.getElementById("state-name");
const stateClassElement = document.getElementById("state-class");
const averageSpeedElement = document.getElementById("average-speed");
const congestionLevelElement = document.getElementById("congestion-level");
const accidentActiveElement = document.getElementById("accident-active");
const carsCountElement = document.getElementById("cars-count");
const stateDescriptionElement = document.getElementById("state-description");
const evidenceStateElement = document.getElementById("evidence-state");
const evidenceClassElement = document.getElementById("evidence-class");
const evidenceActionElement = document.getElementById("evidence-action");
const evidenceResultElement = document.getElementById("evidence-result");
const transitionPanelElement = document.getElementById("transition-panel");
const transitionsListElement = document.getElementById("transitions-list");
const laneStatusesElement = document.getElementById("lane-statuses");
const decisionLogsElement = document.getElementById("decision-logs");
const logsListElement = document.getElementById("logs-list");
const demoMessageElement = document.getElementById("demo-message");
const actionButtons = Array.from(document.querySelectorAll("[data-action]"));

let pollTimerId = null;
let lastRenderedTrace = "";
let lastRenderedTransition = "";
let delegationTimerId = null;
let transitionHighlightTimerId = null;
let demoMessageTimerId = null;
let currentPollIntervalMs = DEFAULT_POLL_INTERVAL_MS;
const carElementsById = new Map();
const previousCarPositions = new Map();
let lastActionKey = "";
let collisionFlashTimerId = null;

document.addEventListener("DOMContentLoaded", () => {
    bindActions();
    refreshStatus();
    startPolling(DEFAULT_POLL_INTERVAL_MS);
});

function bindActions() {
    actionButtons.forEach((button) => {
        button.addEventListener("click", async () => {
            setButtonsDisabled(true);
            showDelegationBanner("La accion fue delegada al estado actual", roadSceneElement.className);

            try {
                const status = await postAction(button.dataset.action);

                if (button.dataset.action === "/api/demo") {
                    startPolling(DEMO_POLL_INTERVAL_MS);
                    showDemoMessage();
                }

                renderStatus(status);
            } catch (error) {
                showLoadError("No fue posible actualizar la simulacion.");
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
    const themeClass = mapThemeClass(status.currentState);
    const actionTraceParts = parseActionTrace(status.lastActionTrace);

    roadSceneElement.className = `road-scene ${visualState}`;
    accidentMarkerElement.hidden = status.accidentActive !== true;
    recoveryBannerElement.hidden = status.currentState !== "ClearedTrafficState";
    renderBlockedLane(status.blockedLane);

    applyStateTheme(stateHeroCardElement, `hero-side-card ${themeClass}`);
    applyStateTheme(stateSummaryCardElement, `status-card accent-card ${mapAccentClass(status.currentState)}`);
    applyStateTheme(evidencePanelElement, `panel evidence-panel ${mapAccentClass(status.currentState)}`);

    stateBadgeElement.textContent = status.spanishStateName;
    stateBadgeDescriptionElement.textContent = translateBadgeMessage(status.currentState);

    stateNameElement.textContent = status.spanishStateName;
    stateClassElement.textContent = status.activeStateClass || status.currentState;
    averageSpeedElement.textContent = `${status.averageSpeed} km/h`;
    congestionLevelElement.textContent = translateCongestionLevel(status.congestionLevel);
    accidentActiveElement.textContent = status.accidentActive ? "Activo" : "Sin accidente";
    carsCountElement.textContent = `${status.cars.length} vehiculos`;
    stateDescriptionElement.textContent = translateDescription(status.currentState, status.description);

    evidenceStateElement.textContent = status.spanishStateName;
    evidenceClassElement.textContent = actionTraceParts.handledBy || status.activeStateClass || status.currentState;
    evidenceActionElement.textContent = actionTraceParts.action || "Sin accion registrada";
    evidenceResultElement.textContent = actionTraceParts.result || "Sin resultado registrado";
    lastActionKey = actionTraceParts.actionKey || "";

    renderCars(status.cars, status.currentState);
    renderTrafficLights(status.trafficLights || []);
    renderLaneStatuses(status.laneStatuses || []);
    renderDecisionLogs(status.decisionLogs || []);
    renderTransitions(status.stateTransitions || []);
    renderLogs(status.logs || []);

    if (status.lastActionTrace && status.lastActionTrace !== lastRenderedTrace) {
        lastRenderedTrace = status.lastActionTrace;
        showDelegationBanner("La accion fue delegada al estado actual", visualState);
    }
}

function renderTrafficLights(trafficLights) {
    trafficLightsLayerElement.innerHTML = "";

    trafficLights.forEach((trafficLight) => {
        const trafficLightElement = document.createElement("div");
        const topPosition = resolveTrafficLightTop(trafficLight.lane);
        trafficLightElement.className = `traffic-light traffic-light-${trafficLight.color.toLowerCase()}`;
        trafficLightElement.style.top = `${topPosition}px`;
        trafficLightElement.innerHTML = `
            <div class="traffic-light-body">
                <span class="traffic-light-dot"></span>
            </div>
            <div class="traffic-light-info">
                <strong>C${trafficLight.lane}</strong>
                <span>${translateTrafficLightColor(trafficLight.color)}</span>
                <span>${trafficLight.remainingSeconds}s</span>
            </div>
        `;
        trafficLightsLayerElement.appendChild(trafficLightElement);
    });
}

function renderBlockedLane(blockedLane) {
    const hasBlockedLane = blockedLane > 0;
    blockedLaneBannerElement.hidden = !hasBlockedLane;

    if (hasBlockedLane) {
        blockedLaneBannerElement.textContent = `Carril bloqueado: ${blockedLane}`;
    }

    laneOverlayElements.forEach((laneOverlayElement, index) => {
        laneOverlayElement.classList.toggle("blocked", blockedLane === index + 1);
    });
}

function startPolling(intervalMs) {
    currentPollIntervalMs = intervalMs;

    if (pollTimerId) {
        window.clearInterval(pollTimerId);
    }

    pollTimerId = window.setInterval(refreshStatus, currentPollIntervalMs);
}

function renderCars(cars, currentState) {
    const sceneWidth = carsLayerElement.clientWidth || roadSceneElement.clientWidth || 900;
    const usableWidth = Math.max(sceneWidth - 120, 240);
    const visibleCars = cars.filter((car) => car.visible !== false);
    const currentCarIds = new Set(visibleCars.map((car) => car.id));

    visibleCars.forEach((car, index) => {
        const normalizedPosition = (car.xPosition % ROAD_LENGTH) / ROAD_LENGTH;
        const leftPosition = Math.min((normalizedPosition * usableWidth) + CAR_START_OFFSET, sceneWidth - 30);
        const laneTop = resolveLaneTop(car.lane);
        const previousPosition = previousCarPositions.get(car.id);
        let carElement = carElementsById.get(car.id);

        if (!carElement) {
            carElement = document.createElement("div");
            carElement.innerHTML = `
                <span class="car-label">${car.id}</span>
                <span class="car-top"></span>
                <span class="car-light"></span>
            `;
            carElementsById.set(car.id, carElement);
            carsLayerElement.appendChild(carElement);
        }

        carElement.className = buildCarClassName(car, currentState, index);
        carElement.querySelector(".car-label").textContent = car.id;
        carElement.style.top = `${laneTop}px`;

        if (previousPosition !== undefined && leftPosition < previousPosition - 120) {
            carElement.style.transition = "none";
            carElement.style.left = `${CAR_START_OFFSET - 46}px`;

            requestAnimationFrame(() => {
                carElement.style.transition = "";
                carElement.style.left = `${leftPosition}px`;
            });
        } else {
            carElement.style.left = `${leftPosition}px`;
        }

        previousCarPositions.set(car.id, leftPosition);
    });

    if (lastActionKey === "provokeAccident" && visibleCars.some((car) => car.crashed)) {
        roadSceneElement.classList.add("collision-impact");

        if (collisionFlashTimerId) {
            window.clearTimeout(collisionFlashTimerId);
        }

        collisionFlashTimerId = window.setTimeout(() => {
            roadSceneElement.classList.remove("collision-impact");
        }, 1100);
    }

    Array.from(carElementsById.keys()).forEach((carId) => {
        if (!currentCarIds.has(carId)) {
            const carElement = carElementsById.get(carId);

            if (carElement) {
                carElement.remove();
            }

            carElementsById.delete(carId);
            previousCarPositions.delete(carId);
        }
    });
}

function renderTransitions(transitions) {
    transitionsListElement.innerHTML = "";

    const visibleTransitions = transitions.length > 0 ? transitions.slice().reverse() : ["Sin transiciones registradas todavia."];

    visibleTransitions.forEach((transition) => {
        const item = document.createElement("li");
        item.textContent = transition.replace("->", " \u2192 ");
        transitionsListElement.appendChild(item);
    });

    const latestTransition = transitions.length > 0 ? transitions[transitions.length - 1] : "";

    if (latestTransition && latestTransition !== lastRenderedTransition) {
        lastRenderedTransition = latestTransition;
        highlightTransitions();
    }
}

function renderLaneStatuses(laneStatuses) {
    laneStatusesElement.innerHTML = "";

    laneStatuses.forEach((laneStatus) => {
        const laneCardElement = document.createElement("article");
        laneCardElement.className = buildLaneCardClassName(laneStatus);
        laneCardElement.innerHTML = `
            <h3>Carril ${laneStatus.laneNumber}</h3>
            <p>Vehiculos: ${laneStatus.vehicleCount}</p>
            <p>Velocidad promedio: ${laneStatus.averageSpeed} km/h</p>
            <p>Prioridad: ${laneStatus.priority ? "Alta" : "Normal"}</p>
            <p>Estado: ${laneStatus.blocked ? "Bloqueado" : "Libre"}</p>
        `;
        laneStatusesElement.appendChild(laneCardElement);
    });
}

function renderDecisionLogs(decisionLogs) {
    decisionLogsElement.innerHTML = "";

    const visibleDecisions = decisionLogs.length > 0
        ? decisionLogs.slice().reverse()
        : ["No hay decisiones registradas todavia."];

    visibleDecisions.forEach((decisionLog) => {
        const decisionItem = document.createElement("li");
        decisionItem.textContent = translateDecisionLog(decisionLog);
        decisionLogsElement.appendChild(decisionItem);
    });
}

function renderLogs(logs) {
    logsListElement.innerHTML = "";

    const visibleLogs = logs.length > 0 ? logs.slice().reverse() : ["No hay eventos registrados todavia."];

    visibleLogs.forEach((log) => {
        const item = document.createElement("li");
        item.textContent = translateLog(log);
        logsListElement.appendChild(item);
    });
}

function buildCarClassName(car, currentState, index) {
    const classNames = ["car-visual", `car-color-${(index % 4) + 1}`];

    if (car.crashed) {
        classNames.push("car-crashed");
    } else if (car.blocked) {
        classNames.push("car-blocked");
    } else if (currentState === "FluentTrafficState") {
        classNames.push("car-fast");
    } else if (currentState === "ClearedTrafficState") {
        classNames.push("car-recovery");
    } else {
        classNames.push("car-slow");
    }

    if (car.changingLane) {
        classNames.push("car-changing-lane");
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

function resolveTrafficLightTop(lane) {
    const laneMap = {
        1: 44,
        2: 156,
        3: 268
    };

    return laneMap[lane] || 44;
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

function mapThemeClass(currentState) {
    const themeClassMap = {
        FluentTrafficState: "state-theme-fluent",
        CongestedTrafficState: "state-theme-congested",
        AccidentTrafficState: "state-theme-accident",
        ClearedTrafficState: "state-theme-cleared"
    };

    return themeClassMap[currentState] || "state-theme-fluent";
}

function mapAccentClass(currentState) {
    const accentClassMap = {
        FluentTrafficState: "state-accent-fluent",
        CongestedTrafficState: "state-accent-congested",
        AccidentTrafficState: "state-accent-accident",
        ClearedTrafficState: "state-accent-cleared"
    };

    return accentClassMap[currentState] || "state-accent-fluent";
}

function parseActionTrace(trace) {
    if (!trace) {
        return {
            action: "",
            actionKey: "",
            handledBy: "",
            result: ""
        };
    }

    const normalizedTrace = trace.replace("Action: ", "");
    const traceParts = normalizedTrace.split(" handled by ");

    if (traceParts.length < 2) {
        return {
            action: trace,
            actionKey: "",
            handledBy: "",
            result: ""
        };
    }

    const action = traceParts[0];
    const stateAndResult = traceParts[1].split(" \u2192 ");

    return {
        action: translateActionName(action),
        actionKey: action,
        handledBy: stateAndResult[0] || "",
        result: translateResult(stateAndResult[1] || "")
    };
}

function buildLaneCardClassName(laneStatus) {
    const classNames = ["lane-card"];

    if (laneStatus.priority) {
        classNames.push("priority");
    }

    if (laneStatus.blocked) {
        classNames.push("blocked");
    }

    return classNames.join(" ");
}

function translateActionName(action) {
    const actions = {
        increaseTraffic: "Aumentar trafico",
        reduceTraffic: "Reducir trafico",
        reportAccident: "Reportar accidente",
        provokeAccident: "Provocar accidente",
        clearAccident: "Despejar accidente",
        advanceSimulation: "Avanzar simulacion",
        reset: "Reiniciar simulador"
    };

    return actions[action] || action;
}

function translateResult(result) {
    if (!result) {
        return "";
    }

    return result
        .replace("Transition to ", "Cambio a ")
        .replace("Remains in ", "Permanece en ");
}

function applyStateTheme(element, className) {
    element.className = className;
}

function showDelegationBanner(message, stateClassName) {
    delegationBannerElement.textContent = message;
    delegationBannerElement.hidden = false;
    delegationBannerElement.className = `delegation-banner ${extractStateClass(stateClassName)} active`;

    if (delegationTimerId) {
        window.clearTimeout(delegationTimerId);
    }

    delegationTimerId = window.setTimeout(() => {
        delegationBannerElement.classList.remove("active");
    }, 1700);
}

function showDemoMessage() {
    demoMessageElement.hidden = false;

    if (demoMessageTimerId) {
        window.clearTimeout(demoMessageTimerId);
    }

    demoMessageTimerId = window.setTimeout(() => {
        demoMessageElement.hidden = true;
    }, 8000);
}

function highlightTransitions() {
    transitionPanelElement.classList.add("highlight");

    if (transitionHighlightTimerId) {
        window.clearTimeout(transitionHighlightTimerId);
    }

    transitionHighlightTimerId = window.setTimeout(() => {
        transitionPanelElement.classList.remove("highlight");
    }, 1300);
}

function extractStateClass(stateClassName) {
    if (stateClassName.includes("state-congested")) {
        return "state-congested";
    }

    if (stateClassName.includes("state-accident")) {
        return "state-accident";
    }

    if (stateClassName.includes("state-cleared")) {
        return "state-cleared";
    }

    return "state-fluent";
}

function setButtonsDisabled(disabled) {
    actionButtons.forEach((button) => {
        button.disabled = disabled;
    });
}

function showLoadError(message) {
    stateBadgeElement.textContent = "Error";
    stateNameElement.textContent = "Error de conexion";
    stateDescriptionElement.textContent = message;
    evidenceActionElement.textContent = "Sin datos";
    evidenceResultElement.textContent = "No fue posible leer la respuesta del backend";
}

function translateBadgeMessage(currentState) {
    const messages = {
        FluentTrafficState: "El trafico circula rapido y con buena separacion entre vehiculos.",
        CongestedTrafficState: "La densidad vehicular aumento y el avance es lento y cercano.",
        AccidentTrafficState: "Existe un accidente activo y el flujo queda bloqueado o casi detenido.",
        ClearedTrafficState: "La via se recupera y la movilidad comienza a normalizarse."
    };

    return messages[currentState] || "Estado visual del simulador.";
}

function translateDescription(currentState, fallbackDescription) {
    const descriptions = {
        FluentTrafficState: "Los vehiculos circulan rapido y espaciados sobre la carretera.",
        CongestedTrafficState: "Los vehiculos avanzan mas juntos y con menor velocidad.",
        AccidentTrafficState: "Se muestra un punto de accidente y los vehiculos quedan bloqueados o avanzan casi detenidos.",
        ClearedTrafficState: "La via entra en recuperacion y el flujo comienza a normalizarse."
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

function translateTrafficLightColor(color) {
    const colorLabels = {
        GREEN: "Verde",
        YELLOW: "Amarillo",
        RED: "Rojo"
    };

    return colorLabels[color] || color;
}

function translateDecisionLog(decisionLog) {
    return decisionLog
        .replace("gave priority to lane ", "dio prioridad al carril ")
        .replace("blocked lane ", "bloqueo el carril ")
        .replace("and redirected vehicles.", "y redirigio vehiculos.")
        .replace("and reduced speed.", "y redujo la velocidad.")
        .replace("restored normal circulation.", "restauro la circulacion normal.")
        .replace("maintained normal circulation.", "mantuvo la circulacion normal.")
        .replace("confirmed that all lanes were already available.", "confirmo que todos los carriles ya estaban disponibles.")
        .replace("maintained adaptive congestion priority on lane ", "mantuvo prioridad adaptativa de congestion en el carril ")
        .replace("kept lane ", "mantuvo el carril ")
        .replace(" blocked while emergency flow control remained active.", " bloqueado mientras el control de emergencia seguia activo.")
        .replace("confirmed that lane ", "confirmo que el carril ")
        .replace(" remains blocked.", " sigue bloqueado.")
        .replace("kept recovery controls active.", "mantuvo activos los controles de recuperacion.")
        .replace("confirmed that an accident is already active.", "confirmo que ya existe un accidente activo.");
}

function translateLog(log) {
    if (log === "TrafficSimulator initialized the Context with FluentTrafficState as the starting State object.") {
        return "TrafficSimulator inicializo el Contexto con FluentTrafficState como objeto State inicial.";
    }

    if (log === "TrafficSimulator reset the simulation and restored FluentTrafficState as the initial academic baseline.") {
        return "TrafficSimulator reinicio la simulacion y restauro FluentTrafficState como linea base academica inicial.";
    }

    if (log === "TrafficSimulator started the automatic demonstration sequence to expose dynamic State-pattern behavior.") {
        return "TrafficSimulator inicio la secuencia automatica para exponer el comportamiento dinamico del patron State.";
    }

    if (log === "TrafficSimulator rejected a new demo request because the previous demonstration sequence is still running.") {
        return "TrafficSimulator rechazo una nueva solicitud de demostracion porque la secuencia anterior sigue en ejecucion.";
    }

    if (log === "TrafficSimulator finished the automatic demonstration sequence.") {
        return "TrafficSimulator finalizo la secuencia automatica de demostracion.";
    }

    if (log.startsWith("TrafficSimulator delegated ")) {
        return log
            .replace("TrafficSimulator delegated ", "TrafficSimulator delego ")
            .replace(", which transitioned to ", ", y este realizo una transicion a ")
            .replace(", which remained in ", ", y este permanecio en ");
    }

    if (log.startsWith("TrafficSimulator updated its active state reference to ")) {
        return log.replace(
            "TrafficSimulator updated its active state reference to ",
            "TrafficSimulator actualizo su referencia de estado activo a "
        );
    }

    return log;
}
