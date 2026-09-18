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
- JUnit 5 y Testcontainers

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

Uno de los siguientes objetivos principales de KakeBot es convertir el proyecto en un ejercicio completo de pruebas automatizadas. Se aplicará la pirámide de tests para mantener una suite rápida, fiable y fácil de mantener.

### 1. Tests unitarios

Constituirán la base más amplia de la pirámide. Verificarán de forma aislada:

- Reglas de negocio de usuarios y gastos.
- Cálculo de totales y periodos.
- Límites mensuales y condiciones de envío de alertas.
- Generación y caducidad de códigos de vinculación.
- Mappers, validaciones y deserialización de importes.
- Procesamiento de comandos de Telegram.

Las dependencias externas se sustituirán por dobles de prueba con Mockito.

### 2. Tests de integración

Comprobarán la colaboración entre las distintas capas:

- Repositorios JPA contra MySQL mediante Testcontainers.
- Endpoints REST con Spring Boot y MockMvc.
- Serialización, validación y tratamiento global de errores.
- Persistencia de usuarios, gastos, límites y códigos temporales.

### 3. Tests end-to-end

Se mantendrá un conjunto reducido para validar los flujos más importantes desde la perspectiva del usuario:

- Registro, inicio de sesión y consulta del perfil.
- Creación y consulta de un gasto.
- Configuración de un límite por categoría.
- Generación y consumo de un código de vinculación con Telegram.

La API real de Telegram no se utilizará en todos los tests. Sus respuestas se simularán para que la suite sea determinista, dejando comprobaciones manuales o específicas para validar la integración real.

También se incorporarán tests del frontend con Vitest y React Testing Library, medición de cobertura del backend con JaCoCo y ejecución automática mediante integración continua.

## Deuda técnica conocida

- La autenticación actual identifica al usuario mediante la cabecera `X-User-Id`; debe sustituirse por Spring Security con JWT o sesiones.
- La cobertura de tests es todavía mínima y debe ampliarse siguiendo la pirámide descrita anteriormente.
- El frontend aún no dispone de tests automatizados.
- Falta integración continua para ejecutar tests, lint y builds en cada cambio.
- La configuración de ESLint debe excluir correctamente los artefactos generados y resolver los errores pendientes del código fuente.
- La imagen de MySQL utilizada por Testcontainers debe fijarse a una versión concreta.
- `spring.jpa.hibernate.ddl-auto=update` debe reemplazarse por migraciones versionadas con Flyway o Liquibase.
- El webhook de Telegram debe validar un secreto que permita comprobar el origen de las peticiones.
- Deben mejorarse el registro de errores, la observabilidad y la configuración específica por entorno.
- Es necesario seguir ampliando la documentación de la API y de las decisiones arquitectónicas.

Documentar esta deuda permite diferenciar las simplificaciones tomadas para aprender de las decisiones necesarias en una aplicación preparada para producción.

## Próximos pasos

1. Crear tests unitarios para los servicios y las reglas de negocio principales.
2. Añadir tests de controladores, repositorios e integración con Testcontainers.
3. Incorporar tests del frontend.
4. Añadir JaCoCo y establecer un objetivo inicial de cobertura.
5. Configurar GitHub Actions para backend y frontend.
6. Implementar autenticación y autorización con Spring Security.
7. Añadir migraciones de base de datos con Flyway o Liquibase.
8. Proteger el webhook de Telegram.
9. Ampliar el bot para registrar y consultar gastos desde Telegram.
10. Mejorar el despliegue, la observabilidad y la experiencia de usuario.

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

Los tests de integración basados en Testcontainers requieren que Docker esté en ejecución. La suite de pruebas se encuentra actualmente en desarrollo.

## Despliegue

El proyecto se ha probado desplegando el backend en Render y utilizando una base de datos MySQL alojada en Railway. Estas plataformas forman parte del entorno de aprendizaje y pueden cambiar durante la evolución del proyecto.

## Documentación adicional

El repositorio incluye documentos con decisiones de aprendizaje y el histórico de preguntas y respuestas utilizadas durante el desarrollo:

- `LEARNING_PACT.md`
- `LEARNING_QA_LOG.md`

## Licencia

Este proyecto se distribuye bajo los términos indicados en [LICENSE](LICENSE).
