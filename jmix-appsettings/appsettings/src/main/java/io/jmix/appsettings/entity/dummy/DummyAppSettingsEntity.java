package io.jmix.appsettings.entity.dummy;

import io.jmix.appsettings.entity.AppSettingsEntity;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.Entity;

// TODO: remove after
//  https://github.com/Haulmont/jmix-data/issues/115
//  https://github.com/Haulmont/jmix-gradle-plugin/issues/18
@JmixEntity
@Entity(name = "dummyAppSettingsEntity")
@SystemLevel
public class DummyAppSettingsEntity extends AppSettingsEntity {
}
