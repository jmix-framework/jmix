package test_support.entity.test;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Table(name = "SCR_DATATYPES_TEST_ENTITY")
@JmixEntity
@Entity(name = "scr_DatatypesTestEntity")
public class DatatypesTestEntity {
    @JmixGeneratedValue
    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    @Column(name = "BIG_DECIMAL_ATTR")
    protected BigDecimal bigDecimalAttr;

    @Column(name = "BOOLEAN_ATTR")
    protected Boolean booleanAttr;

    @Temporal(TemporalType.DATE)
    @Column(name = "DATE_ATTR")
    protected Date dateAttr;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_TIME_ATTR")
    protected Date dateTimeAttr;

    @Column(name = "DOUBLE_ATTR")
    protected Double doubleAttr;

    @Column(name = "INTEGER_ATTR")
    protected Integer integerAttr;

    @Column(name = "LONG_ATTR")
    protected Long longAttr;

    @Column(name = "STRING_ATTR")
    protected String stringAttr;

    @Column(name = "CHAR_ATTR")
    private Character charAttr;

    @Column(name = "TIME_ATTR")
    @Temporal(TemporalType.TIME)
    private Date timeAttr;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    //
//    @Column(name = "BYTE_ARRAY_ATTR")
//    protected byte[] byteArrayAttr;

    @Column(name = "UUID_ATTR")
    protected UUID uuidAttr;

    @Column(name = "LOCAL_DATE_TIME_ATTR")
    protected LocalDateTime localDateTimeAttr;

    @Column(name = "OFFSET_DATE_TIME_ATTR")
    protected OffsetDateTime offsetDateTimeAttr;

    @Column(name = "LOCAL_DATE_ATTR")
    protected LocalDate localDateAttr;

    @Column(name = "LOCAL_TIME_ATTR")
    protected LocalTime localTimeAttr;

    @Column(name = "OFFSET_TIME_ATTR")
    protected OffsetTime offsetTimeAttr;

    @Column(name = "ENUM_ATTR")
    protected String enumAttr;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ASSOCIATION_O2_OATTR_ID")
    protected AssociationO2OTestEntity associationO2Oattr;

    @OneToMany(mappedBy = "datatypesTestEntity")
    protected List<AssociationO2MTestEntity> associationO2Mattr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ASSOCIATION_M2_OATTR_ID")
    protected AssociationM2OTestEntity associationM2Oattr;

    @JoinTable(name = "SCR_DATATYPES_TEST_ENTITY_ASSOCIATION_M2M_TEST_ENTITY_LINK",
            joinColumns = @JoinColumn(name = "DATATYPES_TEST_ENTITY_ID"),
            inverseJoinColumns = @JoinColumn(name = "ASSOCIATION_M2_M_TEST_ENTITY_ID"))
    @ManyToMany
    protected List<AssociationM2MTestEntity> associationM2Mattr;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPOSITION_O2_OATTR_ID")
    protected CompositionO2OTestEntity compositionO2Oattr;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToMany(mappedBy = "datatypesTestEntity")
    protected List<CompositionO2MTestEntity> compositionO2Mattr;

    @Column(name = "NAME")
    protected String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INT_IDENTITY_ID_TEST_ENTITY_ASSOCIATION_O2O_ATTR_ID")
    private IntIdentityIdTestEntity intIdentityIdTestEntityAssociationO2OAttr;

    @JoinTable(name = "SCR_DATATYPES_TEST_ENTITY_INTEGER_ID_TEST_ENTITY_LINK",
            joinColumns = @JoinColumn(name = "DATATYPES_TEST_ENTITY_ID"),
            inverseJoinColumns = @JoinColumn(name = "INTEGER_ID_TEST_ENTITY_ID"))
    @ManyToMany
    private List<IntegerIdTestEntity> integerIdTestEntityAssociationM2MAttr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DATATYPES_TEST_ENTITY3_ID")
    protected DatatypesTestEntity3 datatypesTestEntity3;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STRING_ID_TEST_ENTITY_ASSOCIATION_O2O_IDENTIFIER")
    private StringIdTestEntity stringIdTestEntityAssociationO2O;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STRING_ID_TEST_ENTITY_ASSOCIATION_M2O_ID")
    private StringIdTestEntity stringIdTestEntityAssociationM2O;

    @Column(name = "READ_ONLY_STRING_ATTR")
    protected String readOnlyStringAttr;

    public Character getCharAttr() {
        return charAttr;
    }

    public void setCharAttr(Character charAttr) {
        this.charAttr = charAttr;
    }

    public Date getTimeAttr() {
        return timeAttr;
    }

    public void setTimeAttr(Date timeAttr) {
        this.timeAttr = timeAttr;
    }

    public String getReadOnlyStringAttr() {
        return readOnlyStringAttr;
    }

    public StringIdTestEntity getStringIdTestEntityAssociationO2O() {
        return stringIdTestEntityAssociationO2O;
    }

    public void setStringIdTestEntityAssociationO2O(StringIdTestEntity stringIdTestEntityAssociationO2O) {
        this.stringIdTestEntityAssociationO2O = stringIdTestEntityAssociationO2O;
    }

    public StringIdTestEntity getStringIdTestEntityAssociationM2O() {
        return stringIdTestEntityAssociationM2O;
    }

    public void setStringIdTestEntityAssociationM2O(StringIdTestEntity stringIdTestEntityAssociationM2O) {
        this.stringIdTestEntityAssociationM2O = stringIdTestEntityAssociationM2O;
    }

    public List<IntegerIdTestEntity> getIntegerIdTestEntityAssociationM2MAttr() {
        return integerIdTestEntityAssociationM2MAttr;
    }

    public void setIntegerIdTestEntityAssociationM2MAttr(List<IntegerIdTestEntity> integerIdTestEntityAssociationM2MAttr) {
        this.integerIdTestEntityAssociationM2MAttr = integerIdTestEntityAssociationM2MAttr;
    }

    public IntIdentityIdTestEntity getIntIdentityIdTestEntityAssociationO2OAttr() {
        return intIdentityIdTestEntityAssociationO2OAttr;
    }

    public void setIntIdentityIdTestEntityAssociationO2OAttr(IntIdentityIdTestEntity intIdentityIdTestEntityAssociationO2OAttr) {
        this.intIdentityIdTestEntityAssociationO2OAttr = intIdentityIdTestEntityAssociationO2OAttr;
    }

    public DatatypesTestEntity3 getDatatypesTestEntity3() {
        return datatypesTestEntity3;
    }

    public void setDatatypesTestEntity3(DatatypesTestEntity3 datatypesTestEntity3) {
        this.datatypesTestEntity3 = datatypesTestEntity3;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CompositionO2MTestEntity> getCompositionO2Mattr() {
        return compositionO2Mattr;
    }

    public void setCompositionO2Mattr(List<CompositionO2MTestEntity> compositionO2Mattr) {
        this.compositionO2Mattr = compositionO2Mattr;
    }

    public CompositionO2OTestEntity getCompositionO2Oattr() {
        return compositionO2Oattr;
    }

    public void setCompositionO2Oattr(CompositionO2OTestEntity compositionO2Oattr) {
        this.compositionO2Oattr = compositionO2Oattr;
    }

    public List<AssociationM2MTestEntity> getAssociationM2Mattr() {
        return associationM2Mattr;
    }

    public void setAssociationM2Mattr(List<AssociationM2MTestEntity> associationM2Mattr) {
        this.associationM2Mattr = associationM2Mattr;
    }

    public AssociationM2OTestEntity getAssociationM2Oattr() {
        return associationM2Oattr;
    }

    public void setAssociationM2Oattr(AssociationM2OTestEntity associationM2Oattr) {
        this.associationM2Oattr = associationM2Oattr;
    }

    public List<AssociationO2MTestEntity> getAssociationO2Mattr() {
        return associationO2Mattr;
    }

    public void setAssociationO2Mattr(List<AssociationO2MTestEntity> associationO2Mattr) {
        this.associationO2Mattr = associationO2Mattr;
    }

    public AssociationO2OTestEntity getAssociationO2Oattr() {
        return associationO2Oattr;
    }

    public void setAssociationO2Oattr(AssociationO2OTestEntity associationO2Oattr) {
        this.associationO2Oattr = associationO2Oattr;
    }

    public void setEnumAttr(TestEnum enumAttr) {
        this.enumAttr = enumAttr == null ? null : enumAttr.getId();
    }

    public TestEnum getEnumAttr() {
        return enumAttr == null ? null : TestEnum.fromId(enumAttr);
    }

    public OffsetTime getOffsetTimeAttr() {
        return offsetTimeAttr;
    }

    public void setOffsetTimeAttr(OffsetTime offsetTimeAttr) {
        this.offsetTimeAttr = offsetTimeAttr;
    }

    public LocalTime getLocalTimeAttr() {
        return localTimeAttr;
    }

    public void setLocalTimeAttr(LocalTime localTimeAttr) {
        this.localTimeAttr = localTimeAttr;
    }

    public LocalDate getLocalDateAttr() {
        return localDateAttr;
    }

    public void setLocalDateAttr(LocalDate localDateAttr) {
        this.localDateAttr = localDateAttr;
    }

    public OffsetDateTime getOffsetDateTimeAttr() {
        return offsetDateTimeAttr;
    }

    public void setOffsetDateTimeAttr(OffsetDateTime offsetDateTimeAttr) {
        this.offsetDateTimeAttr = offsetDateTimeAttr;
    }

    public LocalDateTime getLocalDateTimeAttr() {
        return localDateTimeAttr;
    }

    public void setLocalDateTimeAttr(LocalDateTime localDateTimeAttr) {
        this.localDateTimeAttr = localDateTimeAttr;
    }

    public UUID getUuidAttr() {
        return uuidAttr;
    }

    public void setUuidAttr(UUID uuidAttr) {
        this.uuidAttr = uuidAttr;
    }

    public String getStringAttr() {
        return stringAttr;
    }

    public void setStringAttr(String stringAttr) {
        this.stringAttr = stringAttr;
    }

    public Long getLongAttr() {
        return longAttr;
    }

    public void setLongAttr(Long longAttr) {
        this.longAttr = longAttr;
    }

    public Integer getIntegerAttr() {
        return integerAttr;
    }

    public void setIntegerAttr(Integer integerAttr) {
        this.integerAttr = integerAttr;
    }

    public Double getDoubleAttr() {
        return doubleAttr;
    }

    public void setDoubleAttr(Double doubleAttr) {
        this.doubleAttr = doubleAttr;
    }

    public Date getDateTimeAttr() {
        return dateTimeAttr;
    }

    public void setDateTimeAttr(Date dateTimeAttr) {
        this.dateTimeAttr = dateTimeAttr;
    }

    public Date getDateAttr() {
        return dateAttr;
    }

    public void setDateAttr(Date dateAttr) {
        this.dateAttr = dateAttr;
    }

//    public byte[] getByteArrayAttr() {
//        return byteArrayAttr;
//    }
//
//    public void setByteArrayAttr(byte[] byteArrayAttr) {
//        this.byteArrayAttr = byteArrayAttr;
//    }

    public Boolean getBooleanAttr() {
        return booleanAttr;
    }

    public void setBooleanAttr(Boolean booleanAttr) {
        this.booleanAttr = booleanAttr;
    }

    public BigDecimal getBigDecimalAttr() {
        return bigDecimalAttr;
    }

    public void setBigDecimalAttr(BigDecimal bigDecimalAttr) {
        this.bigDecimalAttr = bigDecimalAttr;
    }

}
