package test_support.entity.test;


import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Table(name = "SCR_DATATYPES_TEST_ENTITY3")
@JmixEntity
@Entity(name = "scr_DatatypesTestEntity3")
public class DatatypesTestEntity3 {
    @JmixGeneratedValue
    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "datatypesTestEntity3")
    protected List<DatatypesTestEntity> datatypesTestEntityAttr;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "datatypesTestEntity3")
    private List<IntegerIdTestEntity> integerIdTestEntityAttr;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "datatypesTestEntity3")
    private List<IntIdentityIdTestEntity> intIdentityIdTestEntityAttr;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "datatypesTestEntity3")
    private List<StringIdTestEntity> stringIdTestEntityAttr;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "datatypesTestEntity3")
    private List<WeirdStringIdTestEntity> weirdStringIdTestEntityAttr;

    @InstanceName
    @Column(name = "NAME")
    protected String name;

    public List<WeirdStringIdTestEntity> getWeirdStringIdTestEntityAttr() {
        return weirdStringIdTestEntityAttr;
    }

    public void setWeirdStringIdTestEntityAttr(List<WeirdStringIdTestEntity> weirdStringIdTestEntityAttr) {
        this.weirdStringIdTestEntityAttr = weirdStringIdTestEntityAttr;
    }

    public List<StringIdTestEntity> getStringIdTestEntityAttr() {
        return stringIdTestEntityAttr;
    }

    public void setStringIdTestEntityAttr(List<StringIdTestEntity> stringIdTestEntityAttr) {
        this.stringIdTestEntityAttr = stringIdTestEntityAttr;
    }

    public List<IntIdentityIdTestEntity> getIntIdentityIdTestEntityAttr() {
        return intIdentityIdTestEntityAttr;
    }

    public void setIntIdentityIdTestEntityAttr(List<IntIdentityIdTestEntity> intIdentityIdTestEntityAttr) {
        this.intIdentityIdTestEntityAttr = intIdentityIdTestEntityAttr;
    }

    public List<IntegerIdTestEntity> getIntegerIdTestEntityAttr() {
        return integerIdTestEntityAttr;
    }

    public void setIntegerIdTestEntityAttr(List<IntegerIdTestEntity> integerIdTestEntityAttr) {
        this.integerIdTestEntityAttr = integerIdTestEntityAttr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DatatypesTestEntity> getDatatypesTestEntityAttr() {
        return datatypesTestEntityAttr;
    }

    public void setDatatypesTestEntityAttr(List<DatatypesTestEntity> datatypesTestEntityAttr) {
        this.datatypesTestEntityAttr = datatypesTestEntityAttr;
    }
}