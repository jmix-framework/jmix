package test_support.entity.test;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import javax.validation.constraints.PositiveOrZero;
import java.util.UUID;

@Table(name = "SCR_COMPOSITION_O2O_TEST_ENTITY")
@JmixEntity
@Entity(name = "scr_CompositionO2OTestEntity")
public class CompositionO2OTestEntity {

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
    @InstanceName
    @Column(name = "NAME")
    protected String name;

    @PositiveOrZero
    @Column(name = "QUANTITY")
    private Integer quantity;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NESTED_COMPOSITION_ID")
    protected DeeplyNestedTestEntity nestedComposition;

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public DeeplyNestedTestEntity getNestedComposition() {
        return nestedComposition;
    }

    public void setNestedComposition(DeeplyNestedTestEntity nestedComposition) {
        this.nestedComposition = nestedComposition;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
