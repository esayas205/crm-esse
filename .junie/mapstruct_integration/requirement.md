# Technical Requirement Document: MapStruct and Lombok Integration

## 1. Overview

### Purpose of Integrating MapStruct
The CRM application currently uses `ModelMapper` and manual mapping in some services to handle transformations between JPA Entities and Data Transfer Objects (DTOs). While functional, this approach lacks compile-time safety and can become a performance bottleneck as the application scales. Integrating MapStruct will provide a type-safe, performant, and maintainable way to handle these conversions.

### Benefits over Manual Mapping
- **Performance:** MapStruct generates plain Java code at compile time, which is as fast as manual mapping and significantly faster than reflection-based libraries like ModelMapper.
- **Compile-Time Safety:** Mapping errors (e.g., mismatched types or missing properties) are caught during the build process.
- **Reduced Boilerplate:** Automatically handles standard mappings, nested objects, and collection transformations.
- **Maintainability:** Clear separation between business logic and mapping logic.

### Relationship between MapStruct and Lombok
MapStruct and Lombok both operate during the annotation processing phase of compilation. To work together:
1. Lombok generates the necessary getters, setters, and builders.
2. MapStruct uses these generated methods to create the mapper implementations.
A specific binding dependency is required to ensure they execute in the correct order.

### Expected Improvements
- **Performance:** Elimination of reflection during runtime mapping.
- **Reliability:** Reduced risk of `NullPointerException` or runtime mapping failures.
- **Developer Productivity:** Faster implementation of new modules with automated mapping.

---

## 2. Scope
The integration covers the entire CRM backend mapping layer, specifically:
- **Modules:** Account, Contact, Lead, Opportunity, Activity.
- **Transformations:** Entity-to-DTO (read), DTO-to-Entity (create), and partial Entity updates from DTOs.
- **Patterns:** Reusable mapper interfaces following a consistent architectural standard.

---

## 3. Architecture Design

### Mapping Layer Placement
The mapping layer sits between the **Repository** (Entity) and the **Controller** (DTO), managed by the **Service** layer.

### Recommended Package Structure
```text
com.esse.crm.
├── controller/      # Receives DTOs
├── service/         # Orchestrates logic, uses Mappers
├── mapper/          # MapStruct Mapper interfaces (NEW)
├── dto/             # Data Transfer Objects
└── entity/          # JPA Entities
```

### Interaction Flow
1. **Controller** receives a request with a `DTO`.
2. **Service** receives the `DTO`.
3. **Service** uses the `Mapper` to convert `DTO` → `Entity`.
4. **Service** interacts with the **Repository** using the `Entity`.
5. **Service** uses the `Mapper` to convert the result `Entity` → `DTO` and returns it to the **Controller**.

---

## 4. Technical Requirements

### Maven Dependencies
The following dependencies and configurations must be added to `pom.xml`:

```xml
<properties>
    <org.mapstruct.version>1.6.3</org.mapstruct.version>
    <lombok-mapstruct-binding.version>0.2.0</lombok-mapstruct-binding.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>${org.mapstruct.version}</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>${java.version}</source>
                <target>${java.version}</target>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </path>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok-mapstruct-binding</artifactId>
                        <version>${lombok-mapstruct-binding.version}</version>
                    </path>
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>${org.mapstruct.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

## 5. Mapping Design Standards

### Core Standards
- **Component Model:** Use `componentModel = "spring"` to allow mappers to be injected as Spring Beans.
- **Naming Conventions:**
    - Mappers: `{EntityName}Mapper` (e.g., `LeadMapper`).
    - Methods: `toDTO(Entity entity)`, `toEntity(DTO dto)`.
- **DTO Standards:** DTOs should reflect the API requirements and use Jakarta validation.
- **Updates:** Use `@MappingTarget` for updating existing entities from DTOs to avoid unnecessary DB fetches or overwriting audit fields.
- **Collections:** MapStruct automatically handles `List<Entity>` to `List<DTO>` if the singular mapping is defined.

---

## 6. Implementation Guidelines

### Integration Steps
1. Add dependencies and Maven plugin configuration.
2. Create `com.esse.crm.mapper` package.
3. Define Mapper interfaces with `@Mapper(componentModel = "spring")`.
4. Replace `ModelMapper` or manual conversion in Services with injected Mappers.
5. Remove `ModelMapper` dependency once migration is complete.

### IntelliJ IDEA Configuration
To ensure smooth development in IntelliJ:
1. Go to **Settings > Build, Execution, Deployment > Compiler > Annotation Processors**.
2. Check **Enable annotation processing**.
3. Ensure the project is built using Maven (`Build > Rebuild Project`).

---

## 7. Example Implementation: Lead Module

### Lead Mapper Interface
```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LeadMapper {

    LeadDTO toDTO(Lead lead);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activities", ignore = true)
    Lead toEntity(LeadDTO leadDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activities", ignore = true)
    void updateLeadFromDto(LeadDTO leadDTO, @MappingTarget Lead lead);
}
```

### Example Service Usage
```java
@Service
@RequiredArgsConstructor
public class LeadService {
    private final LeadRepository leadRepository;
    private final LeadMapper leadMapper;

    @Transactional
    public LeadDTO updateLead(Long id, LeadDTO leadDTO) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found"));
        
        // Use MapStruct for clean partial update
        leadMapper.updateLeadFromDto(leadDTO, lead);
        
        return leadMapper.toDTO(leadRepository.save(lead));
    }
}
```

---

## 8. Non-Functional Requirements
- **Maintainability:** Centralized mapping logic reduces effort when fields change.
- **Performance:** Native Java code execution with zero reflection.
- **Scalability:** Easy to apply to new modules by following the established interface pattern.

---

## 9. Error Handling & Validation
- **Null Safety:** MapStruct handles null checks by default (returns null if source is null).
- **Default Values:** Use `defaultValue` in `@Mapping` for optional fields.
- **Safe Practices:** Explicitly ignore unmapped targets in the `@Mapper` configuration to prevent build warnings.

---

## 10. Testing Strategy
- **Unit Tests:** Test Mappers in isolation using `Mappers.getMapper(LeadMapper.class)`.
- **Integration Tests:** Verify mapping works correctly within the Spring context during integration tests.

---

## 11. Risks and Considerations
- **Annotation Processing Order:** Critical to have Lombok, then Lombok-MapStruct-Binding, then MapStruct-Processor.
- **Cycle Dependencies:** Careful when mapping bidirectional relationships (e.g., Account ↔ Contact). Use `@Context` or specialized mapping methods if needed.

---

## 12. Acceptance Criteria
- [ ] MapStruct implementation classes are generated in `target/generated-sources`.
- [ ] No compilation conflicts between Lombok and MapStruct.
- [ ] Services use `Mapper` beans instead of `ModelMapper` or manual sets.
- [ ] All unit and integration tests for Lead module pass with the new mapping logic.
