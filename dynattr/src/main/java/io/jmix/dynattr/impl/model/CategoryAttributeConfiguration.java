/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.dynattr.impl.model;

import com.google.common.base.Strings;
import io.jmix.core.JmixEntity;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.ModelObject;
import io.jmix.core.metamodel.annotation.ModelProperty;
import io.jmix.dynattr.OptionsLoaderType;

import javax.annotation.Nullable;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@ModelObject(name = "sys_CategoryAttributeConfiguration")
@SystemLevel
public class CategoryAttributeConfiguration implements JmixEntity {
    private static final long serialVersionUID = 2670605418267938507L;

    @Id
    @ModelProperty
    protected String id;

    @ModelProperty
    protected Integer minInt;

    @ModelProperty
    protected Double minDouble;

    @ModelProperty
    protected BigDecimal minDecimal;

    @ModelProperty
    protected Integer maxInt;

    @ModelProperty
    protected Double maxDouble;

    @ModelProperty
    protected BigDecimal maxDecimal;

    @ModelProperty
    protected String validatorGroovyScript;

    @ModelProperty
    protected String columnName;

    @ModelProperty
    protected String columnAlignment;

    @ModelProperty
    protected Integer columnWidth;

    @ModelProperty
    protected String numberFormatPattern;

    @ModelProperty
    protected String optionsLoaderType;

    @ModelProperty
    protected String optionsLoaderScript;

    @ModelProperty
    protected String recalculationScript;

    @ModelProperty
    protected Integer columnNumber;

    @ModelProperty
    protected Integer rowNumber;

//    todo
//    @Transient
//    protected transient Collection<CategoryAttribute> dependentAttributes;
//
//    @Transient
//    protected List<UUID> dependsOnCategoryAttributesIds;

    @ModelProperty
    @Transient
    protected transient List<CategoryAttribute> dependsOnAttributes;

    public CategoryAttributeConfiguration() {
        id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getMinInt() {
        return minInt;
    }

    public void setMinInt(Integer minInt) {
        this.minInt = minInt;
    }

    public Double getMinDouble() {
        return minDouble;
    }

    public void setMinDouble(Double minDouble) {
        this.minDouble = minDouble;
    }

    public BigDecimal getMinDecimal() {
        return minDecimal;
    }

    public void setMinDecimal(BigDecimal minDecimal) {
        this.minDecimal = minDecimal;
    }

    public Integer getMaxInt() {
        return maxInt;
    }

    public void setMaxInt(Integer maxInt) {
        this.maxInt = maxInt;
    }

    public Double getMaxDouble() {
        return maxDouble;
    }

    public void setMaxDouble(Double maxDouble) {
        this.maxDouble = maxDouble;
    }

    public BigDecimal getMaxDecimal() {
        return maxDecimal;
    }

    public void setMaxDecimal(BigDecimal maxDecimal) {
        this.maxDecimal = maxDecimal;
    }

    public String getValidatorGroovyScript() {
        return validatorGroovyScript;
    }

    public void setValidatorGroovyScript(String validatorGroovyScript) {
        this.validatorGroovyScript = validatorGroovyScript;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnAlignment() {
        return columnAlignment;
    }

    public void setColumnAlignment(String columnAlignment) {
        this.columnAlignment = columnAlignment;
    }

    public Integer getColumnWidth() {
        return columnWidth;
    }

    public void setColumnWidth(Integer columnWidth) {
        this.columnWidth = columnWidth;
    }

    public String getNumberFormatPattern() {
        return numberFormatPattern;
    }

    public void setNumberFormatPattern(String numberFormatPattern) {
        this.numberFormatPattern = numberFormatPattern;
    }

    public String getRecalculationScript() {
        return recalculationScript;
    }

    public void setRecalculationScript(String recalculationScript) {
        this.recalculationScript = recalculationScript;
    }

    public List<CategoryAttribute> getDependsOnAttributes() {
        return dependsOnAttributes;
    }

    public void setDependsOnAttributes(List<CategoryAttribute> dependsOnAttributes) {
        this.dependsOnAttributes = dependsOnAttributes;
    }

//    TODO:
//    public Collection<CategoryAttribute> getDependentAttributes() {
//        if (dependentAttributes == null) {
//            DynamicAttributesTools dynamicAttributesTools = AppBeans.get(DynamicAttributesTools.NAME);
//            dependentAttributes = dynamicAttributesTools.getDependentCategoryAttributes(categoryAttribute);
//        }
//
//        return dependentAttributes;
//    }

    public Boolean isReadOnly() {
        return !Strings.isNullOrEmpty(recalculationScript);
    }

    @Nullable
    public OptionsLoaderType getOptionsLoaderType() {
        return OptionsLoaderType.fromId(optionsLoaderType);
    }

    public void setOptionsLoaderType(OptionsLoaderType optionsLoaderType) {
        this.optionsLoaderType = optionsLoaderType == null ? null : optionsLoaderType.getId();
    }

    public String getOptionsLoaderScript() {
        return optionsLoaderScript;
    }

    public void setOptionsLoaderScript(String optionsLoaderScript) {
        this.optionsLoaderScript = optionsLoaderScript;
    }

//    public boolean hasOptionsLoader() {
//        if (getOptionsLoaderType() != null) {
//            OptionsLoaderType loaderType = getOptionsLoaderType();
//            if (SQL == loaderType || GROOVY == loaderType) {
//                return !Strings.isNullOrEmpty(getOptionsLoaderScript());
//            } else if (JPQL == loaderType) {
//                return categoryAttribute != null && categoryAttribute.getDataType() == PropertyType.ENTITY;
//            }
//        }
//        return false;
//    }

    public Integer getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(Integer columnNumber) {
        this.columnNumber = columnNumber;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }
}