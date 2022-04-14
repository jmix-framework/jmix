package test_support.entity.lazyloading.nullability;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "ADDITIONAL_ENTITY")
@Entity
public class AdditionalEntity {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "NAME")
    private String name;
    @JoinTable(name = "PARENT_ADDITIONAL_ENTITY_LINK",
            joinColumns = @JoinColumn(name = "ADDITIONAL_ENTITY_ID"),
            inverseJoinColumns = @JoinColumn(name = "PARENT_ENTITY_ID"))
    @ManyToMany
    private List<ParentEntity> parentEntities;


    public List<ParentEntity> getParentEntities() {
        return parentEntities;
    }

    public void setParentEntities(List<ParentEntity> parentEntities) {
        this.parentEntities = parentEntities;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
