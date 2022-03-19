package test_support.app.entity.jmix_entities;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

@JmixEntity(name = "test_MappedWithJmix", annotatedPropertiesOnly = true)
@MappedSuperclass
public class MappedWithJmix {

    @Id
    @Column(name = "UUID")
    @JmixGeneratedValue
    @JmixProperty
    private UUID uuid;

    @Column(name = "DATA")
    protected String data;

    @Column(name = "NAME")
    @JmixProperty
    protected String name;

    @JmixProperty
    protected String qualifier;


    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQualifier() {
        return qualifier;
    }
}
