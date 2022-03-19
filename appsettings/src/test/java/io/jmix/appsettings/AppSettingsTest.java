package io.jmix.appsettings;


import io.jmix.appsettings.test_entity.TestAppSettingsEntity;
import io.jmix.core.UnconstrainedDataManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = AppSettingsTestConfiguration.class)
class AppSettingsTest {

    @Autowired
    private AppSettings appSettings;

    @Autowired
    private UnconstrainedDataManager dataManager;

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


        //ensure non-null saved values override defaults and default values are returned for null values
        testAppSettingsEntity.setTestIntegerValue(410);
        testAppSettingsEntity.setTestStringValue("defValChanged");
        appSettings.save(testAppSettingsEntity);
        testAppSettingsEntity = appSettings.load(TestAppSettingsEntity.class);
        Assertions.assertEquals(1, testAppSettingsEntity.getId());
        Assertions.assertTrue(testAppSettingsEntity.getTestBooleanValue());
        Assertions.assertEquals(410, testAppSettingsEntity.getTestIntegerValue());
        Assertions.assertEquals(100500L, testAppSettingsEntity.getTestLongValue());
        Assertions.assertEquals(3.1415926535, testAppSettingsEntity.getTestDoubleValue());
        Assertions.assertEquals("defValChanged", testAppSettingsEntity.getTestStringValue());

        testAppSettingsEntity.setTestBooleanValue(false);
        testAppSettingsEntity.setTestLongValue(500100L);
        testAppSettingsEntity.setTestDoubleValue(2.7182818284);
        testAppSettingsEntity.setTestStringValue("access denied");
        appSettings.save(testAppSettingsEntity);
        testAppSettingsEntity = appSettings.load(TestAppSettingsEntity.class);
        Assertions.assertEquals(1, testAppSettingsEntity.getId());
        Assertions.assertFalse(testAppSettingsEntity.getTestBooleanValue());
        Assertions.assertEquals(410, testAppSettingsEntity.getTestIntegerValue());
        Assertions.assertEquals(500100L, testAppSettingsEntity.getTestLongValue());
        Assertions.assertEquals(2.7182818284, testAppSettingsEntity.getTestDoubleValue());
        Assertions.assertEquals("access denied", testAppSettingsEntity.getTestStringValue());


        //ensure default values are returned for null values
        testAppSettingsEntity.setTestBooleanValue(null);
        testAppSettingsEntity.setTestIntegerValue(null);
        testAppSettingsEntity.setTestLongValue(null);
        testAppSettingsEntity.setTestDoubleValue(null);
        testAppSettingsEntity.setTestStringValue(null);
        appSettings.save(testAppSettingsEntity);
        testAppSettingsEntity = appSettings.load(TestAppSettingsEntity.class);
        Assertions.assertEquals(1, testAppSettingsEntity.getId());
        Assertions.assertTrue(testAppSettingsEntity.getTestBooleanValue());
        Assertions.assertEquals(123, testAppSettingsEntity.getTestIntegerValue());
        Assertions.assertEquals(100500L, testAppSettingsEntity.getTestLongValue());
        Assertions.assertEquals(3.1415926535, testAppSettingsEntity.getTestDoubleValue());
        Assertions.assertEquals("defVal", testAppSettingsEntity.getTestStringValue());

        //ensure that only one record can exist for each application settings
        TestAppSettingsEntity createdTestAppSettingsEntity = dataManager.create(TestAppSettingsEntity.class);
        createdTestAppSettingsEntity.setTestBooleanValue(true);
        createdTestAppSettingsEntity.setTestLongValue(333L);
        createdTestAppSettingsEntity.setTestDoubleValue(6.626);
        createdTestAppSettingsEntity.setTestStringValue("access granted");
        appSettings.save(createdTestAppSettingsEntity);
        TestAppSettingsEntity loadedTestAppSettingsEntity = appSettings.load(TestAppSettingsEntity.class);
        Assertions.assertEquals(1, loadedTestAppSettingsEntity.getId());
        Assertions.assertTrue(loadedTestAppSettingsEntity.getTestBooleanValue());
        Assertions.assertEquals(123, loadedTestAppSettingsEntity.getTestIntegerValue());
        Assertions.assertEquals(333L, loadedTestAppSettingsEntity.getTestLongValue());
        Assertions.assertEquals(6.626, loadedTestAppSettingsEntity.getTestDoubleValue());
        Assertions.assertEquals("access granted", loadedTestAppSettingsEntity.getTestStringValue());
    }

}
