package test_support.app.entity.fetch_plans;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;


@Entity(name = "app_ParentTestEntity")
@JmixEntity
public class ParentTestEntity {
    @Id
    @Column(name = "UUID")
    @JmixGeneratedValue
    private UUID uuid;

    @Column(name = "NAME")
    protected String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FIRSTBORN_ID")
    protected ChildTestEntity firstborn;

    @OneToMany(mappedBy = "parent")
    protected List<ChildTestEntity> youngerChildren;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ChildTestEntity getFirstborn() {
        return firstborn;
    }

    public void setFirstborn(ChildTestEntity firstborn) {
        this.firstborn = firstborn;
    }

    public List<ChildTestEntity> getYoungerChildren() {
        return youngerChildren;
    }

    public void setYoungerChildren(List<ChildTestEntity> youngerChildren) {
        this.youngerChildren = youngerChildren;
    }
}
