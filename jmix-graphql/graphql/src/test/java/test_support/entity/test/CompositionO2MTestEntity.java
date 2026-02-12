package test_support.entity.test;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.PositiveOrZero;
import java.util.UUID;

@Table(name = "SCR_COMPOSITION_O2M_TEST_ENTITY")
@JmixEntity
@Entity(name = "scr_CompositionO2MTestEntity")
public class CompositionO2MTestEntity {
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DATATYPES_TEST_ENTITY_ID")
    protected DatatypesTestEntity datatypesTestEntity;

    @PositiveOrZero
    @Column(name = "QUANTITY")
    private Integer quantity;

    @InstanceName
    @Column(name = "NAME")
    protected String name;

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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
