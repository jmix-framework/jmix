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
import io.jmix.appsettings.test_support.entity.TenantTestUser;
import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.data.PersistenceHints;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.function.Supplier;

@SpringBootTest(classes = AppSettingsTestConfiguration.class)
class AppSettingsTest {

    @Autowired
    private AppSettings appSettings;

    @Autowired
    private UnconstrainedDataManager dataManager;

    @Autowired
    private AppSettingsTools appSettingsTools;

    @Autowired
    private Metadata metadata;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
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
    void testGetDefaultValuesForAppSettings() {
        //ensure default values are returned without actual record in database
        TestAppSettingsEntity testAppSettingsEntity = appSettings.load(TestAppSettingsEntity.class);
        Assertions.assertEquals(1, testAppSettingsEntity.getId());
        Assertions.assertTrue(testAppSettingsEntity.getTestBooleanValue());
        Assertions.assertEquals(123, testAppSettingsEntity.getTestIntegerValue());
        Assertions.assertEquals(100500L, testAppSettingsEntity.getTestLongValue());
        Assertions.assertEquals(3.1415926535, testAppSettingsEntity.getTestDoubleValue());
        Assertions.assertEquals("defVal", testAppSettingsEntity.getTestStringValue());
        Assertions.assertEquals("systemDef", testAppSettingsEntity.getTestSystemLevelValue());


        //ensure non-null saved values override defaults and default values are returned for null values
        testAppSettingsEntity.setTestIntegerValue(410);
        testAppSettingsEntity.setTestStringValue("defValChanged");
        testAppSettingsEntity.setTestSystemLevelValue("systemChanged");
        appSettings.save(testAppSettingsEntity);
        TestAppSettingsEntity storedTestAppSettingsEntity = loadStoredTestAppSettingsEntity();
        Assertions.assertTrue(storedTestAppSettingsEntity.getTestBooleanValue());
        Assertions.assertEquals(100500L, storedTestAppSettingsEntity.getTestLongValue());
        Assertions.assertEquals(3.1415926535, storedTestAppSettingsEntity.getTestDoubleValue());
        Assertions.assertEquals("systemChanged", storedTestAppSettingsEntity.getTestSystemLevelValue());

        testAppSettingsEntity = appSettings.load(TestAppSettingsEntity.class);
        Assertions.assertEquals(1, testAppSettingsEntity.getId());
        Assertions.assertTrue(testAppSettingsEntity.getTestBooleanValue());
        Assertions.assertEquals(410, testAppSettingsEntity.getTestIntegerValue());
        Assertions.assertEquals(100500L, testAppSettingsEntity.getTestLongValue());
        Assertions.assertEquals(3.1415926535, testAppSettingsEntity.getTestDoubleValue());
        Assertions.assertEquals("defValChanged", testAppSettingsEntity.getTestStringValue());
        Assertions.assertEquals("systemChanged", testAppSettingsEntity.getTestSystemLevelValue());

        testAppSettingsEntity.setTestBooleanValue(false);
        testAppSettingsEntity.setTestLongValue(500100L);
        testAppSettingsEntity.setTestDoubleValue(2.7182818284);
        testAppSettingsEntity.setTestStringValue("access denied");
        testAppSettingsEntity.setTestSystemLevelValue("systemDenied");
        appSettings.save(testAppSettingsEntity);
        testAppSettingsEntity = appSettings.load(TestAppSettingsEntity.class);
        Assertions.assertEquals(1, testAppSettingsEntity.getId());
        Assertions.assertFalse(testAppSettingsEntity.getTestBooleanValue());
        Assertions.assertEquals(410, testAppSettingsEntity.getTestIntegerValue());
        Assertions.assertEquals(500100L, testAppSettingsEntity.getTestLongValue());
        Assertions.assertEquals(2.7182818284, testAppSettingsEntity.getTestDoubleValue());
        Assertions.assertEquals("access denied", testAppSettingsEntity.getTestStringValue());
        Assertions.assertEquals("systemDenied", testAppSettingsEntity.getTestSystemLevelValue());


        //ensure default values are returned for null values
        testAppSettingsEntity.setTestBooleanValue(null);
        testAppSettingsEntity.setTestIntegerValue(null);
        testAppSettingsEntity.setTestLongValue(null);
        testAppSettingsEntity.setTestDoubleValue(null);
        testAppSettingsEntity.setTestStringValue(null);
        testAppSettingsEntity.setTestSystemLevelValue(null);
        appSettings.save(testAppSettingsEntity);
        storedTestAppSettingsEntity = loadStoredTestAppSettingsEntity();
        Assertions.assertNull(storedTestAppSettingsEntity.getTestBooleanValue());
        Assertions.assertNull(storedTestAppSettingsEntity.getTestIntegerValue());
        Assertions.assertNull(storedTestAppSettingsEntity.getTestLongValue());
        Assertions.assertNull(storedTestAppSettingsEntity.getTestDoubleValue());
        Assertions.assertNull(storedTestAppSettingsEntity.getTestStringValue());
        Assertions.assertNull(storedTestAppSettingsEntity.getTestSystemLevelValue());

        testAppSettingsEntity = appSettings.load(TestAppSettingsEntity.class);
        Assertions.assertEquals(1, testAppSettingsEntity.getId());
        Assertions.assertTrue(testAppSettingsEntity.getTestBooleanValue());
        Assertions.assertEquals(123, testAppSettingsEntity.getTestIntegerValue());
        Assertions.assertEquals(100500L, testAppSettingsEntity.getTestLongValue());
        Assertions.assertEquals(3.1415926535, testAppSettingsEntity.getTestDoubleValue());
        Assertions.assertEquals("defVal", testAppSettingsEntity.getTestStringValue());
        Assertions.assertEquals("systemDef", testAppSettingsEntity.getTestSystemLevelValue());

        //ensure that only one record can exist for each application settings
        TestAppSettingsEntity createdTestAppSettingsEntity = metadata.create(TestAppSettingsEntity.class, 1);
        createdTestAppSettingsEntity.setTestBooleanValue(true);
        createdTestAppSettingsEntity.setTestLongValue(333L);
        createdTestAppSettingsEntity.setTestDoubleValue(6.626);
        createdTestAppSettingsEntity.setTestStringValue("access granted");
        createdTestAppSettingsEntity.setTestSystemLevelValue("systemGranted");
        appSettings.save(createdTestAppSettingsEntity);
        TestAppSettingsEntity loadedTestAppSettingsEntity = appSettings.load(TestAppSettingsEntity.class);
        Assertions.assertEquals(1, loadedTestAppSettingsEntity.getId());
        Assertions.assertTrue(loadedTestAppSettingsEntity.getTestBooleanValue());
        Assertions.assertEquals(123, loadedTestAppSettingsEntity.getTestIntegerValue());
        Assertions.assertEquals(333L, loadedTestAppSettingsEntity.getTestLongValue());
        Assertions.assertEquals(6.626, loadedTestAppSettingsEntity.getTestDoubleValue());
        Assertions.assertEquals("access granted", loadedTestAppSettingsEntity.getTestStringValue());
        Assertions.assertEquals("systemGranted", loadedTestAppSettingsEntity.getTestSystemLevelValue());
    }

    @Test
    void testTenantSpecificSettingsOverrideGlobalSettings() {
        TestAppSettingsEntity globalSettings = appSettings.load(TestAppSettingsEntity.class);
        globalSettings.setTestIntegerValue(410);
        globalSettings.setTestStringValue("global");
        appSettings.save(globalSettings);

        TestAppSettingsEntity tenantAFallback = withTenant("tenantA",
                () -> appSettings.load(TestAppSettingsEntity.class));
        Assertions.assertEquals(1, tenantAFallback.getId());
        Assertions.assertNull(tenantAFallback.getTenantId());
        Assertions.assertEquals(410, tenantAFallback.getTestIntegerValue());
        Assertions.assertEquals("global", tenantAFallback.getTestStringValue());

        tenantAFallback.setTestIntegerValue(777);
        tenantAFallback.setTestStringValue("tenantA");
        withTenant("tenantA", () -> {
            appSettings.save(tenantAFallback);
            return null;
        });

        TestAppSettingsEntity tenantASettings = withTenant("tenantA",
                () -> appSettings.load(TestAppSettingsEntity.class));
        Assertions.assertNotEquals(1, tenantASettings.getId());
        Assertions.assertEquals("tenantA", tenantASettings.getTenantId());
        Assertions.assertEquals(777, tenantASettings.getTestIntegerValue());
        Assertions.assertEquals("tenantA", tenantASettings.getTestStringValue());

        TestAppSettingsEntity tenantBFallback = withTenant("tenantB",
                () -> appSettings.load(TestAppSettingsEntity.class));
        Assertions.assertEquals(1, tenantBFallback.getId());
        Assertions.assertNull(tenantBFallback.getTenantId());
        Assertions.assertEquals(410, tenantBFallback.getTestIntegerValue());
        Assertions.assertEquals("global", tenantBFallback.getTestStringValue());

        TestAppSettingsEntity globalAfterTenantOverride = appSettings.load(TestAppSettingsEntity.class);
        Assertions.assertEquals(1, globalAfterTenantOverride.getId());
        Assertions.assertNull(globalAfterTenantOverride.getTenantId());
        Assertions.assertEquals(410, globalAfterTenantOverride.getTestIntegerValue());
        Assertions.assertEquals("global", globalAfterTenantOverride.getTestStringValue());

        List<TestAppSettingsEntity> storedEntities = dataManager.load(TestAppSettingsEntity.class)
                .query("select e from testAppSettingsEntity e order by e.id")
                .list();
        Assertions.assertEquals(2, storedEntities.size());
        Assertions.assertEquals(1, storedEntities.get(0).getId());
        Assertions.assertNull(storedEntities.get(0).getTenantId());
        Assertions.assertEquals("tenantA", storedEntities.get(1).getTenantId());
        Assertions.assertEquals(777, storedEntities.get(1).getTestIntegerValue());
    }

    @Test
    void testLoadAppSettingsEntityIgnoringSoftDeletion() {
        TestAppSettingsEntity settingsEntity = appSettings.load(TestAppSettingsEntity.class);
        settingsEntity.setTestIntegerValue(410);
        settingsEntity.setTestStringValue("stored");
        appSettings.save(settingsEntity);

        TestAppSettingsEntity storedEntity = loadStoredTestAppSettingsEntity();
        dataManager.remove(storedEntity);

        TestAppSettingsEntity regularLoad = appSettings.load(TestAppSettingsEntity.class);
        Assertions.assertEquals(1, regularLoad.getId());
        Assertions.assertEquals(123, regularLoad.getTestIntegerValue());
        Assertions.assertEquals("defVal", regularLoad.getTestStringValue());

        TestAppSettingsEntity loadIgnoringSoftDeletion =
                appSettingsTools.loadAppSettingsEntityFromDataStore(
                        TestAppSettingsEntity.class,
                        AppSettingsEntityLoadMode.FOR_READ,
                        false
                );
        Assertions.assertEquals(1, loadIgnoringSoftDeletion.getId());
        Assertions.assertEquals(410, loadIgnoringSoftDeletion.getTestIntegerValue());
        Assertions.assertEquals("stored", loadIgnoringSoftDeletion.getTestStringValue());
        Assertions.assertNotNull(loadIgnoringSoftDeletion.getDeletedDate());
    }

    @Test
    void testTenantSettingsSaveRestoresSoftDeletedRecord() {
        withTenant("tenantA", () -> {
            TestAppSettingsEntity tenantSettings = appSettings.load(TestAppSettingsEntity.class);
            tenantSettings.setTestIntegerValue(777);
            tenantSettings.setTestStringValue("tenantA");
            appSettings.save(tenantSettings);
            return null;
        });

        TestAppSettingsEntity storedTenantEntity = loadStoredTestAppSettingsEntity("tenantA");
        dataManager.remove(storedTenantEntity);

        withTenant("tenantA", () -> {
            TestAppSettingsEntity tenantSettings = appSettings.load(TestAppSettingsEntity.class);
            Assertions.assertEquals(123, tenantSettings.getTestIntegerValue());
            Assertions.assertEquals("defVal", tenantSettings.getTestStringValue());

            tenantSettings.setTestIntegerValue(888);
            tenantSettings.setTestStringValue("tenantA-restored");
            appSettings.save(tenantSettings);
            return null;
        });

        List<TestAppSettingsEntity> storedEntities = dataManager.load(TestAppSettingsEntity.class)
                .all()
                .hint(PersistenceHints.SOFT_DELETION, false)
                .list();
        Assertions.assertEquals(1, storedEntities.size());

        TestAppSettingsEntity restoredTenantEntity = storedEntities.get(0);
        Assertions.assertEquals(storedTenantEntity.getId(), restoredTenantEntity.getId());
        Assertions.assertEquals("tenantA", restoredTenantEntity.getTenantId());
        Assertions.assertEquals(888, restoredTenantEntity.getTestIntegerValue());
        Assertions.assertEquals("tenantA-restored", restoredTenantEntity.getTestStringValue());
        Assertions.assertNull(restoredTenantEntity.getDeletedDate());
    }

    private TestAppSettingsEntity loadStoredTestAppSettingsEntity() {
        return dataManager.load(TestAppSettingsEntity.class)
                .query("select e from testAppSettingsEntity e where e.id = 1 and e.tenantId is null")
                .one();
    }

    private TestAppSettingsEntity loadStoredTestAppSettingsEntity(String tenantId) {
        return dataManager.load(TestAppSettingsEntity.class)
                .query("select e from testAppSettingsEntity e where e.tenantId = :tenantId")
                .parameter("tenantId", tenantId)
                .one();
    }

    private <T> T withTenant(String tenantId, Supplier<T> action) {
        TenantTestUser user = metadata.create(TenantTestUser.class);
        user.setUsername(tenantId);
        user.setPassword("{noop}" + tenantId);
        user.setTenantId(tenantId);
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
