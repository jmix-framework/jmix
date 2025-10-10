package test_support.entity.lazyloading.instantiated_vh_wrapping;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

import java.util.Collection;
import java.util.UUID;

@JmixEntity
@Entity(name = "ivw_First")
@Table(name = "IVW_FIRST")
public class First {
    @Id
    @Column(name = "ID", nullable = false)
    @JmixGeneratedValue
    private UUID id;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @Column(name = "User", nullable = false)
    private String name;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "first")
    private Second second;


    public final UUID getId() {
        return this.id;
    }

    public final void setId(UUID uuid) {
        this.id = uuid;
    }


    public final Integer getVersion() {
        return this.version;
    }

    public final void setVersion(Integer version) {
        this.version = version;
    }


    public final String getName() {
        return this.name;
    }

    public final void setName(String name) {
        this.name = name;
    }


    public final Second getSecond() {
        return this.second;
    }

    public final void setSecond(Second second) {
        this.second = second;
    }
}
