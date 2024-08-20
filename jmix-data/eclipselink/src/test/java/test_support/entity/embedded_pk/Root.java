package test_support.entity.embedded_pk;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.util.List;

@JmixEntity
@Table(name = "TST_ROOT")
@Entity
public class Root {
    @EmbeddedId
    private MyKey id;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "o2oRoot")
    private Branch o2oBranch;

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @OneToMany(mappedBy = "root")
    private List<Branch> branches;

    public Branch getO2oBranch() {
        return o2oBranch;
    }

    public void setO2oBranch(Branch o2oBranch) {
        this.o2oBranch = o2oBranch;
    }

    public List<Branch> getBranches() {
        return branches;
    }

    public void setBranches(List<Branch> branches) {
        this.branches = branches;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MyKey getId() {
        return id;
    }

    public void setId(MyKey id) {
        this.id = id;
    }
}