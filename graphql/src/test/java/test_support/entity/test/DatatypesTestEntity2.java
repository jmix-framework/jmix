package test_support.entity.test;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.util.UUID;

@Table(name = "SCR_DATATYPES_TEST_ENTITY2")
@JmixEntity
@Entity(name = "scr_DatatypesTestEntity2")
public class DatatypesTestEntity2 {
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
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DATATYPES_TEST_ENTITY_ATTR_ID")
    protected DatatypesTestEntity datatypesTestEntityAttr;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INT_IDENTITY_ID_TEST_ENTITY_ATTR_ID")
    private IntIdentityIdTestEntity intIdentityIdTestEntityAttr;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INTEGER_ID_TEST_ENTITY_ATTR_ID")
    private IntegerIdTestEntity integerIdTestEntityAttr;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STRING_ID_TEST_ENTITY_ATTR_IDENTIFIER")
    private StringIdTestEntity stringIdTestEntityAttr;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WEIRD_STRING_ID_TEST_ENTITY_ATTR_IDENTIFIER")
    private WeirdStringIdTestEntity weirdStringIdTestEntityAttr;

    public WeirdStringIdTestEntity getWeirdStringIdTestEntityAttr() {
        return weirdStringIdTestEntityAttr;
    }

    public void setWeirdStringIdTestEntityAttr(WeirdStringIdTestEntity weirdStringIdTestEntityAttr) {
        this.weirdStringIdTestEntityAttr = weirdStringIdTestEntityAttr;
    }

    public StringIdTestEntity getStringIdTestEntityAttr() {
        return stringIdTestEntityAttr;
    }

    public void setStringIdTestEntityAttr(StringIdTestEntity stringIdTestEntityAttr) {
        this.stringIdTestEntityAttr = stringIdTestEntityAttr;
    }

    public IntegerIdTestEntity getIntegerIdTestEntityAttr() {
        return integerIdTestEntityAttr;
    }

    public void setIntegerIdTestEntityAttr(IntegerIdTestEntity integerIdTestEntityAttr) {
        this.integerIdTestEntityAttr = integerIdTestEntityAttr;
    }

    public IntIdentityIdTestEntity getIntIdentityIdTestEntityAttr() {
        return intIdentityIdTestEntityAttr;
    }

    public void setIntIdentityIdTestEntityAttr(IntIdentityIdTestEntity intIdentityIdTestEntityAttr) {
        this.intIdentityIdTestEntityAttr = intIdentityIdTestEntityAttr;
    }

    public DatatypesTestEntity getDatatypesTestEntityAttr() {
        return datatypesTestEntityAttr;
    }

    public void setDatatypesTestEntityAttr(DatatypesTestEntity datatypesTestEntityAttr) {
        this.datatypesTestEntityAttr = datatypesTestEntityAttr;
    }
}
