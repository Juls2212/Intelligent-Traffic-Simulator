# Simulador de Trafico Inteligente

## Nombre del proyecto
Simulador de Trafico Inteligente con Patron State.

## Objetivo
Demostrar de forma clara, defendible y academica como el Patron State permite que un mismo simulador responda de manera distinta a las mismas acciones segun el estado actual del trafico.

## Caso de estudio
El proyecto modela una via urbana con vehiculos que atraviesan cuatro situaciones operativas:

- Trafico fluido
- Trafico congestionado
- Accidente activo
- Via despejada en recuperacion

La aplicacion web permite ejecutar acciones sobre el simulador y observar, en tiempo real, como cambia el comportamiento del sistema cuando cambia el objeto State activo.

## Por que aplica el Patron State
Este problema es apropiado para el Patron State porque el sistema expone siempre las mismas operaciones:

- `increaseTraffic()`
- `reduceTraffic()`
- `reportAccident()`
- `clearAccident()`
- `advanceSimulation()`

Sin embargo, esas operaciones no significan lo mismo en todos los momentos. El resultado depende del estado actual del objeto `TrafficSimulator`. Por esa razon, la logica no se concentra en grandes bloques `if` o `switch` dentro del contexto, sino que se distribuye en objetos State concretos.

## Evidencia del Patron State
### Contexto
`TrafficSimulator`

Es el Contexto del patron. Mantiene una referencia al estado actual, conserva los vehiculos, el estado de la via, los registros de eventos, el historial de transiciones y la traza de la ultima accion. Su responsabilidad principal es delegar las operaciones al objeto `currentState`.

### Interfaz State
`TrafficState`

Es la interfaz comun del patron. Define las acciones que todos los estados concretos deben implementar:

- `increaseTraffic(TrafficSimulator simulator)`
- `reduceTraffic(TrafficSimulator simulator)`
- `reportAccident(TrafficSimulator simulator)`
- `clearAccident(TrafficSimulator simulator)`
- `advanceSimulation(TrafficSimulator simulator)`

### Estados concretos
- `FluentTrafficState`
- `CongestedTrafficState`
- `AccidentTrafficState`
- `ClearedTrafficState`

Cada una de estas clases encapsula el comportamiento especifico de la misma operacion segun la condicion actual del trafico.

### Como cambia dinamicamente el comportamiento
La evidencia central del patron es que el mismo metodo produce resultados distintos segun el objeto State activo.

#### `increaseTraffic()`
- En `FluentTrafficState`, el contexto delega la accion y el estado concreto cambia a `CongestedTrafficState`.
- En `CongestedTrafficState`, la accion se resuelve sin cambio de estado.
- En `AccidentTrafficState`, la accion no elimina el accidente y el sistema permanece en ese estado.
- En `ClearedTrafficState`, la recuperacion se interrumpe y el sistema vuelve a congestion.

#### `reduceTraffic()`
- En `FluentTrafficState`, el sistema permanece estable.
- En `CongestedTrafficState`, la reduccion de trafico provoca una transicion a `ClearedTrafficState`.
- En `AccidentTrafficState`, la reduccion no resuelve el accidente.
- En `ClearedTrafficState`, la accion apoya la recuperacion sin cambiar el estado.

#### `reportAccident()`
- En `FluentTrafficState`, aparece un accidente y el sistema cambia a `AccidentTrafficState`.
- En `CongestedTrafficState`, la congestion evoluciona a accidente.
- En `AccidentTrafficState`, la accion se atiende, pero el estado ya estaba activo.
- En `ClearedTrafficState`, la recuperacion se interrumpe y reaparece el estado de accidente.

#### `clearAccident()`
- En `FluentTrafficState`, no existe accidente por despejar.
- En `CongestedTrafficState`, tampoco hay accidente activo.
- En `AccidentTrafficState`, la limpieza provoca una transicion a `ClearedTrafficState`.
- En `ClearedTrafficState`, la accion no cambia el estado porque la via ya se encuentra en recuperacion.

#### `advanceSimulation()`
- En `FluentTrafficState`, los vehiculos avanzan rapidamente.
- En `CongestedTrafficState`, avanzan lentamente.
- En `AccidentTrafficState`, quedan bloqueados o casi detenidos.
- En `ClearedTrafficState`, la simulacion concluye la recuperacion y el sistema retorna a `FluentTrafficState`.

## Separacion de responsabilidades
- `context`: contiene el Contexto del patron State.
- `state`: contiene la interfaz State y los estados concretos.
- `model`: contiene las entidades del dominio.
- `server`: contiene el adaptador HTTP y la vista de consola.
- `public`: contiene la interfaz web.

La arquitectura mantiene estas reglas:

- No hay logica de estado en la interfaz web.
- No hay logica de estado en el servidor HTTP.
- Solo los estados concretos deciden como responder a cada accion.
- `TrafficSimulator` delega, pero no reemplaza el comportamiento de los estados.

## Como ejecutar el proyecto en IntelliJ IDEA
1. Abrir IntelliJ IDEA.
2. Seleccionar `Open` y elegir la carpeta del proyecto.
3. Confirmar que `src/main/java` este marcado como carpeta de codigo fuente.
4. Configurar un JDK compatible.
5. Ejecutar la clase `Main`.

La clase `Main` solo conecta el Contexto `TrafficSimulator` con el servidor HTTP `TrafficHttpServer`.

## Como abrir la aplicacion web
1. Ejecutar `Main`.
2. Esperar el mensaje:

```text
Server running at http://localhost:8080
```

3. Abrir el navegador en:

```text
http://localhost:8080
```

## Que debe probar el profesor
Para evidenciar el Patron State de manera directa, el profesor puede seguir esta secuencia:

1. Verificar que el estado inicial es `FluentTrafficState`.
2. Presionar `Aumentar trafico` y observar la transicion a `CongestedTrafficState`.
3. Presionar `Reportar accidente` y observar la transicion a `AccidentTrafficState`.
4. Presionar `Despejar accidente` y verificar la transicion a `ClearedTrafficState`.
5. Presionar `Reducir trafico` y comprobar que el sistema permanece en recuperacion.
6. Presionar `Avanzar simulacion` y observar el retorno a `FluentTrafficState`.
7. Presionar `Modo demostracion` para ver una secuencia automatica de delegacion, trazas y transiciones.

Durante la evaluacion debe observarse:

- La clase activa del estado.
- La ultima accion ejecutada.
- El resultado de la accion.
- El historial de transiciones.
- El registro de eventos academico.
- La diferencia visual entre trafico fluido, congestionado, accidente y via despejada.

## Conclusion academica
El proyecto es defendible porque la evidencia del patron es visible tanto en el codigo como en la interfaz:

- El Contexto es explicito.
- La interfaz State es explicita.
- Los estados concretos son explicitos.
- Las transiciones ocurren dentro de los estados concretos.
- El servidor solo expone endpoints.
- La interfaz solo representa lo que el backend envia.
