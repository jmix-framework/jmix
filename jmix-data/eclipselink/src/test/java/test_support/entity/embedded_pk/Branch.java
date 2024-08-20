package test_support.entity.embedded_pk;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

@JmixEntity
@Table(name = "TST_BRANCH", indexes = {
        @Index(name = "IDX_BRANCH_ROCOROCO", columnList = "ROOT_CODE1, ROOT_CODE2")
})
@Entity
public class Branch {
    @EmbeddedId
    private MyKey2 id;

    @InstanceName
    @Column(name = "NAME")
    private String name;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @OnDelete(DeletePolicy.UNLINK)
    @JoinColumns({
            @JoinColumn(name = "ROOT_CODE1", referencedColumnName = "CODE1", nullable = false),
            @JoinColumn(name = "ROOT_CODE2", referencedColumnName = "CODE2", nullable = false)
    })
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Root root;


    @JoinColumns({
            @JoinColumn(name = "O2O_ROOT_CODE1", referencedColumnName = "CODE1"),
            @JoinColumn(name = "O2O_ROOT_CODE2", referencedColumnName = "CODE2")
    })
    @OneToOne(fetch = FetchType.LAZY)
    private Root o2oRoot;
    public Root getO2oRoot() {
        return o2oRoot;
    }

    public void setO2oRoot(Root o2oRoot) {
        this.o2oRoot = o2oRoot;
    }

    public Root getRoot() {
        return root;
    }

    public void setRoot(Root root) {
        this.root = root;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MyKey2 getId() {
        return id;
    }

    public void setId(MyKey2 id) {
        this.id = id;
    }
}