# MapStruct and Lombok Integration: Task List

## Phase 1: Technical Configuration (Maven Setup)
1. [x] Add `<org.mapstruct.version>1.6.3</org.mapstruct.version>` to `pom.xml` properties.
2. [x] Add `<lombok-mapstruct-binding.version>0.2.0</lombok-mapstruct-binding.version>` to `pom.xml` properties.
3. [x] Add `org.mapstruct:mapstruct` dependency to the `<dependencies>` section in `pom.xml`.
4. [x] Configure `maven-compiler-plugin` in `pom.xml` to include `org.projectlombok:lombok` in `<annotationProcessorPaths>`.
5. [x] Configure `maven-compiler-plugin` in `pom.xml` to include `org.projectlombok:lombok-mapstruct-binding` in `<annotationProcessorPaths>`.
6. [x] Configure `maven-compiler-plugin` in `pom.xml` to include `org.mapstruct:mapstruct-processor` in `<annotationProcessorPaths>`.
7. [x] Run `./run-docker.sh compile` to verify the build and check for generated sources in `target/generated-sources/annotations`.

## Phase 2: Create Mapping Layer
8. [x] Create the base package `com.esse.crm.mapper`.
9. [x] Implement `LeadMapper.java` with `toDTO`, `toEntity`, and `updateLeadFromDto`.
10. [x] Implement `AccountMapper.java` for the Account module.
11. [x] Implement `ContactMapper.java` for the Contact module.
12. [x] Implement `OpportunityMapper.java` for the Opportunity module.
13. [x] Implement `ActivityMapper.java` for the Activity module.
14. [x] Ensure all mappers use `componentModel = "spring"` and `unmappedTargetPolicy = ReportingPolicy.IGNORE`.

## Phase 3: Service Layer Refactoring
15. [x] Inject `LeadMapper` into `LeadService` and replace `ModelMapper` calls.
16. [x] Inject `AccountMapper` into `AccountService` and replace `ModelMapper` calls.
17. [x] Inject `ContactMapper` into `ContactService` and replace `ModelMapper` calls.
18. [x] Inject `OpportunityMapper` into `OpportunityService` and replace `ModelMapper` calls.
19. [x] Inject `ActivityMapper` into `ActivityService` (or relevant service) and replace `ModelMapper` calls.
20. [x] Refactor all update operations to use `@MappingTarget` methods.
21. [x] Remove private `convertToDTO` and `convertToEntity` helper methods from all services.

## Phase 4: Verification and Testing
22. [x] Create unit tests for `LeadMapper` in `src/test/java/com/esse/crm/mapper/`.
23. [x] Create unit tests for other mappers (Account, Contact, etc.).
24. [x] Run all unit and integration tests using `./run-docker.sh test`.
25. [x] Perform full verification with `./run-docker.sh verify`.

## Phase 5: Cleanup
26. [x] Remove `org.modelmapper:modelmapper` dependency from `pom.xml`.
27. [x] Delete `ModelMapperConfig.java` if present.
28. [x] Perform a final build and test cycle to ensure a clean state.
29. [x] Update project documentation to reflect the new mapping standards.
