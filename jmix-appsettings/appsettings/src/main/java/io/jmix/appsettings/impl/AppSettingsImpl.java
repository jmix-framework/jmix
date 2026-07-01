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

package io.jmix.appsettings.impl;

import io.jmix.appsettings.AppSettings;
import io.jmix.appsettings.AppSettingsEntityLoadMode;
import io.jmix.appsettings.AppSettingsProperties;
import io.jmix.appsettings.AppSettingsTools;
import io.jmix.appsettings.entity.AppSettingsEntity;
import io.jmix.core.DataManager;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@NullMarked
@Component("appset_AppSettings")
public class AppSettingsImpl implements AppSettings {

    private static final Logger log = LoggerFactory.getLogger(AppSettingsImpl.class);

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected UnconstrainedDataManager unconstrainedDataManager;

    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    @Autowired
    protected AppSettingsTools appSettingsTools;

    @Autowired
    protected AppSettingsProperties appSettingsProperties;

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

        T settingsEntity = appSettingsTools.loadAppSettingsEntityFromDataStore(clazz,
                AppSettingsEntityLoadMode.FOR_SAVE);

        updatePropertyValues(settingsEntityToSave, settingsEntity, getPropertyNames(clazz));

        saveAppSettingsEntity(settingsEntity);
    }

    protected <T extends AppSettingsEntity> T getAppSettingsEntity(Class<T> clazz) {
        return appSettingsTools.loadAppSettingsEntityFromDataStore(clazz, AppSettingsEntityLoadMode.FOR_READ);
    }

    protected <T extends AppSettingsEntity> void saveAppSettingsEntity(T settingsEntity) {
        getDataManagerForAppSettingsEntity().save(settingsEntity);
    }

    protected UnconstrainedDataManager getDataManagerForAppSettingsEntity() {
        return appSettingsProperties.isCheckPermissionsForAppSettingsEntity() ? dataManager : unconstrainedDataManager;
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
     *
     * @param srcSettingsEntity provided entity to save
     * @param dstSettingsEntity entity to be updated and actually saved
     * @param propertyNames     all non-system properties of {@code T}
     */
    protected <T extends AppSettingsEntity> void updatePropertyValues(T srcSettingsEntity, T dstSettingsEntity, List<String> propertyNames) {
        for (String propertyName : propertyNames) {
            Object propertyValue = EntityValues.getValue(srcSettingsEntity, propertyName);
            EntityValues.setValue(dstSettingsEntity, propertyName, propertyValue);
        }
    }

}
