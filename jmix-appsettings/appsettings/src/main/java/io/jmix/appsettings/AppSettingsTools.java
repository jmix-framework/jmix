package io.jmix.appsettings;

import io.jmix.appsettings.entity.AppSettingsEntity;

import jakarta.annotation.Nullable;
import java.util.List;

public interface AppSettingsTools {

    /**
     * Loads application settings entity by it {@code clazz} from data store.
     *
     * @param clazz class that extends {@link AppSettingsEntity}
     * @return application settings entity
     */
    <T extends AppSettingsEntity> T loadAppSettingsEntityFromDataStore(Class<T> clazz);

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
