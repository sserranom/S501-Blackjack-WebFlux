# ♠️ Reactive Blackjack API ♣️

This project implements a **Blackjack game API** using **Spring Boot WebFlux** (reactive programming), **MongoDB**, and **MySQL (R2DBC)**. The application is designed to be modular, testable, and easily deployable with Docker.

---

## 🚀 Key Features

- **Reactive API:** Built with Spring WebFlux for efficient concurrency handling and high performance.
- **Dual Persistence:** Uses MongoDB to store Blackjack game states and MySQL (via R2DBC) to manage player information and scores.
- **Modular Game Logic:** Game logic (card dealing, hand evaluation, outcome determination) is encapsulated in modular components like `Deck`, `HandEvaluator`, `GameOutcomeDeterminer`, and `BlackjackGameEngine`.
- **Type-Safe Enums:** Uses enums for game statuses, play types, card suits, and ranks to improve readability and reduce errors.
- **Centralized Exception Handling:** Implements custom exceptions and a `GlobalExceptionHandler` to provide clear and consistent error responses.
- **Robust Unit Testing:** Includes unit tests for core services and controllers using JUnit 5 and Mockito.
- **Dockerized:** Easily packaged and deployed with Docker alongside MongoDB and MySQL using Dockerfile and Docker Compose.

---

## 🛠️ Technologies Used

- Spring Boot (WebFlux, Spring Data Reactive MongoDB, Spring Data R2DBC)
- Java 23
- Maven 3.9+
- MongoDB
- MySQL 8.0
- Lombok
- Project Reactor
- JUnit 5
- Mockito
- Docker & Docker Compose
- Swagger/OpenAPI 3 (API documentation)

---

## ⚙️ Project Setup

### Prerequisites

- Java Development Kit (JDK) 23
- Apache Maven 3.9+
- Docker Desktop (Windows/macOS) or Docker Engine (Linux)

### Installation and Execution Steps

1. **Clone the Repository** or navigate to your project’s root folder if you already have the code.

2. **Verify Configuration Files:**
   - `pom.xml` → Maven build configuration.
   - `Dockerfile` → Instructions to build the Docker image.
   - `docker-compose.yml` → Orchestrates the application, MongoDB, and MySQL services.
   - `schema.sql` → SQL script to initialize the MySQL database (`players` table).
  

3. **Build the Java Application Locally:**

   ```bash
   mvn clean package -DskipTests

4. **▶️ API Usage

Once the application is running (`docker-compose up`):

### API Documentation (Swagger UI)

Access the Swagger UI at:

http://localhost:8080/swagger-ui.html

### Main Endpoints

- `POST /game/new` → Create a new Blackjack game.
- `GET /game/{id}` → Retrieve details of a specific game.
- `POST /game/{id}/play` → Make a play (HIT, STAND, DOUBLE_DOWN, SURRENDER).
- `GET /ranking` → Get the player rankings.

You can interact with these endpoints via Swagger, Postman, or any HTTP client.

---

## 🗄️ Direct Database Access (Optional)

### MongoDB

- **Host:** `localhost`
- **Port:** `27017`
- **Database:** `blackjack_reactive_mongo`
- You can use tools like **MongoDB Compass** or the `mongosh` shell.

### MySQL

- **Host:** `localhost`
- **Port:** `3307` (exposed by Docker)
- **Username:** `root`
- **Password:** *(configured in `docker-compose.yml`, typically: `1234`)*
- **Database:** `blackjack_reactive_mysql`
- You can use tools like **MySQL Workbench**, **DBeaver**, or any SQL client.

---

## 🛑 Stopping and Cleaning Up

To **stop all services and remove containers** (without deleting database data):

```bash
docker-compose down

