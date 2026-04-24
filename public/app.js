const DEFAULT_POLL_INTERVAL_MS = 2500;
const DEMO_POLL_INTERVAL_MS = 1000;
const POSITION_SCALE = 1.6;

const roadSceneElement = document.getElementById("road-scene");
const carsLayerElement = document.getElementById("cars-layer");
const accidentMarkerElement = document.getElementById("accident-marker");
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
    accidentMarkerElement.hidden = !status.accidentActive;
    recoveryBannerElement.hidden = status.currentState !== "ClearedTrafficState";

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

    renderCars(status.cars, status.currentState);
    renderTransitions(status.stateTransitions || []);
    renderLogs(status.logs || []);

    if (status.lastActionTrace && status.lastActionTrace !== lastRenderedTrace) {
        lastRenderedTrace = status.lastActionTrace;
        showDelegationBanner("La accion fue delegada al estado actual", visualState);
    }
}

function startPolling(intervalMs) {
    currentPollIntervalMs = intervalMs;

    if (pollTimerId) {
        window.clearInterval(pollTimerId);
    }

    pollTimerId = window.setInterval(refreshStatus, currentPollIntervalMs);
}

function renderCars(cars, currentState) {
    carsLayerElement.innerHTML = "";
    const sceneWidth = carsLayerElement.clientWidth || roadSceneElement.clientWidth || 900;
    const maxPosition = Math.max(...cars.map((car) => car.xPosition), 1);
    const usableWidth = Math.max(sceneWidth - 120, 220);

    cars.forEach((car, index) => {
        const carElement = document.createElement("div");
        const relativePosition = maxPosition === 0 ? 0 : car.xPosition / maxPosition;
        const scaledPosition = Math.min(car.xPosition * POSITION_SCALE, usableWidth);
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
            handledBy: "",
            result: ""
        };
    }

    const normalizedTrace = trace.replace("Action: ", "");
    const traceParts = normalizedTrace.split(" handled by ");

    if (traceParts.length < 2) {
        return {
            action: trace,
            handledBy: "",
            result: ""
        };
    }

    const action = traceParts[0];
    const stateAndResult = traceParts[1].split(" \u2192 ");

    return {
        action: translateActionName(action),
        handledBy: stateAndResult[0] || "",
        result: translateResult(stateAndResult[1] || "")
    };
}

function translateActionName(action) {
    const actions = {
        increaseTraffic: "Aumentar trafico",
        reduceTraffic: "Reducir trafico",
        reportAccident: "Reportar accidente",
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
