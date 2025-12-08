package test_support.entity.lazyloading.instantiated_vh_wrapping;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@JmixEntity
@Table(name = "IVW_SECOND")
@Entity(name = "ivw_Second")
public class Second {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @JoinColumn(name = "FIRST_ID", nullable = false)
    @NotNull
    @OneToOne(fetch = FetchType.LAZY,optional = false)
    private First first;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "second")
    private Third third;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "secondForCollection")
    private List<Third> thirds;

    
    public final UUID getId() {
        return this.id;
    }

    public final void setId( UUID uuid) {
        this.id = uuid;
    }

    
    public final First getFirst() {
        return this.first;
    }

    public final void setFirst(First first) {
        this.first = first;
    }

    
    public final Third getThird() {
        return this.third;
    }

    public final void setThird(Third third) {
        this.third = third;
    }

    public List<Third> getThirds() {
        return thirds;
    }

    public void setThirds(List<Third> thirds) {
        this.thirds = thirds;
    }
}
