# CRM Application (Spring Boot + MySQL + Docker)

This is a comprehensive CRM REST API built with Spring Boot, using MySQL for data persistence and containerized with Docker. The system supports Leads, Accounts, Contacts, Opportunities, and Activities.

## Project Structure
- `src/main/java`: Spring Boot source code (Entities, DTOs, Controllers, Services)
- `src/main/resources`: Configuration and Flyway migrations
- `Dockerfile`: Multi-stage build for the Spring Boot app
- `docker-compose.yml`: Orchestrates the app and MySQL containers
- `docker-compose.test.yml`: Setup for integration tests

## Prerequisites
- Docker and Docker Compose
- Java 11 and Maven (for local running without Docker)

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

## Key Modules & Endpoints

### 1. Leads
Manage potential customers and convert them to Accounts/Contacts.
- `POST /api/leads`: Create a lead
- `GET /api/leads`: Search leads (pagination, filtering)
- `POST /api/leads/{id}/convert`: Convert lead to Account, Contact, and Opportunity

### 2. Accounts & Contacts
Accounts represent companies, while Contacts are individuals associated with an Account.
- `GET /api/accounts`: List accounts
- `POST /api/accounts`: Create an account
- `GET /api/accounts/{id}/contacts`: Get contacts for an account
- `GET /api/contacts`: List all contacts

### 3. Opportunities
Track potential sales deals.
- `GET /api/opportunities`: Search opportunities by stage, account, amount, etc.
- `PATCH /api/opportunities/{id}/stage`: Advance an opportunity stage

### 4. Activities
Track interactions (Calls, Emails, Meetings) with Leads, Accounts, Contacts, or Opportunities.
- `GET /api/activities`: Search activities with various filters
- `PATCH /api/activities/{id}/complete`: Mark an activity as completed

### 5. Health Check (Actuator)
```bash
curl http://localhost:8080/actuator/health
```

## Example: Create and Convert a Lead

1. **Create a Lead**
```bash
curl -X POST http://localhost:8080/api/leads \
     -H "Content-Type: application/json" \
     -d '{
       "firstName": "John",
       "lastName": "Doe",
       "email": "john.doe@company.com",
       "company": "Tech Corp",
       "status": "NEW",
       "source": "WEBSITE"
     }'
```

2. **Convert the Lead**
```bash
curl -X POST http://localhost:8080/api/leads/1/convert
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
