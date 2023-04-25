package test_support.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.*;
import java.util.List;

@Table(name = "TEST_MANY_TO_MANY_FIRST_ENTITY")
@Entity(name = "test_ManyToManyFirstEntity")
@JmixEntity
public class ManyToManyFirstEntity extends BaseEntity {
    @Column(name = "NAME")
    protected String name;

    @JoinTable(name = "TEST_MANY_TO_MANY_FIRST_ENTITY_MANY_TO_MANY_SECOND_ENTITY_LINK",
            joinColumns = @JoinColumn(name = "MANY_TO_MANY_FIRST_ENTITY_ID"),
            inverseJoinColumns = @JoinColumn(name = "MANY_TO_MANY_SECOND_ENTITY_ID"))
    @ManyToMany(fetch = FetchType.LAZY)
    protected List<ManyToManySecondEntity> manyToManySecondEntities;

    public List<ManyToManySecondEntity> getManyToManySecondEntities() {
        return manyToManySecondEntities;
    }

    public void setManyToManySecondEntities(List<ManyToManySecondEntity> manyToManySecondEntities) {
        this.manyToManySecondEntities = manyToManySecondEntities;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}