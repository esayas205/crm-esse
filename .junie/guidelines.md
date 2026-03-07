# CRM Application Guidelines

## Tech Stack
- **Java 17** with **Spring Boot 3.2.2**
- **MySQL 8.0** (production) / **H2** (tests)
- **Flyway** for database migrations
- **JWT** for authentication (access: 15min, refresh: 7 days)
- **Lombok** for boilerplate reduction
- **SpringDoc OpenAPI** for API documentation

## Project Structure
```
src/main/java/com/esse/crm/
├── config/          # App configuration classes
├── controller/      # REST controllers
├── dto/             # Data Transfer Objects
├── entity/          # JPA entities
├── exception/       # Custom exceptions & GlobalExceptionHandler
├── repository/      # Spring Data JPA repositories
├── security/        # JWT auth, filters, security config
│   ├── config/
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── filter/
│   ├── repository/
│   └── service/
└── service/         # Business logic layer
```

## Development Environment

**IMPORTANT: All operations run through Docker Compose. Nothing runs directly on the host computer.**

### Prerequisites
- Docker and Docker Compose installed

### Running the Application
```bash
docker-compose up --build
```
App available at `http://localhost:8080`, Swagger UI at `/swagger-ui.html`.

### Building the Project
```bash
./run-docker.sh clean package     # Clean and build
./run-docker.sh compile           # Compile only
```

### Running Tests
```bash
./run-docker.sh test              # Run all tests
./run-docker.sh verify            # Full verification with Failsafe
```

### Running Any Maven Command
```bash
./run-docker.sh <maven-command>   # Run any Maven command through Docker
```

Examples:
```bash
./run-docker.sh clean install -DskipTests
./run-docker.sh dependency:tree
./run-docker.sh test -Dtest=AccountControllerIT
```

### Viewing Logs
```bash
docker-compose logs -f app        # Follow application logs
```

### Connecting to MySQL
```bash
docker exec -it crm-mysql mysql -uroot -prootpassword crm_db
```

## Test Configuration
Tests use:
- `@ActiveProfiles("test")` with H2 in-memory database (MySQL mode)
- `@SpringBootTest` + `@AutoConfigureMockMvc` for integration tests
- `@WithMockUser` for security context in tests
- Test naming: `*Test.java` (unit), `*IT.java` (integration)

## Code Style & Conventions

### Entities
- Use Lombok: `@Data`, `@SuperBuilder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Extend `AuditedEntity` for audit fields (createdAt, updatedAt, createdBy, updatedBy)
- Use `@ToString.Exclude` on lazy collections to prevent N+1 issues
- Use `@Builder.Default` for collection initialization

```java
@Entity
@Table(name = "accounts")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Account extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToMany(mappedBy = "account")
    @Builder.Default
    @ToString.Exclude
    private List<Contact> contacts = new ArrayList<>();
}
```

### DTOs
- Use Lombok: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Use Jakarta validation annotations with custom messages

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    @NotBlank(message = "Account name is required")
    private String accountName;
    
    @NotNull(message = "Status is required")
    private AccountStatus status;
}
```

### Controllers
- Use `@RequiredArgsConstructor` for constructor injection
- Use `@PreAuthorize` for method-level security
- Authority naming: `{ENTITY}_READ`, `{ENTITY}_WRITE`, `ROLE_ADMIN`
- Return `ResponseEntity` with appropriate HTTP status codes

```java
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    @PreAuthorize("hasAuthority('ACCOUNT_WRITE')")
    public ResponseEntity<AccountDTO> createAccount(@Valid @RequestBody AccountDTO dto) {
        return new ResponseEntity<>(accountService.createAccount(dto), HttpStatus.CREATED);
    }
}
```

### Services
- Use `@Service` and `@RequiredArgsConstructor`
- Use `@Transactional(readOnly = true)` for read operations
- Use `@Transactional` for write operations
- Manual DTO-Entity conversion using builder pattern
- Throw `ResourceNotFoundException` for missing entities

```java
@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public AccountDTO getAccountById(Long id) {
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
        return convertToDTO(account);
    }
}
```

### Exception Handling
- `ResourceNotFoundException` → 404 Not Found
- `ConflictException` → 409 Conflict
- Validation errors → 400 Bad Request with field-level errors
- All exceptions handled by `GlobalExceptionHandler`

## Database Migrations
- Location: `src/main/resources/db/migration/`
- Naming: `V{version}__{Description}.sql` (e.g., `V1__Initial_Schema.sql`)
- Flyway runs automatically on startup (disabled in tests)

## Testing Guidelines

### Integration Test Pattern
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN", authorities = {"ACCOUNT_READ", "ACCOUNT_WRITE"})
public class AccountControllerIT {
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up repositories in correct order (respect FK constraints)
        activityRepository.deleteAll();
        opportunityRepository.deleteAll();
        accountRepository.deleteAll();
    }
    
    @Test
    void shouldCreateAccount() throws Exception {
        AccountDTO dto = AccountDTO.builder()
            .accountName("Test Corp")
            .status(AccountStatus.ACTIVE)
            .build();

        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accountName", is("Test Corp")));
    }
}
```

## API Documentation
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Use "Authorize" button in Swagger UI with JWT token from `/api/auth/login`

## Key Endpoints
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Get JWT tokens
- `POST /api/auth/refresh` - Refresh access token
- `GET /actuator/health` - Health check (public)
