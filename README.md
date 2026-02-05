# CRM Application (Spring Boot + MySQL + Docker)

This is a simple CRM REST API built with Spring Boot, using MySQL for data persistence and containerized with Docker.

## Project Structure
- `src/main/java`: Spring Boot source code (Entities, DTOs, Controllers, Services)
- `src/main/resources`: Configuration and Flyway migrations
- `Dockerfile`: Multi-stage build for the Spring Boot app
- `docker-compose.yml`: Orchestrates the app and MySQL containers

## Prerequisites
- Docker and Docker Compose
- Java 17 and Maven (for local running without Docker)

## How to Run

### Using Docker Compose (Recommended)
1. Build and start the containers:
   ```bash
   docker-compose up --build
   ```
2. The app will be available at `http://localhost:8080`.
3. MySQL is exposed on `localhost:3306` (Tradeoff: convenient for local dev, but should be hidden in production).

### Local Running (Manual)
1. Build the JAR:
   ```bash
   mvn clean package -DskipTests
   ```
2. Run the JAR (requires local MySQL):
   ```bash
   java -jar target/crm-app-0.0.1-SNAPSHOT.jar
   ```

## API Endpoints & Curl Examples

### 1. Create a Customer
```bash
curl -X POST http://localhost:8080/api/customers \
     -H "Content-Type: application/json" \
     -d '{
       "firstName": "John",
       "lastName": "Doe",
       "email": "john.doe@example.com",
       "phone": "1234567890",
       "status": "ACTIVE"
     }'
```

### 2. Get All Customers (with Pagination & Search)
```bash
curl "http://localhost:8080/api/customers?search=John&status=ACTIVE&page=0&size=10"
```

### 3. Get Customer by ID
```bash
curl http://localhost:8080/api/customers/1
```

### 4. Update Customer
```bash
curl -X PUT http://localhost:8080/api/customers/1 \
     -H "Content-Type: application/json" \
     -d '{
       "firstName": "John",
       "lastName": "Smith",
       "email": "john.smith@example.com",
       "phone": "0987654321",
       "status": "PROSPECT"
     }'
```

### 5. Delete Customer
```bash
curl -X DELETE http://localhost:8080/api/customers/1
```

### 6. Health Check (Actuator)
```bash
curl http://localhost:8080/actuator/health
```

## Troubleshooting
- **Communications link failure**: Usually means the DB isn't ready. The `depends_on` healthcheck in `docker-compose.yml` handles this.
- **Access denied for user 'root'**: Check the `MYSQL_ROOT_PASSWORD` and `MYSQL_PASSWORD` env vars match.
- **Flyway Migration Failure**: Ensure the SQL script in `src/main/resources/db/migration` is valid for MySQL 8.0.

## Connect to MySQL Container
```bash
docker exec -it crm-mysql mysql -uroot -prootpassword crm_db
```

## View Logs
```bash
docker-compose logs -f app
```
