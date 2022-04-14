package test_support.entity.lazyloading.nullability;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.util.UUID;

@JmixEntity
@Table(name = "CHILD_ENTITY")
@Entity
public class ChildEntity {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "CHILDNAME")
    private String childname;

    @JoinColumn(name = "PARENT_ENTITY_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ParentEntity parentEntity;

    @JoinColumn(name = "ADDITIONAL_ENTITY_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private AdditionalEntity additionalEntity;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getChildname() {
        return childname;
    }

    public void setChildname(String childname) {
        this.childname = childname;
    }

    public ParentEntity getParentEntity() {
        return parentEntity;
    }

    public void setParentEntity(ParentEntity parentEntity) {
        this.parentEntity = parentEntity;
    }

    public AdditionalEntity getAdditionalEntity() {
        return additionalEntity;
    }

    public void setAdditionalEntity(AdditionalEntity additionalEntity) {
        this.additionalEntity = additionalEntity;
    }
}