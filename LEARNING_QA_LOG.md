# Learning Q&A Log - KakeBot

Este documento registra dudas reales del aprendizaje y sus respuestas, para poder repasar sin perder contexto entre sesiones.

## Regla de uso

1. Cada nueva duda relevante se anota aqui.
2. Se escribe una respuesta corta y practica.
3. Se prioriza lenguaje simple y ejemplos concretos.

---

## Arquitectura y paquetes

### Pregunta
Que es `package-info.java` y por que aparece con icono Java?

### Respuesta
Es un archivo especial para documentar un paquete (Javadoc y anotaciones de paquete). Tiene icono Java porque es un `.java` normal para IntelliJ.

### Pregunta
Por que crear `common`?

### Respuesta
Para elementos transversales de toda la app (por ejemplo `GlobalExceptionHandler`, config comun, utilidades compartidas).

### Pregunta
Telegram necesita `repository`?

### Respuesta
No en general. Telegram suele ser canal de entrada/salida y orquestacion; la persistencia principal esta en modulos de dominio como `expense` o `user`.

---

## Modelo de datos y usuarios

### Pregunta
Por que `ownerId` y por que `String` al inicio?

### Respuesta
Era una estrategia flexible inicial para integrar ids externos (Telegram). Luego evolucionamos a modelo mas solido: `Expense` relacionado con `User` por `user_id` (FK).

### Pregunta
Para que sirve `externalId` en `User`?

### Respuesta
Guarda el identificador del proveedor externo (por ejemplo Telegram `from.id`). El `id` interno es propio de la BD y estable para relaciones internas.

### Pregunta
Por que `User` no tiene `List<Expense>`?

### Respuesta
No es obligatorio. Con `Expense.user_id` ya existe la relacion y se puede consultar por repositorio. Es una forma mas simple para V1.

### Pregunta
Los ids de gastos son globales o por usuario?

### Respuesta
Globales en la tabla `expenses` (autoincrement). La separacion por usuario la da `user_id`.

---

## DTO, mapper y validacion

### Pregunta
Por que DTO como `record`?

### Respuesta
Menos boilerplate, inmutabilidad por defecto y contrato claro para request/response.

### Pregunta
Por que mover mapeo al paquete `mapper`?

### Respuesta
Para mantener el service centrado en logica de negocio y dejar la transformacion DTO/entity en una capa dedicada.

### Pregunta
Es mejor validar en service o con anotaciones?

### Respuesta
Las validaciones de formato/campos obligatorios van en DTO (`@NotNull`, `@NotBlank`, `@Positive`) + `@Valid`. En service quedan reglas de negocio (ej. usuario inexistente).

---

## Endpoints, headers y autenticacion

### Pregunta
Por que usamos `X-User-Id` y no path variable de usuario?

### Respuesta
Ahora es temporal para probar en Swagger sin auth. El objetivo final es obtener el usuario del contexto de seguridad (token/sesion), no de URL ni header manual.

### Pregunta
Que diferencia hay entre `@RequestHeader` y `@PathVariable`?

### Respuesta
`@PathVariable` lee datos de la URL (`/expenses/{id}`), `@RequestHeader` lee cabeceras HTTP (`X-User-Id: 1`).

### Pregunta
Por que el `POST` antes devolvia `ResponseEntity<?>`?

### Respuesta
Por manejo temporal de distintos tipos de respuesta. Se dejo tipado como `ResponseEntity<ExpenseResponseDto>` y las excepciones se delegan al flujo global.

---

## Totales por categoria

### Pregunta
Por que inicializar categorias a 0?

### Respuesta
Para que siempre aparezcan todas las categorias en la respuesta, incluso sin gastos en ese mes.

### Pregunta
Que hace `new EnumMap<>(ExpenseCategory.class)`?

### Respuesta
Es un mapa optimizado para claves enum. Solo acepta claves de `ExpenseCategory` y mantiene el orden natural del enum.

### Pregunta
Que hace `merge` con 3 argumentos?

### Respuesta
`merge(clave, nuevoValor, funcion)`:
1. Si no existe clave, usa `nuevoValor`.
2. Si existe, combina `valorActual` y `nuevoValor` con la funcion (aqui `BigDecimal::add`).

### Pregunta
Por que `entrySet().stream()` y no `totalsByCategory.stream()`?

### Respuesta
Porque `Map` no tiene `stream()` directo. `entrySet()` permite recorrer clave+valor juntos para mapear a DTO.

---

## Entorno local y base de datos

### Pregunta
Por que fallaba MySQL con `using password: NO`?

### Respuesta
La app no estaba recibiendo `DB_PASSWORD` en runtime. Se resolvio configurando variables de entorno en la Run Configuration de IntelliJ.

### Pregunta
Workbench debe estar abierto para que funcione la app?

### Respuesta
No. Workbench es cliente visual. Solo necesita estar activo el servidor MySQL.

---

## Nota de progreso

- Primer endpoint funcional completo: `POST /api/expenses`.
- Endpoints funcionales: `GET /today`, `GET /month`, `GET /total/today`, `GET /total/month`, `GET /total/month/by-category`.
- Pendientes en `ExpenseController`: `GET /recent`, `DELETE /{id}`.
