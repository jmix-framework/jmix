package test_support.app.entity.jmix_entities;

import io.jmix.core.entity.annotation.JmixGeneratedValue;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;


@Entity(name = "test_entityNotJmix")
@Table(name = "TEST_ENTITY_NOT_JMIX")
public class EntityWithoutJmix {
    @Id
    @Column(name = "UUID")
    @JmixGeneratedValue
    private UUID uuid;

    @Column(name = "NAME")
    protected String name;

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
}
