package io.jmix.appsettings.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

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