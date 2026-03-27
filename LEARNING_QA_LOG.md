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

### Pregunta
Por que usar `passwordHash` en `User` en vez de guardar una contrasena simple de 6 caracteres o numeros? Y que significa `GenerationType.IDENTITY`?

### Respuesta
`passwordHash` significa que no guardamos la contrasena real en la base de datos, sino una version transformada y segura de esa contrasena.

Por que:

1. Si alguien accede a la BD, no vera la contrasena real del usuario.
2. Es la forma profesional y segura de almacenar credenciales.
3. En login se compara el hash calculado con el hash guardado, no texto plano con texto plano.

Importante:
La longitud de la contrasena (por ejemplo 6 caracteres) es una regla de validacion; el hash es la forma segura de guardarla. Son cosas distintas.

`GenerationType.IDENTITY` viene de JPA/Hibernate y significa que la base de datos genera automaticamente el `id` del registro al insertarlo, normalmente con autoincrement.

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
Para que sirven los endpoints `GET /me` y `PUT /me` en `user`?

### Respuesta
`GET /me` sirve para recuperar el perfil del usuario autenticado o actual sin tener que pedir explicitamente su id en la URL. Es el endpoint tipico para que el front sepa "quien soy" y cargue los datos basicos del perfil.

En nuestro caso puede usarse para:

1. Mostrar nombre de usuario en la app.
2. Saber si la cuenta tiene Telegram vinculado o no.
3. Recuperar datos del usuario actual tras login o al entrar en la aplicacion.

`PUT /me` sirve para actualizar datos del propio usuario actual sin exponer una ruta tipo `/users/{id}`.

En nuestro caso podria usarse mas adelante para:

1. Cambiar nombre de usuario.
2. Cambiar contrasena.
3. Actualizar preferencias de perfil.
4. Gestionar datos de cuenta relacionados con la vinculacion.

Importancia:

1. `GET /me` tiene mucho valor practico desde el inicio.
2. `PUT /me` es util, pero no es bloqueante para el flujo principal del proyecto en esta fase.

### Pregunta
Por que el `POST` antes devolvia `ResponseEntity<?>`?

### Respuesta
Por manejo temporal de distintos tipos de respuesta. Se dejo tipado como `ResponseEntity<ExpenseResponseDto>` y las excepciones se delegan al flujo global.

### Pregunta
Que hacen anotaciones Swagger/OpenAPI como `@Operation`, `@ApiResponse`, `@Content` y `@Schema` en un controller?

### Respuesta
Sirven para documentar mejor cada endpoint en Swagger.

Ejemplo visto:

```java
@Operation(
        operationId = "Create a new resource",
        summary = "Creates a new resource and associates it with a challenge based on its topic.",
        description = "If a challenge with the same topic exists, it is automatically assigned. If multiple challenges exist, it can be linked to all or remain unassigned.",
        responses = {
                @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(implementation = ResourceDto.class), mediaType = "application/json")}),
                @ApiResponse(responseCode = "400", description = "Invalid parameters"),
                @ApiResponse(responseCode = "500", description = "Server error")
        }
)
```

Significado practico:

1. `@Operation` define nombre, resumen y descripcion del endpoint.
2. `operationId` identifica la operacion de forma unica.
3. `summary` muestra una descripcion corta en Swagger.
4. `description` amplia el comportamiento del endpoint.
5. `@ApiResponse` documenta codigos de respuesta esperados.
6. `@Content` y `@Schema` indican el tipo de body que devuelve.

Conclusión:
No es necesario para que funcione la API, pero da una documentacion mas profesional y clara.

Pendiente acordado:
Anadir esta documentacion Swagger/OpenAPI detallada cuando los endpoints esten mas estables y no estemos cambiando tanto rutas, DTOs o respuestas.

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

---

## Validacion en DTOs

### Pregunta
Como funciona un DTO que tiene metodos dentro con `@AssertTrue`? Desde donde se llaman?

### Respuesta
Cuando en el controller usamos `@Valid @RequestBody`, Spring valida automaticamente el DTO antes de entrar al metodo del controller.

Esa validacion revisa:
1. anotaciones sobre campos, como `@Size`, `@NotBlank`, etc.
2. metodos anotados con `@AssertTrue`

Eso significa que los metodos del DTO no los llamamos nosotros a mano. Spring los ejecuta automaticamente durante la validacion.

Ejemplo practico en `UserUpdateRequestDto`:
1. `hasAnyFieldToUpdate()` comprueba que venga al menos un dato para actualizar.
2. `hasCurrentPasswordWhenNewPasswordIsPresent()` comprueba que si llega `newPassword`, tambien llegue `currentPassword`.

Si alguno de esos metodos devuelve `false`, Spring considera que el request es invalido y responde con `400 Bad Request` antes de llegar al service.

Resumen corto:
- `@Valid` en controller dispara la validacion.
- Spring valida campos y tambien metodos con `@AssertTrue`.
- Si falla, el controller no entra al service.
- El error lo recoge nuestro `GlobalExceptionHandler`.

---

## Pendiente de estudio: JPA y Hibernate

### Tema pendiente
Conceptos a estudiar con calma: `LAZY`, `EAGER` y `@Transactional`.

### Significado basico
1. `LAZY`
- Una relacion no se carga completa al principio.
- Hibernate deja la carga para mas tarde, solo si realmente se accede a ella.
- Ventaja: evita traer datos innecesarios.
- Riesgo: si luego intentas acceder fuera de una sesion activa, puede aparecer `LazyInitializationException`.

2. `EAGER`
- La relacion se carga inmediatamente junto con la entidad principal.
- Ventaja: evita problemas de carga diferida en casos simples.
- Riesgo: puede traer mas datos de los necesarios y hacer consultas mas pesadas.

3. `@Transactional`
- Marca un metodo como unidad de trabajo con base de datos.
- Mantiene activa la sesion/transaccion durante la ejecucion del metodo.
- En muchos casos permite que Hibernate resuelva relaciones `LAZY` dentro de ese metodo sin error.

### Nuestro caso real
En `UserTelegramLinkCode`, la relacion con `User` esta marcada como `LAZY`.
Cuando en `linkTelegramUser(...)` se accedio a `linkCode.getUser()`, Hibernate intento cargar el usuario en ese momento.
El error aparecio porque ya no habia sesion activa para hacerlo.

Por eso, para este caso, la solucion recomendada es estudiar el uso de `@Transactional` antes de cambiar todo a `EAGER`.

### Idea clave para recordar
- `LAZY` y `EAGER` deciden cuando se cargan las relaciones.
- `@Transactional` decide si la operacion mantiene un contexto activo de base de datos durante el metodo.
- No conviene usar `EAGER` solo para apagar errores sin entender el coste.
