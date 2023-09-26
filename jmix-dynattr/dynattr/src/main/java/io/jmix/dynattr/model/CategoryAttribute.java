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

import com.google.common.base.Strings;
import io.jmix.core.Metadata;
import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.data.entity.ReferenceToEntity;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.impl.CategoryAttributeConfigurationConvertor;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@JmixEntity
@Entity(name = "dynat_CategoryAttribute")
@Table(name = "DYNAT_CATEGORY_ATTR")
@SystemLevel
public class CategoryAttribute implements Serializable {
    private static final long serialVersionUID = -6959392628534815752L;

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
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;

    @Column(name = "CATEGORY_ENTITY_TYPE")
    private String categoryEntityType;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "CODE", nullable = false)
    private String code;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "ENUMERATION")
    private String enumeration;

    @Column(name = "DATA_TYPE")
    private String dataType;

    @Column(name = "ENTITY_CLASS")
    private String entityClass;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "entityId", column = @Column(name = "DEFAULT_ENTITY_VALUE")),
            @AttributeOverride(name = "stringEntityId", column = @Column(name = "DEFAULT_STR_ENTITY_VALUE")),
            @AttributeOverride(name = "intEntityId", column = @Column(name = "DEFAULT_INT_ENTITY_VALUE")),
            @AttributeOverride(name = "longEntityId", column = @Column(name = "DEFAULT_LONG_ENTITY_VALUE"))
    })
    @EmbeddedParameters(nullAllowed = true)
    private ReferenceToEntity defaultEntity;

    @Column(name = "ORDER_NO")
    private Integer orderNo = 0;

    @Column(name = "SCREEN")
    private String screen;

    @Column(name = "REQUIRED")
    private Boolean required = false;

    @Column(name = "LOOKUP")
    private Boolean lookup = false;

    @Column(name = "TARGET_SCREENS")
    private String targetScreens;//comma separated list of screenId#componentId pairs. componentId might be empty

    @Column(name = "DEFAULT_STRING")
    private String defaultString;

    @Column(name = "DEFAULT_INT")
    private Integer defaultInt;

    @Column(name = "DEFAULT_DOUBLE")
    private Double defaultDouble;

    @Column(name = "DEFAULT_DECIMAL", precision = 36, scale = 10)
    private BigDecimal defaultDecimal;

    @Column(name = "DEFAULT_BOOLEAN")
    private Boolean defaultBoolean;

    @Column(name = "DEFAULT_DATE")
    private Date defaultDate;

    @Column(name = "DEFAULT_DATE_WO_TIME")
    private LocalDate defaultDateWithoutTime;

    @Column(name = "DEFAULT_DATE_IS_CURRENT")
    private Boolean defaultDateIsCurrent;

    @Column(name = "WIDTH", length = 20)
    private String width;

    @Column(name = "ROWS_COUNT")
    private Integer rowsCount;

    @Column(name = "IS_COLLECTION")
    private Boolean isCollection = false;

    @Column(name = "WHERE_CLAUSE")
    private String whereClause;

    @Column(name = "JOIN_CLAUSE")
    private String joinClause;

    @Column(name = "FILTER_XML")
    protected String filterXml;

    @Column(name = "LOCALE_NAMES")
    protected String localeNames;

    @Column(name = "LOCALE_DESCRIPTIONS")
    protected String localeDescriptions;

    @Column(name = "ENUMERATION_LOCALES")
    protected String enumerationLocales;

    @Column(name = "ATTRIBUTE_CONFIGURATION_JSON")
    @Convert(converter = CategoryAttributeConfigurationConvertor.class)
    protected CategoryAttributeConfiguration configuration;

    @PostConstruct
    public void init(@NotNull Metadata metadata) {
        defaultEntity = metadata.create(ReferenceToEntity.class);
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

    @InstanceName
    @DependsOnProperties({"name", "code"})
    public String getCaption() {
        return String.format("%s (%s)", getName(), getCode());
    }

    public void setCategory(Category entityType) {
        this.category = entityType;
    }

    public Category getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEnumeration() {
        return enumeration;
    }

    public void setEnumeration(String e) {
        this.enumeration = e;
    }

    public AttributeType getDataType() {
        return AttributeType.fromId(dataType);
    }

    public void setDataType(AttributeType dataType) {
        this.dataType = dataType != null ? dataType.getId() : null;
    }

    public Boolean getIsEntity() {
        return getDataType() == AttributeType.ENTITY;
    }

    public ReferenceToEntity getDefaultEntity() {
        return defaultEntity;
    }

    public void setDefaultEntity(ReferenceToEntity defaultEntity) {
        this.defaultEntity = defaultEntity;
    }

    public String getDefaultString() {
        return defaultString;
    }

    public void setDefaultString(String defaultString) {
        this.defaultString = defaultString;
    }

    public Integer getDefaultInt() {
        return defaultInt;
    }

    public void setDefaultInt(Integer defaultInt) {
        this.defaultInt = defaultInt;
    }

    public Double getDefaultDouble() {
        return defaultDouble;
    }

    public void setDefaultDouble(Double defaultDouble) {
        this.defaultDouble = defaultDouble;
    }

    public BigDecimal getDefaultDecimal() {
        return defaultDecimal;
    }

    public void setDefaultDecimal(BigDecimal defaultDecimal) {
        this.defaultDecimal = defaultDecimal;
    }

    public Boolean getDefaultBoolean() {
        return defaultBoolean;
    }

    public void setDefaultBoolean(Boolean defaultBoolean) {
        this.defaultBoolean = defaultBoolean;
    }

    public Date getDefaultDate() {
        return defaultDate;
    }

    public void setDefaultDate(Date defaultDate) {
        this.defaultDate = defaultDate;
    }

    public LocalDate getDefaultDateWithoutTime() {
        return defaultDateWithoutTime;
    }

    public void setDefaultDateWithoutTime(LocalDate defaultDateTime) {
        this.defaultDateWithoutTime = defaultDateTime;
    }

    public void setObjectDefaultEntityId(Object entity) {
        defaultEntity.setObjectEntityId(entity);
    }

    @Nullable
    public Object getObjectDefaultEntityId() {
        return defaultEntity != null ? defaultEntity.getObjectEntityId() : null;
    }

    public Integer getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getLookup() {
        return lookup;
    }

    public void setLookup(Boolean lookup) {
        this.lookup = lookup;
    }

    public Boolean getDefaultDateIsCurrent() {
        return defaultDateIsCurrent;
    }

    public void setDefaultDateIsCurrent(Boolean defaultDateIsCurrent) {
        this.defaultDateIsCurrent = defaultDateIsCurrent;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTargetScreens() {
        return targetScreens;
    }

    public void setTargetScreens(String targetScreens) {
        this.targetScreens = targetScreens;
    }

    public String getCategoryEntityType() {
        return categoryEntityType;
    }

    public void setCategoryEntityType(String categoryEntityType) {
        this.categoryEntityType = categoryEntityType;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public Integer getRowsCount() {
        return rowsCount;
    }

    public void setRowsCount(Integer rowsCount) {
        this.rowsCount = rowsCount;
    }

    public Boolean getIsCollection() {
        return isCollection;
    }

    public void setIsCollection(Boolean isCollection) {
        this.isCollection = isCollection;
    }

    public String getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    public String getJoinClause() {
        return joinClause;
    }

    public void setJoinClause(String joinClause) {
        this.joinClause = joinClause;
    }

    public String getFilterXml() {
        return filterXml;
    }

    public void setFilterXml(String filterXml) {
        this.filterXml = filterXml;
    }

    public String getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(String entityClass) {
        this.entityClass = entityClass;
    }

    public String getLocaleNames() {
        return localeNames;
    }

    public String getLocaleDescriptions() {
        return localeDescriptions;
    }

    public String getEnumerationLocales() {
        return enumerationLocales;
    }

    public String getNameMsgBundle() {
        return localeNames;
    }

    public void setLocaleNames(String localeNames) {
        this.localeNames = localeNames;
    }

    public String getDescriptionsMsgBundle() {
        return localeDescriptions;
    }

    public void setLocaleDescriptions(String localeDescriptions) {
        this.localeDescriptions = localeDescriptions;
    }

    public void setEnumerationLocales(String enumerationLocales) {
        this.enumerationLocales = enumerationLocales;
    }

    public String getEnumerationMsgBundle() {
        return enumerationLocales;
    }

    public CategoryAttributeConfiguration getConfiguration() {
        if (configuration == null) {
            configuration = new CategoryAttributeConfiguration();
        }
        return configuration;
    }

    public void setConfiguration(CategoryAttributeConfiguration configuration) {
        this.configuration = configuration;
    }

    @PrePersist
    @PreUpdate
    protected void initCategoryEntityType() {
        if (getCategory() != null) {
            setCategoryEntityType(getCategory().getEntityType());
        }
    }

    public Class<?> getJavaType() {
        if (!Strings.isNullOrEmpty(getEntityClass())) {
            return ReflectionHelper.getClass(getEntityClass());
        }
        return null;
    }

    public Set<String> getTargetScreensSet() {
        if (StringUtils.isNotBlank(getTargetScreens())) {
            return new HashSet<>(Arrays.asList(getTargetScreens().split(",")));
        } else {
            return Collections.emptySet();
        }
    }
}