package test_support.entity.test;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Table(name = "SCR_ASSOCIATION_M2M_TEST_ENTITY")
@JmixEntity
@Entity(name = "scr_AssociationM2MTestEntity")
public class AssociationM2MTestEntity {

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


    @JoinTable(name = "SCR_DATATYPES_TEST_ENTITY_ASSOCIATION_M2M_TEST_ENTITY_LINK",
        joinColumns = @JoinColumn(name = "ASSOCIATION_M2_M_TEST_ENTITY_ID"),
        inverseJoinColumns = @JoinColumn(name = "DATATYPES_TEST_ENTITY_ID"))
    @ManyToMany
    protected List<DatatypesTestEntity> datatypesTestEntities;

    @InstanceName
    @Column(name = "NAME")
    protected String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DatatypesTestEntity> getDatatypesTestEntities() {
        return datatypesTestEntities;
    }

    public void setDatatypesTestEntities(List<DatatypesTestEntity> datatypesTestEntities) {
        this.datatypesTestEntities = datatypesTestEntities;
    }
}
