/*
 * Copyright 2026 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.appsettings;

import io.jmix.appsettings.test_entity.TestAppSettingsEntity;
import io.jmix.appsettings.test_support.entity.AppSettingsTestUser;
import io.jmix.appsettings.test_support.role.AppSettingsReadOnlyRole;
import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.data.PersistenceHints;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@SpringBootTest(classes = AppSettingsTestConfiguration.class)
@TestPropertySource(properties = "jmix.appsettings.check-permissions-for-app-settings-entity=true")
class AppSettingsPermissionEnforcementTest {

    @Autowired
    private AppSettings appSettings;

    @Autowired
    private UnconstrainedDataManager unconstrainedDataManager;

    @Autowired
    private Metadata metadata;

    @Autowired
    private RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        List<TestAppSettingsEntity> storedEntities = unconstrainedDataManager.load(TestAppSettingsEntity.class)
                .all()
                .hint(PersistenceHints.SOFT_DELETION, false)
                .list();

        if (!storedEntities.isEmpty()) {
            SaveContext saveContext = new SaveContext()
                    .setHint(PersistenceHints.SOFT_DELETION, false);
            for (TestAppSettingsEntity storedEntity : storedEntities) {
                saveContext.removing(storedEntity);
            }
            unconstrainedDataManager.save(saveContext);
        }
    }

    @Test
    void testSaveDeniedWhenUserHasReadOnlyRole() {
        TestAppSettingsEntity preExisting = metadata.create(TestAppSettingsEntity.class, 1);
        preExisting.setTestStringValue("original");
        unconstrainedDataManager.save(preExisting);

        TestAppSettingsEntity submitted = metadata.create(TestAppSettingsEntity.class, 1);
        submitted.setTestStringValue("policy-bypass");

        Assertions.assertThrows(AccessDeniedException.class,
                () -> asReadOnlyUser(() -> {
                    appSettings.save(submitted);
                    return null;
                }));

        TestAppSettingsEntity reloaded = unconstrainedDataManager.load(TestAppSettingsEntity.class)
                .id(1)
                .one();
        Assertions.assertEquals("original", reloaded.getTestStringValue());
    }

    private <T> T asReadOnlyUser(Supplier<T> action) {
        AppSettingsTestUser user = metadata.create(AppSettingsTestUser.class);
        user.setUsername("read-only-user");
        user.setPassword("{noop}read-only-user");
        GrantedAuthority authority = roleGrantedAuthorityUtils
                .createResourceRoleGrantedAuthority(AppSettingsReadOnlyRole.NAME);
        user.setAuthorities(Collections.singletonList(authority));
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, user.getPassword(), user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            return action.get();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
