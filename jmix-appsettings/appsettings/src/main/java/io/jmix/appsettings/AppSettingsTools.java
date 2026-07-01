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

import io.jmix.appsettings.entity.AppSettingsEntity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public interface AppSettingsTools {

    /**
     * Loads application settings entity by its {@code clazz} from data store for reading.
     *
     * @param clazz class that extends {@link AppSettingsEntity}
     * @return application settings entity
     */
    default <T extends AppSettingsEntity> T loadAppSettingsEntityFromDataStore(Class<T> clazz) {
        return loadAppSettingsEntityFromDataStore(clazz, AppSettingsEntityLoadMode.FOR_READ, true);
    }

    /**
     * Loads application settings entity by its {@code clazz} from data store for reading.
     *
     * @param clazz          class that extends {@link AppSettingsEntity}
     * @param softDeletion   whether soft-deleted entities should be filtered out
     * @return application settings entity
     */
    default <T extends AppSettingsEntity> T loadAppSettingsEntityFromDataStore(Class<T> clazz, boolean softDeletion) {
        return loadAppSettingsEntityFromDataStore(clazz, AppSettingsEntityLoadMode.FOR_READ, softDeletion);
    }

    /**
     * Loads application settings entity by its {@code clazz} from data store according to provided {@link AppSettingsEntityLoadMode}.
     *
     * @param clazz class that extends {@link AppSettingsEntity}
     * @param mode  load mode
     * @return application settings entity
     */
    default <T extends AppSettingsEntity> T loadAppSettingsEntityFromDataStore(Class<T> clazz,
                                                                               AppSettingsEntityLoadMode mode) {
        return loadAppSettingsEntityFromDataStore(clazz, mode, true);
    }

    /**
     * Loads application settings entity by its {@code clazz} from data store according to provided
     * {@link AppSettingsEntityLoadMode} and soft deletion hint.
     *
     * @param clazz         class that extends {@link AppSettingsEntity}
     * @param mode          load mode
     * @param softDeletion  whether soft-deleted entities should be filtered out
     * @return application settings entity
     */
    <T extends AppSettingsEntity> T loadAppSettingsEntityFromDataStore(Class<T> clazz,
                                                                       AppSettingsEntityLoadMode mode,
                                                                       boolean softDeletion);

    /**
     * Returns actual value for provided {@code propertyName} and {@code clazz}.
     *
     * @param clazz        class that extends {@link AppSettingsEntity}
     * @param propertyName property name for which value should be calculated
     * @return actual value of provided {@code propertyName}
     */
    @Nullable
    Object getPropertyValue(Class<? extends AppSettingsEntity> clazz, String propertyName);

    /**
     * Returns default value for provided {@code propertyName} and {@code clazz} based on value of one of the AppSettingsDefault annotation.
     *
     * @param clazz        class that extends {@link AppSettingsEntity}
     * @param propertyName property name for which default value should be calculated
     * @return value by default of provided {@code propertyName}
     */
    @Nullable
    Object getDefaultPropertyValue(Class<? extends AppSettingsEntity> clazz, String propertyName);

    /**
     * Returns all non-system properties' names for given {@code clazz}.
     *
     * @param clazz class that extends {@link AppSettingsEntity}
     * @return non-system properties' names for given {@code clazz}
     */
    <T extends AppSettingsEntity> List<String> getPropertyNames(Class<T> clazz);
}
