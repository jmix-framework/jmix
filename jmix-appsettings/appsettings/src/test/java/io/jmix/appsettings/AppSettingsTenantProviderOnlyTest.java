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
import io.jmix.appsettings.test_support.TestAppSettingsTenantProvider;
import io.jmix.appsettings.impl.AppSettingsTenantSupport;
import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.data.PersistenceHints;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import test_support.AppSettingsTenantProviderOnlyTestConfiguration;

import java.util.List;
import java.util.function.Supplier;

@SpringJUnitConfig(classes = AppSettingsTenantProviderOnlyTestConfiguration.class)
class AppSettingsTenantProviderOnlyTest {

    @Autowired
    private AppSettings appSettings;

    @Autowired
    private UnconstrainedDataManager dataManager;

    @Autowired
    private Metadata metadata;

    @Autowired
    private TestAppSettingsTenantProvider tenantProvider;

    @Autowired
    private AppSettingsTenantSupport tenantSupport;

    @AfterEach
    void tearDown() {
        tenantProvider.clear();
        List<TestAppSettingsEntity> storedEntities = dataManager.load(TestAppSettingsEntity.class)
                .all()
                .hint(PersistenceHints.SOFT_DELETION, false)
                .list();

        if (!storedEntities.isEmpty()) {
            SaveContext saveContext = new SaveContext()
                    .setHint(PersistenceHints.SOFT_DELETION, false);
            for (TestAppSettingsEntity storedEntity : storedEntities) {
                saveContext.removing(storedEntity);
            }
            dataManager.save(saveContext);
        }
    }

    @Test
    void testLoadTenantSpecificSettingsWithCustomTenantProvider() {
        TestAppSettingsEntity globalSettings = metadata.create(TestAppSettingsEntity.class, 1);
        globalSettings.setTestStringValue("global");

        TestAppSettingsEntity tenantASettings = metadata.create(TestAppSettingsEntity.class, 1001);
        tenantASettings.setTenantId("tenantA");
        tenantASettings.setTestStringValue("tenantA");

        TestAppSettingsEntity tenantBSettings = metadata.create(TestAppSettingsEntity.class, 1002);
        tenantBSettings.setTenantId("tenantB");
        tenantBSettings.setTestStringValue("tenantB");

        dataManager.save(new SaveContext().saving(globalSettings, tenantASettings, tenantBSettings));

        TestAppSettingsEntity storedTenantASettings = dataManager.load(TestAppSettingsEntity.class)
                .condition(PropertyCondition.equal("tenantId", "tenantA"))
                .one();
        Assertions.assertEquals(1001, storedTenantASettings.getId());

        TestAppSettingsEntity loadedTenantASettings = withTenant("tenantA",
                () -> {
                    Assertions.assertEquals("tenantA", tenantProvider.getCurrentTenantId());
                    Assertions.assertEquals("tenantA", tenantSupport.getCurrentTenantId());
                    return appSettings.load(TestAppSettingsEntity.class);
                });
        Assertions.assertEquals(1001, loadedTenantASettings.getId());
        Assertions.assertEquals("tenantA", loadedTenantASettings.getTenantId());
        Assertions.assertEquals("tenantA", loadedTenantASettings.getTestStringValue());

        TestAppSettingsEntity loadedTenantBSettings = withTenant("tenantB",
                () -> appSettings.load(TestAppSettingsEntity.class));
        Assertions.assertEquals(1002, loadedTenantBSettings.getId());
        Assertions.assertEquals("tenantB", loadedTenantBSettings.getTenantId());
        Assertions.assertEquals("tenantB", loadedTenantBSettings.getTestStringValue());
    }

    private <T> T withTenant(String tenantId, Supplier<T> action) {
        tenantProvider.setCurrentTenantId(tenantId);
        try {
            return action.get();
        } finally {
            tenantProvider.clear();
        }
    }
}
