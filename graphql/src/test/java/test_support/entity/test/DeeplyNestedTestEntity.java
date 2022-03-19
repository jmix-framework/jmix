package test_support.entity.test;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import java.util.UUID;

@Table(name = "SCR_DEEPLY_NESTED_TEST_ENTITY")
@JmixEntity
@Entity(name = "scr_DeeplyNestedTestEntity")
public class DeeplyNestedTestEntity {
    @JmixGeneratedValue
    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    @Length(message = "{msg://test_support.entity.test/DeeplyNestedTestEntity.name.validation.Length}", min = 6)
    @InstanceName
    @Column(name = "NAME")
    protected String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ASSOCIATION_O2_OATTR_ID")
    protected AssociationO2OTestEntity associationO2Oattr;

    @Composition
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "nestedComposition")
    private CompositionO2OTestEntity parentO2Ocomposition;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public CompositionO2OTestEntity getParentO2Ocomposition() {
        return parentO2Ocomposition;
    }

    public void setParentO2Ocomposition(CompositionO2OTestEntity ff) {
        this.parentO2Ocomposition = ff;
    }

    public AssociationO2OTestEntity getAssociationO2Oattr() {
        return associationO2Oattr;
    }

    public void setAssociationO2Oattr(AssociationO2OTestEntity associationO2Oattr) {
        this.associationO2Oattr = associationO2Oattr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
