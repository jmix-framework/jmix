package test_support.entity.test;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Table(name = "SCR_INTEGER_ID_TEST_ENTITY")
@JmixEntity
@Entity(name = "scr_IntegerIdTestEntity")
public class IntegerIdTestEntity {

    @Id
    @JmixGeneratedValue
    @Column(name = "ID")
    protected Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @InstanceName
    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "CREATE_TS")
    private Date createTs;

    @Column(name = "CREATED_BY", length = 50)
    private String createdBy;

    @Column(name = "UPDATE_TS")
    private Date updateTs;

    @Column(name = "UPDATED_BY", length = 50)
    private String updatedBy;

    @Column(name = "DELETE_TS")
    private Date deleteTs;

    @Column(name = "DELETED_BY", length = 50)
    private String deletedBy;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DATATYPES_TEST_ENTITY3_ID")
    private DatatypesTestEntity3 datatypesTestEntity3;
    @JoinTable(name = "SCR_DATATYPES_TEST_ENTITY_INTEGER_ID_TEST_ENTITY_LINK",
            joinColumns = @JoinColumn(name = "INTEGER_ID_TEST_ENTITY_ID"),
            inverseJoinColumns = @JoinColumn(name = "DATATYPES_TEST_ENTITY_ID"))
    @ManyToMany
    private List<DatatypesTestEntity> datatypesTestEntities;

    public List<DatatypesTestEntity> getDatatypesTestEntities() {
        return datatypesTestEntities;
    }

    public void setDatatypesTestEntities(List<DatatypesTestEntity> datatypesTestEntities) {
        this.datatypesTestEntities = datatypesTestEntities;
    }

    public DatatypesTestEntity3 getDatatypesTestEntity3() {
        return datatypesTestEntity3;
    }

    public void setDatatypesTestEntity3(DatatypesTestEntity3 datatypesTestEntity3) {
        this.datatypesTestEntity3 = datatypesTestEntity3;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean isDeleted() {
        return deleteTs != null;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public Date getDeleteTs() {
        return deleteTs;
    }

    public void setDeleteTs(Date deleteTs) {
        this.deleteTs = deleteTs;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdateTs() {
        return updateTs;
    }

    public void setUpdateTs(Date updateTs) {
        this.updateTs = updateTs;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreateTs() {
        return createTs;
    }

    public void setCreateTs(Date createTs) {
        this.createTs = createTs;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}