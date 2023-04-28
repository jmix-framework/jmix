package test_support.app.entity.fetch_plans;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

@JmixEntity
@Entity(name = "app_ChildTestEntity")
public class ChildTestEntity {

    @Id
    @Column(name = "UUID")
    @JmixGeneratedValue
    private UUID uuid;

    @ManyToOne
    @Column(name = "PARENT_ID")
    protected ParentTestEntity parent;

    @Column(name = "BIRTH_DATE")
    protected Date birthDate;

    @Column(name = "NAME")
    protected String name;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }


    public ParentTestEntity getParent() {
        return parent;
    }

    public void setParent(ParentTestEntity parent) {
        this.parent = parent;
    }


    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
