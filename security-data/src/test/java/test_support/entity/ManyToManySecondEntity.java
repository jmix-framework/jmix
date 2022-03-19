package test_support.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.util.List;

@Table(name = "TEST_MANY_TO_MANY_SECOND_ENTITY")
@Entity(name = "test_ManyToManySecondEntity")
@JmixEntity
public class ManyToManySecondEntity extends BaseEntity {
    @Column(name = "NAME")
    protected String name;

    @JoinTable(name = "TEST_MANY_TO_MANY_FIRST_ENTITY_MANY_TO_MANY_SECOND_ENTITY_LINK",
            joinColumns = @JoinColumn(name = "MANY_TO_MANY_SECOND_ENTITY_ID"),
            inverseJoinColumns = @JoinColumn(name = "MANY_TO_MANY_FIRST_ENTITY_ID"))
    @ManyToMany(fetch = FetchType.LAZY)
    protected List<ManyToManyFirstEntity> manyToManyFirstEntities;

    public List<ManyToManyFirstEntity> getManyToManyFirstEntities() {
        return manyToManyFirstEntities;
    }

    public void setManyToManyFirstEntities(List<ManyToManyFirstEntity> manyToManyFirstEntities) {
        this.manyToManyFirstEntities = manyToManyFirstEntities;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}