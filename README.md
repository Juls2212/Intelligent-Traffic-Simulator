# Simulador de Tráfico Inteligente

## Nombre del proyecto
Simulador de Tráfico Inteligente con Patrón State.

## Objetivo
Demostrar de forma clara y académica cómo el patrón de diseño State permite que un mismo simulador cambie su comportamiento según el estado actual del tráfico, sin usar grandes bloques `if` o `switch` para decidir la lógica principal.

## Caso de estudio
El proyecto modela una vía urbana con vehículos que cambian su velocidad y comportamiento ante diferentes situaciones:

- Tráfico fluido
- Tráfico congestionado
- Accidente activo
- Vía despejada en recuperación

El usuario puede interactuar con el sistema desde una aplicación web y observar cómo el backend responde de manera distinta a las mismas acciones dependiendo del estado actual.

## Por qué aplica el Patrón State
Este caso encaja con el patrón State porque el simulador debe responder a las mismas operaciones, pero con efectos diferentes según la condición actual de la vía.

Por ejemplo:

- `increaseTraffic()` no se comporta igual si el tráfico está fluido que si ya existe congestión.
- `clearAccident()` no significa lo mismo si hay un accidente activo o si la vía ya está en recuperación.
- `advanceSimulation()` produce una evolución distinta en cada estado.

En lugar de centralizar todas esas decisiones en el contexto con condicionales extensos, cada estado concreto encapsula su propia lógica y decide cuándo cambiar al siguiente estado.

## Clases principales del patrón
### Contexto
`TrafficSimulator`

Es la clase que mantiene una referencia al estado actual (`TrafficState`), almacena los vehículos, el estado de la vía y los registros recientes. Además, delega las acciones al objeto `currentState`.

### Interfaz State
`TrafficState`

Define las operaciones comunes que todos los estados deben implementar:

- `increaseTraffic()`
- `reduceTraffic()`
- `reportAccident()`
- `clearAccident()`
- `advanceSimulation()`

### Estados concretos
- `FluentTrafficState`
- `CongestedTrafficState`
- `AccidentTrafficState`
- `ClearedTrafficState`

Cada una de estas clases implementa `TrafficState` y encapsula su comportamiento específico.

## Evidencia del Patrón State
La evidencia principal del patrón es que las mismas acciones producen respuestas distintas según el estado actual del contexto.

### `increaseTraffic()`
- En `FluentTrafficState`, el sistema cambia a `CongestedTrafficState`.
- En `CongestedTrafficState`, el sistema permanece congestionado.
- En `AccidentTrafficState`, la congestión empeora alrededor del accidente.
- En `ClearedTrafficState`, el sistema vuelve a congestionarse.

### `reduceTraffic()`
- En `FluentTrafficState`, no cambia el estado porque ya existe flujo normal.
- En `CongestedTrafficState`, el sistema pasa a `ClearedTrafficState`.
- En `AccidentTrafficState`, reducir tráfico no resuelve el accidente.
- En `ClearedTrafficState`, ayuda a la recuperación de la vía.

### `reportAccident()`
- En `FluentTrafficState`, cambia a `AccidentTrafficState`.
- En `CongestedTrafficState`, también cambia a `AccidentTrafficState`.
- En `AccidentTrafficState`, informa que el accidente ya está activo.
- En `ClearedTrafficState`, activa nuevamente un estado de accidente.

### `clearAccident()`
- En `FluentTrafficState`, indica que no existe accidente para despejar.
- En `CongestedTrafficState`, indica que no hay accidente que limpiar.
- En `AccidentTrafficState`, cambia a `ClearedTrafficState`.
- En `ClearedTrafficState`, informa que la vía ya está en proceso de recuperación.

### `advanceSimulation()`
- En `FluentTrafficState`, los vehículos avanzan rápidamente.
- En `CongestedTrafficState`, avanzan lentamente.
- En `AccidentTrafficState`, quedan bloqueados o casi detenidos.
- En `ClearedTrafficState`, la simulación lleva al sistema nuevamente a `FluentTrafficState`.

## Estructura general del proyecto
- `context`: contiene el contexto del patrón.
- `state`: contiene la interfaz State y los estados concretos.
- `model`: contiene las entidades del dominio.
- `server`: contiene el servidor HTTP y la vista de consola.
- `public`: contiene la interfaz web en HTML, CSS y JavaScript.

## Cómo ejecutar el proyecto en IntelliJ IDEA
1. Abrir IntelliJ IDEA.
2. Seleccionar `Open` y elegir la carpeta del proyecto.
3. Verificar que IntelliJ reconozca `src/main/java` como carpeta de código fuente.
4. Asegurarse de tener un JDK configurado.
5. Ejecutar la clase `Main`.

La clase `Main` inicia el servidor HTTP integrado en el puerto `8080`.

## Cómo abrir la aplicación web
1. Ejecutar `Main`.
2. Esperar el mensaje en consola:

```text
Server running at http://localhost:8080
```

3. Abrir un navegador web.
4. Ir a:

```text
http://localhost:8080
```

## Qué debe probar el profesor para evidenciar el patrón
Para verificar correctamente el uso del patrón State, se recomienda probar esta secuencia:

1. Iniciar la aplicación y observar el estado inicial `FluentTrafficState`.
2. Presionar `Aumentar tráfico` y verificar el cambio a `CongestedTrafficState`.
3. Presionar `Reportar accidente` y verificar el cambio a `AccidentTrafficState`.
4. Presionar `Reducir tráfico` y comprobar que el accidente no se resuelve.
5. Presionar `Despejar accidente` y verificar el cambio a `ClearedTrafficState`.
6. Presionar `Avanzar simulación` y comprobar el retorno a `FluentTrafficState`.

Durante esa prueba, el profesor debe observar:

- El cambio de clase de estado en pantalla.
- El cambio de velocidad promedio.
- El cambio del nivel de congestión.
- El cambio visual de los vehículos en la carretera.
- El registro de eventos.
- Que las transiciones ocurren dentro de las clases de estado y no en el contexto.

## Observación final
El proyecto mantiene una separación clara de responsabilidades:

- El backend en Java conserva la lógica del dominio y del patrón State.
- El servidor HTTP solo expone endpoints y archivos estáticos.
- El frontend representa visualmente la información enviada por el backend.
- La lógica de transición de estados permanece encapsulada en los estados concretos.
