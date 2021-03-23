package test_support.entity.test;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.util.UUID;

@Table(name = "SCR_ASSOCIATION_O2O_TEST_ENTITY")
@JmixEntity
@Entity(name = "scr_AssociationO2OTestEntity")
public class AssociationO2OTestEntity {
    @JmixGeneratedValue
    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "associationO2Oattr")
    protected DatatypesTestEntity datatypesTestEntity;

    @InstanceName
    @Column(name = "NAME")
    protected String name;
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "associationO2Oattr")
    protected DeeplyNestedTestEntity deeplyNestedTestEntity;

    public DeeplyNestedTestEntity getDeeplyNestedTestEntity() {
        return deeplyNestedTestEntity;
    }

    public void setDeeplyNestedTestEntity(DeeplyNestedTestEntity deeplyNestedTestEntity) {
        this.deeplyNestedTestEntity = deeplyNestedTestEntity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DatatypesTestEntity getDatatypesTestEntity() {
        return datatypesTestEntity;
    }

    public void setDatatypesTestEntity(DatatypesTestEntity datatypesTestEntity) {
        this.datatypesTestEntity = datatypesTestEntity;
    }
}
