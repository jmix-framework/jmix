package test_support.entity.lazyloading.nullability;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.impl.lazyloading.NotInstantiatedList;
import io.jmix.data.impl.lazyloading.NotInstantiatedSet;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@JmixEntity
@Table(name = "PARENT_ENTITY")
@Entity
public class ParentEntity {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @Composition
    @OneToMany(mappedBy = "parentEntity")
    // field initializer that doesn't provoke eager loading of collection without correct ref fields lazy loading initialization
    private List<ChildEntity> children = new NotInstantiatedList<>();

    @NotNull
    @JoinTable(name = "PARENT_ADDITIONAL_ENTITY_LINK",
            joinColumns = @JoinColumn(name = "PARENT_ENTITY_ID"),
            inverseJoinColumns = @JoinColumn(name = "ADDITIONAL_ENTITY_ID"))
    @ManyToMany
    // field initializer that doesn't provoke eager loading of collection without correct ref fields lazy loading initialization
    private Set<AdditionalEntity> relatedAdditionalEntities = new NotInstantiatedSet<>();

    public Set<AdditionalEntity> getRelatedAdditionalEntities() {
        return relatedAdditionalEntities;
    }

    public void setRelatedAdditionalEntities(Set<AdditionalEntity> relatedAdditionalEntities) {
        this.relatedAdditionalEntities = relatedAdditionalEntities;
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

    public List<ChildEntity> getChildren() {
        return children;
    }

    public void setChildren(List<ChildEntity> children) {
        this.children = children;
    }
}