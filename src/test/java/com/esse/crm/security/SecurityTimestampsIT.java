package com.esse.crm.security;

import com.esse.crm.security.entity.AppUser;
import com.esse.crm.security.entity.Permission;
import com.esse.crm.security.entity.Role;
import com.esse.crm.security.repository.AppUserRepository;
import com.esse.crm.security.repository.PermissionRepository;
import com.esse.crm.security.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SecurityTimestampsIT {

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Test
    void shouldPopulateTimestampsForAppUser() {
        AppUser user = AppUser.builder()
                .username("timestamp_user")
                .password("password")
                .email("timestamp@example.com")
                .build();

        AppUser savedUser = userRepository.saveAndFlush(user);

        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldPopulateTimestampsForRole() {
        Role role = Role.builder()
                .name("ROLE_TIMESTAMP")
                .build();

        Role savedRole = roleRepository.saveAndFlush(role);

        assertThat(savedRole.getCreatedAt()).isNotNull();
        assertThat(savedRole.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldPopulateTimestampsForPermission() {
        Permission permission = Permission.builder()
                .name("PERMISSION_TIMESTAMP")
                .build();

        Permission savedPermission = permissionRepository.saveAndFlush(permission);

        assertThat(savedPermission.getCreatedAt()).isNotNull();
        assertThat(savedPermission.getUpdatedAt()).isNotNull();
    }
}
