package io.jmix.appsettings;

import io.jmix.appsettings.entity.AppSettingsEntity;
import org.jspecify.annotations.Nullable;

import java.util.List;

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
