# KakeBot

<img src="docs/images/download.jpg" alt="Mascota de KakeBot" width="420" />

KakeBot es una aplicación de finanzas personales inspirada en el método japonés *kakebo*. Permite registrar gastos, consultar resúmenes mensuales, establecer límites por categoría y vincular una cuenta de Telegram para interactuar con la aplicación desde el bot.

> [!NOTE]
> KakeBot es un proyecto de estudio. Su objetivo principal es aprender a diseñar un backend propio y conectarlo con una API externa, en este caso la API de bots de Telegram. No debe considerarse todavía un producto preparado para producción.

## Objetivos de aprendizaje

- Diseñar una API REST con Spring Boot.
- Organizar un monolito modular mediante *package by feature*.
- Persistir datos con Spring Data JPA y MySQL.
- Integrar un backend propio con la API externa de Telegram.
- Validar datos y tratar errores de forma consistente.
- Construir un frontend React que consuma la API REST.
- Generar informes mensuales en PDF.
- Diseñar y automatizar pruebas aplicando la pirámide de testing.
- Contenerizar y desplegar una aplicación full stack.

## Estado actual

KakeBot cuenta actualmente con un backend Spring Boot, un frontend React y una integración funcional con Telegram.

### Capturas de la aplicación

<p align="center">
  <img src="https://jordicasasdev.onrender.com/images/optimized/kakebot/presentation.jpg" alt="Presentación de KakeBot" width="30%" />
  <img src="https://jordicasasdev.onrender.com/images/optimized/kakebot/dashboard.jpg" alt="Dashboard de KakeBot" width="30%" />
  <img src="https://jordicasasdev.onrender.com/images/optimized/kakebot/expense-entry.jpg" alt="Registro de un gasto en KakeBot" width="30%" />
</p>

<p align="center">
  <img src="https://jordicasasdev.onrender.com/images/optimized/kakebot/login.jpg" alt="Inicio de sesión de KakeBot" width="30%" />
  <img src="https://jordicasasdev.onrender.com/images/optimized/kakebot/telegram.jpg" alt="Integración de KakeBot con Telegram" width="30%" />
</p>

Puedes consultar estas capturas y el resto de proyectos en el [portfolio personal de Jordi Casas](https://jordicasasdev.onrender.com/#proyectos).

### Usuarios

- Registro e inicio de sesión con contraseñas cifradas mediante BCrypt.
- Consulta y actualización del perfil.
- Cambio de nombre de usuario y contraseña.
- Generación de códigos temporales para vincular Telegram.
- Vinculación y desvinculación de una cuenta de Telegram.

### Gastos

- Creación y eliminación de gastos.
- Importes con separador decimal mediante punto o coma.
- Consulta de gastos diarios, mensuales, históricos y recientes.
- Cálculo de totales diarios y mensuales.
- Totales agrupados por categoría.
- Límites mensuales de gasto por categoría.
- Avisos mediante Telegram cuando se supera un límite configurado.
- Generación de informes mensuales en PDF.

### Telegram

- Webhook para recibir actualizaciones del bot.
- Comandos `/start`, `/help` y `/link <code>`.
- Vinculación de un usuario de Telegram con una cuenta de KakeBot.
- Envío de alertas relacionadas con límites de gasto.

### Frontend

- Registro e inicio de sesión.
- Dashboard de gastos y resúmenes.
- Gestión del perfil y de la vinculación con Telegram.
- Configuración de límites por categoría.
- Interfaz adaptable con temática visual propia de KakeBot.

## Arquitectura

El backend sigue un enfoque de monolito modular organizado por funcionalidad. Los dominios principales son:

- `user`: usuarios, vinculación con Telegram y límites por categoría.
- `expense`: registro, consulta y agregación de gastos.
- `telegram`: recepción de webhooks y comunicación con la API de Telegram.
- `report`: generación de informes de gastos.
- `common`: configuración, salud de la aplicación y tratamiento de errores.

Dentro de cada dominio se separan controladores, servicios, repositorios, entidades, DTOs, mappers y excepciones cuando corresponde.

## Tecnologías

### Backend

- Java 21
- Spring Boot 3
- Spring Web y Spring Data JPA
- MySQL
- Bean Validation y BCrypt
- Swagger / OpenAPI
- OpenPDF
- JUnit 5, Mockito, MockMvc, Testcontainers y WireMock

### Frontend

- React 19
- Vite
- JavaScript y CSS
- ESLint

### Infraestructura e integraciones

- Docker
- Render y Railway
- Telegram Bot API

## Flujo de integración con Telegram

1. El usuario se registra en la aplicación web.
2. Desde KakeBot solicita un código temporal de vinculación.
3. Envía `/link <code>` al bot de Telegram.
4. El backend recibe el mensaje mediante el webhook.
5. El backend valida y consume el código temporal.
6. La identidad de Telegram queda asociada al usuario de KakeBot.
7. El backend puede enviar al usuario alertas a través de Telegram.

Este flujo es la parte central del objetivo educativo: conectar una aplicación propia con una API externa y gestionar el intercambio de información entre ambos sistemas.

## Estrategia de testing

KakeBot también funciona como ejercicio práctico de testing. La suite aplica la pirámide de pruebas: una base amplia de tests unitarios, un grupo menor de tests de integración y pocos recorridos end-to-end representativos.

### Aprendizaje asistido con IA

Para desarrollar y revisar esta estrategia se ha utilizado **Codex como herramienta de apoyo**. Su uso se ha planteado como parte de un ejercicio controlado de aprendizaje: identificar casos relevantes, contrastar el alcance de cada nivel de prueba y construir de forma progresiva una pirámide de testing coherente. También se ha empleado para ayudar a estructurar una documentación más clara y redactar descripciones de *pull requests* que reflejen sus objetivos, cambios, decisiones técnicas y forma de verificación. Todo este contenido se ha revisado y adaptado al estado real del proyecto; las decisiones sobre qué comprobar, la adaptación de los casos al dominio de KakeBot y la ejecución y validación de la suite se han realizado dentro del propio proyecto.

En sus primeras etapas, KakeBot se centró principalmente en estudiar la comunicación entre un backend propio y dos interfaces externas: la API de Telegram y la API REST consumida por el frontend. Por ese motivo, la automatización de pruebas no recibió inicialmente la profundidad necesaria. La incorporación posterior de la suite permite corregir esa carencia y convertir el proyecto también en un caso de estudio sobre:

- Las diferencias entre tests unitarios, de integración y end-to-end.
- La responsabilidad y los límites de cada nivel de la pirámide.
- El uso de mocks, MockMvc, Testcontainers y WireMock según el tipo de colaboración que se desea comprobar.
- La selección de casos representativos sin repetir las mismas verificaciones en todas las capas.
- La obtención de una cobertura funcional amplia basada en comportamientos y recorridos relevantes, no únicamente en una cifra de líneas ejecutadas.

Este proceso sirve tanto para repasar conceptos de testing como para documentar una evolución real del proyecto: detectar una debilidad inicial, diseñar una estrategia proporcionada y verificarla mediante una suite automatizada reproducible.

Actualmente, el backend cuenta con **176 ejecuciones automatizadas**:

- **135 tests unitarios**.
- **34 tests de integración**.
- **6 tests end-to-end**.
- **1 prueba de humo** que comprueba el arranque del contexto completo de Spring.

La intención no es repetir los mismos casos en todos los niveles, sino comprobar una responsabilidad distinta en cada uno.

### 1. Tests unitarios

Constituyen la base más amplia de la pirámide. Verifican de forma aislada:

- Reglas de negocio de usuarios y gastos.
- Cálculo de totales y periodos.
- Límites mensuales y condiciones de envío de alertas.
- Generación y caducidad de códigos de vinculación.
- Mappers, validaciones y deserialización de importes.
- Procesamiento de comandos de Telegram.

Las dependencias se sustituyen por dobles de prueba con Mockito. De esta forma se comprueba la lógica sin cargar Spring, acceder a MySQL ni llamar a Telegram.

### 2. Tests de integración

Comprueban puntos concretos de colaboración entre la aplicación y su infraestructura:

- Repositorios JPA, Hibernate y una instancia real de MySQL 8.4 mediante Testcontainers.
- Capa web de todos los controllers mediante `@WebMvcTest` y MockMvc.
- Serialización, validación y tratamiento global de errores.
- Persistencia de usuarios, gastos, límites y códigos temporales de Telegram.

Los tests web simulan los servicios porque su objetivo es verificar el contrato HTTP. Los tests de repositorio, en cambio, utilizan MySQL real. Esta separación mantiene visible qué integración comprueba cada clase.

### 3. Tests end-to-end

Se mantiene un conjunto reducido que levanta la aplicación en un puerto real y recorre todas las capas internas:

> Estos E2E validan el backend completo mediante peticiones HTTP reales. No son pruebas de navegador ni automatizan todavía la interfaz React.

- Registro de un usuario.
- Inicio de sesión de un usuario registrado.
- Creación y consulta de un gasto persistido.
- Configuración y consulta de un límite por categoría.
- Generación de un informe PDF usando datos persistidos.
- Recepción de un webhook y envío de una respuesta a Telegram.

Los cinco primeros recorridos siguen este camino:

```text
HTTP → Controller → Service → Repository → MySQL Testcontainers
```

El recorrido de Telegram atraviesa el límite externo de la aplicación:

```text
Webhook HTTP → Controller → TelegramService → TelegramClient → WireMock
```

WireMock sustituye a la API real de Telegram durante la prueba. Así se comprueban la URL, el método y el cuerpo enviados sin utilizar un token real, depender de Internet ni mandar mensajes a usuarios.

Los tests del frontend con Vitest y React Testing Library forman parte de la evolución futura del proyecto.

## Alcance actual y deuda técnica conocida

La deuda técnica se mantiene documentada como parte del aprendizaje y no se presenta como funcionalidad terminada:

- **Autenticación:** actualmente se utiliza la cabecera `X-User-Id` para centrar el ejercicio en la integración entre el backend y Telegram. Antes de considerar la aplicación preparada para producción se incorporaría autenticación y autorización con Spring Security y JWT. JWT ya se ha practicado en otros proyectos del portfolio, pero su integración en KakeBot queda pendiente.
- **Migraciones de base de datos:** durante esta etapa Hibernate gestiona el esquema con `ddl-auto=update`. Una evolución natural sería versionar los cambios mediante Flyway o Liquibase.
- **Automatización de calidad:** Render realiza el autodespliegue de la aplicación, pero el `Dockerfile` omite los tests durante la construcción. Falta un flujo de integración continua independiente que ejecute los tests de backend y frontend antes de desplegar.
- **Frontend:** la pirámide actual cubre el backend. Queda pendiente añadir pruebas automatizadas de componentes y flujos de la interfaz React.

Los secretos reales no se guardan en el repositorio: la contraseña de MySQL y el token de Telegram se reciben mediante variables de entorno, y el archivo `.env` local está excluido de Git.

## Próximos pasos

1. Incorporar autenticación y autorización con Spring Security y JWT.
2. Versionar el esquema de MySQL con Flyway o Liquibase.
3. Incorporar tests del frontend con Vitest y React Testing Library.
4. Ejecutar automáticamente los tests antes del autodespliegue de Render.
5. Ampliar el bot para registrar y consultar más información desde Telegram.
6. Continuar mejorando la experiencia de usuario y el despliegue.

## Ejecución local

### Requisitos

- Java 21
- Node.js 22 o compatible
- MySQL
- Docker para los tests de integración con Testcontainers
- Un bot de Telegram para probar la integración real

### Variables de entorno

Crea un archivo `.env` a partir de `.env.example` y configura:

```env
DB_URL=jdbc:mysql://localhost:3306/kakebot?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Madrid
DB_USERNAME=root
DB_PASSWORD=your_password
TELEGRAM_BOT_TOKEN=your_bot_token
```

### Backend

```bash
./mvnw spring-boot:run
```

Swagger UI estará disponible en `http://localhost:8080/swagger-ui/index.html`.

### Frontend

```bash
cd frontend
npm ci
npm run dev
```

Para generar la versión de producción:

```bash
cd frontend
npm run build
```

### Tests del backend

```bash
./mvnw test
```

Docker debe estar en ejecución porque los tests de persistencia y los E2E levantan MySQL 8.4 mediante Testcontainers. WireMock se inicia automáticamente para el recorrido de Telegram; no se necesita un token real para ejecutar la suite.

## Despliegue

El proyecto se ha probado desplegando el backend en Render y utilizando una base de datos MySQL alojada en Railway. Estas plataformas forman parte del entorno de aprendizaje y pueden cambiar durante la evolución del proyecto.

## Documentación adicional

El repositorio incluye documentos con decisiones de aprendizaje y el histórico de preguntas y respuestas utilizadas durante el desarrollo:

- `LEARNING_PACT.md`
- `LEARNING_QA_LOG.md`

## Licencia

Este proyecto se distribuye bajo los términos indicados en [LICENSE](LICENSE).
