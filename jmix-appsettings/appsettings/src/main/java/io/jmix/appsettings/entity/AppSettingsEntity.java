package io.jmix.appsettings.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

/**
 * Base class for entities that present application settings.
 */
@JmixEntity
@MappedSuperclass
public class AppSettingsEntity {

    @Id
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    /**
     * Only one record of each application settings can exist
     */
    public AppSettingsEntity() {
        this.id = 1;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

}