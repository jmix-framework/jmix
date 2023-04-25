/*
 * Copyright 2021 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.dynattr.model;


import io.jmix.core.DeletePolicy;
import io.jmix.core.Metadata;
import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.entity.ReferenceToEntity;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@JmixEntity
@jakarta.persistence.Entity(name = "dynat_CategoryAttributeValue")
@Table(name = "DYNAT_ATTR_VALUE")
@SystemLevel
public class CategoryAttributeValue implements Serializable {
    private static final long serialVersionUID = -2861790889151226985L;

    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    private UUID id;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @CreatedDate
    @Column(name = "CREATE_TS")
    private Date createTs;

    @CreatedBy
    @Column(name = "CREATED_BY", length = 50)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "UPDATE_TS")
    private Date updateTs;

    @LastModifiedBy
    @Column(name = "UPDATED_BY", length = 50)
    private String updatedBy;

    @DeletedDate
    @Column(name = "DELETE_TS")
    private Date deleteTs;

    @DeletedBy
    @Column(name = "DELETED_BY", length = 50)
    private String deletedBy;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ATTR_ID")
    private CategoryAttribute categoryAttribute;

    @Column(name = "CODE", nullable = false)
    private String code;

    @Column(name = "STRING_VALUE")
    private String stringValue;

    @Column(name = "INTEGER_VALUE")
    private Integer intValue;

    @Column(name = "DOUBLE_VALUE")
    private Double doubleValue;

    @Column(name = "DECIMAL_VALUE", precision = 36, scale = 10)
    private BigDecimal decimalValue;

    @Column(name = "BOOLEAN_VALUE")
    private Boolean booleanValue;

    @Column(name = "DATE_VALUE")
    private Date dateValue;

    @Column(name = "DATE_WO_TIME_VALUE")
    private LocalDate dateWithoutTimeValue;

    @Embedded
    @EmbeddedParameters(nullAllowed = false)
    private ReferenceToEntity entity;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "entityId", column = @Column(name = "ENTITY_VALUE")),
            @AttributeOverride(name = "stringEntityId", column = @Column(name = "STRING_ENTITY_VALUE")),
            @AttributeOverride(name = "intEntityId", column = @Column(name = "INT_ENTITY_VALUE")),
            @AttributeOverride(name = "longEntityId", column = @Column(name = "LONG_ENTITY_VALUE"))
    })
    @EmbeddedParameters(nullAllowed = false)
    private ReferenceToEntity entityValue;

    @Transient
    private Object transientEntityValue;

    @OneToMany(mappedBy = "parent")
    @OnDelete(DeletePolicy.CASCADE)
    private List<CategoryAttributeValue> childValues;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    private CategoryAttributeValue parent;

    @Transient
    private List<Object> transientCollectionValue;

    @PostConstruct
    public void init(Metadata metadata) {
        entity = metadata.create(ReferenceToEntity.class);
        entityValue = metadata.create(ReferenceToEntity.class);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Date getCreateTs() {
        return createTs;
    }

    public void setCreateTs(Date createTs) {
        this.createTs = createTs;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getUpdateTs() {
        return updateTs;
    }

    public void setUpdateTs(Date updateTs) {
        this.updateTs = updateTs;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getDeleteTs() {
        return deleteTs;
    }

    public void setDeleteTs(Date deleteTs) {
        this.deleteTs = deleteTs;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public void setCategoryAttribute(CategoryAttribute categoryAttribute) {
        this.categoryAttribute = categoryAttribute;
    }

    public CategoryAttribute getCategoryAttribute() {
        return categoryAttribute;
    }

    public ReferenceToEntity getEntity() {
        return entity;
    }

    public void setEntity(ReferenceToEntity entity) {
        this.entity = entity;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(Double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public BigDecimal getDecimalValue() {
        return decimalValue;
    }

    public void setDecimalValue(BigDecimal decimalValue) {
        this.decimalValue = decimalValue;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public LocalDate getDateWithoutTimeValue() {
        return dateWithoutTimeValue;
    }

    public void setDateWithoutTimeValue(LocalDate dateWithoutTimeValue) {
        this.dateWithoutTimeValue = dateWithoutTimeValue;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ReferenceToEntity getEntityValue() {
        return entityValue;
    }

    public void setEntityValue(ReferenceToEntity entityValue) {
        this.entityValue = entityValue;
    }

    public Object getTransientEntityValue() {
        return transientEntityValue;
    }

    public void setTransientEntityValue(Object transientEntityValue) {
        this.transientEntityValue = transientEntityValue;
    }

    public List<CategoryAttributeValue> getChildValues() {
        return childValues;
    }

    public void setChildValues(List<CategoryAttributeValue> childValues) {
        this.childValues = childValues;
    }

    public CategoryAttributeValue getParent() {
        return parent;
    }

    public void setParent(CategoryAttributeValue parent) {
        this.parent = parent;
    }

    public List<Object> getTransientCollectionValue() {
        return transientCollectionValue;
    }

    public void setTransientCollectionValue(List<Object> transientCollectionValue) {
        this.transientCollectionValue = transientCollectionValue;
    }

    public Object getValue() {
        if (stringValue != null) {
            return stringValue;
        } else if (intValue != null) {
            return intValue;
        } else if (doubleValue != null) {
            return doubleValue;
        } else if (decimalValue != null) {
            return decimalValue;
        } else if (dateValue != null) {
            return dateValue;
        } else if (dateWithoutTimeValue != null) {
            return dateWithoutTimeValue;
        } else if (booleanValue != null) {
            return booleanValue;
        } else if (transientEntityValue != null) {
            return transientEntityValue;
        }
        if (transientCollectionValue != null) {
            return transientCollectionValue;
        }

        return null;
    }

    public void setObjectEntityId(Object entityId) {
        entity.setObjectEntityId(entityId);
    }

    public Object getObjectEntityId() {
        return entity.getObjectEntityId();
    }

    public void setObjectEntityValueId(Object entityId) {
        entityValue.setObjectEntityId(entityId);
    }

    public Object getObjectEntityValueId() {
        return entityValue.getObjectEntityId();
    }
}