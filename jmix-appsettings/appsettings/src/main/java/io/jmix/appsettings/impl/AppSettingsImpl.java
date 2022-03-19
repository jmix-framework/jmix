package io.jmix.appsettings.impl;

import io.jmix.appsettings.AppSettings;
import io.jmix.appsettings.AppSettingsTools;
import io.jmix.appsettings.entity.AppSettingsEntity;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component("appset_AppSettings")
public class AppSettingsImpl implements AppSettings {

    private static final Logger log = LoggerFactory.getLogger(AppSettingsImpl.class);

    @Autowired
    protected UnconstrainedDataManager dataManager;

    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    @Autowired
    protected AppSettingsTools appSettingsTools;

    @Override
    public <T extends AppSettingsEntity> T load(Class<T> clazz) {
        log.debug("load application settings entity by class [{}]", clazz);

        T settingsEntity = getAppSettingsEntity(clazz);

        setDefaultValuesForMissingProperties(settingsEntity, getPropertyNames(clazz));

        return settingsEntity;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends AppSettingsEntity> void save(T settingsEntityToSave) {
        log.debug("save application settings entity [{}]", settingsEntityToSave);
        Class<T> clazz = (Class<T>) settingsEntityToSave.getClass();

        T settingsEntity = getAppSettingsEntity(clazz);

        updatePropertyValues(settingsEntityToSave, settingsEntity, getPropertyNames(clazz));

        saveAppSettingsEntity(settingsEntity);
    }

    protected <T extends AppSettingsEntity> T getAppSettingsEntity(Class<T> clazz) {
        return appSettingsTools.loadAppSettingsEntityFromDataStore(clazz);
    }

    protected <T extends AppSettingsEntity> void saveAppSettingsEntity(T settingsEntity) {
        dataManager.save(settingsEntity);
    }

    protected <T extends AppSettingsEntity> List<String> getPropertyNames(Class<T> clazz) {
        return appSettingsTools.getPropertyNames(clazz);
    }

    /**
     * Enriches provided {@code settingsEntity} with setting up default values for properties with null values.
     *
     * @param settingsEntity entity to be enriched
     * @param propertyNames  all non-system properties of {@code T}
     */
    protected <T extends AppSettingsEntity> void setDefaultValuesForMissingProperties(T settingsEntity, List<String> propertyNames) {
        Class<? extends AppSettingsEntity> clazz = settingsEntity.getClass();
        for (String propertyName : propertyNames) {
            Object propertyValue = EntityValues.getValue(settingsEntity, propertyName);
            if (Objects.isNull(propertyValue)) {
                EntityValues.setValue(settingsEntity, propertyName, appSettingsTools.getDefaultPropertyValue(clazz, propertyName));
            }
        }
    }

    /**
     * Update all non-system properties of {@code dstSettingsEntity} based on provided {@code srcSettingsEntity}.
     * Note, that if value of some property in {@code srcSettingsEntity} are equal to default value it will be overridden
     * with null value in {@code dstSettingsEntity}.
     *
     * @param srcSettingsEntity provided entity to save
     * @param dstSettingsEntity entity to be updated and actually saved
     * @param propertyNames     all non-system properties of {@code T}
     */
    protected <T extends AppSettingsEntity> void updatePropertyValues(T srcSettingsEntity, T dstSettingsEntity, List<String> propertyNames) {
        Class<? extends AppSettingsEntity> clazz = srcSettingsEntity.getClass();
        for (String propertyName : propertyNames) {
            Object propertyValue = EntityValues.getValue(srcSettingsEntity, propertyName);
            Object defaultValue = appSettingsTools.getDefaultPropertyValue(clazz, propertyName);
            if (propertyValue != null && propertyValue.equals(defaultValue)) {
                propertyValue = null;
            }
            EntityValues.setValue(dstSettingsEntity, propertyName, propertyValue);
        }
    }

}
