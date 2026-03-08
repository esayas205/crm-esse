# Implementation Plan: MapStruct and Lombok Integration

This document outlines the step-by-step plan to integrate MapStruct with Lombok in the CRM application, based on the requirements defined in `requirement.md`.

## Phase 1: Technical Configuration (Maven Setup)

1.  **Modify `pom.xml` Properties:**
    - Add `org.mapstruct.version` (1.6.3)
    - Add `lombok-mapstruct-binding.version` (0.2.0)
2.  **Add Dependencies:**
    - Add `org.mapstruct:mapstruct` to the `<dependencies>` section.
3.  **Configure `maven-compiler-plugin`:**
    - Update `<annotationProcessorPaths>` to include:
        - `org.projectlombok:lombok`
        - `org.projectlombok:lombok-mapstruct-binding`
        - `org.mapstruct:mapstruct-processor`
4.  **Verification:**
    - Run `./run-docker.sh compile` to ensure implementation classes are generated in `target/generated-sources/annotations`.

## Phase 2: Create Mapping Layer

1.  **Package Setup:**
    - Create the base package: `com.esse.crm.mapper`.
2.  **Lead Module Mapper:**
    - Create `LeadMapper.java` interface.
    - Implement `toDTO`, `toEntity`, and `updateLeadFromDto` with `@MappingTarget`.
3.  **Other Modules (Account, Contact, Opportunity, Activity):**
    - Create corresponding Mapper interfaces for each entity.
    - Handle collections (e.g., `List<Activity>`) by defining standard mapping methods.
4.  **Global Configuration:**
    - Ensure all mappers use `componentModel = "spring"`.
    - Set `unmappedTargetPolicy = ReportingPolicy.IGNORE` in `@Mapper` annotation to keep builds clean.

## Phase 3: Service Layer Refactoring

1.  **Dependency Injection:**
    - Replace `ModelMapper` with the specific Mapper bean (e.g., `LeadMapper`) in each service.
2.  **Method Migration:**
    - Replace `modelMapper.map(source, Target.class)` with `mapper.toTarget(source)`.
3.  **Refactor Update Operations:**
    - Use `mapper.updateEntityFromDto(dto, entity)` to perform clean partial updates in transactional methods.
4.  **Remove Helper Methods:**
    - Delete private `convertToDTO` and `convertToEntity` methods once the Mapper is fully integrated.

## Phase 4: Verification and Testing

1.  **Unit Tests:**
    - Create/Update tests for the new Mappers to ensure field parity.
2.  **Integration Tests:**
    - Run existing integration tests to verify no regression in API behavior.
    - Run `./run-docker.sh test`.
3.  **Full Verification:**
    - Run `./run-docker.sh verify` to ensure the entire build pipeline (including Failsafe) passes.

## Phase 5: Cleanup

1.  **Remove ModelMapper:**
    - Delete the `modelMapper` dependency from `pom.xml`.
    - Remove `ModelMapperConfig.java` if it exists.
2.  **Documentation Update:**
    - Update any relevant internal documentation or README if mapping standards have changed.
