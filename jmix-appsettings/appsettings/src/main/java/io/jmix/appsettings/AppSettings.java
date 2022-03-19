package io.jmix.appsettings;

import io.jmix.appsettings.entity.AppSettingsEntity;

public interface AppSettings {

    /**
     * Loads application settings entity of type {@code T}.
     * Note, that in default implementation attributes with empty values will be replaced with default values if specified.
     *
     * @param clazz class of entity that extends {@link AppSettingsEntity}
     */
    <T extends AppSettingsEntity> T load(Class<T> clazz);

    /**
     * Saves application settings entity of type {@code T}.
     * Note, that in default implementation attributes with values equal to default will be overridden with null value.
     *
     * @param settingsEntityToSave entity to be saved
     */
    <T extends AppSettingsEntity> void save(T settingsEntityToSave);

}
