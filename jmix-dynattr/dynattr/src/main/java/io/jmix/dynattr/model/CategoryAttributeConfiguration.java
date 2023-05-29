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
import io.jmix.core.Entity;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.dynattr.OptionsLoaderType;

import jakarta.annotation.Nullable;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@JmixEntity(name = "dynat_CategoryAttributeConfiguration")
@SystemLevel
public class CategoryAttributeConfiguration implements Serializable, Cloneable {
    private static final long serialVersionUID = 2670605418267938507L;

    @Id
    @JmixProperty
    @JmixGeneratedValue
    protected String id;

    @JmixProperty
    protected Integer minInt;

    @JmixProperty
    protected Double minDouble;

    @JmixProperty
    protected BigDecimal minDecimal;

    @JmixProperty
    protected Integer maxInt;

    @JmixProperty
    protected Double maxDouble;

    @JmixProperty
    protected BigDecimal maxDecimal;

    @JmixProperty
    protected String validatorGroovyScript;

    @JmixProperty
    protected String columnName;

    @JmixProperty
    protected String columnAlignment;

    @JmixProperty
    protected Integer columnWidth;

    @JmixProperty
    protected String numberFormatPattern;

    @JmixProperty
    protected String optionsLoaderType;

    @JmixProperty
    protected String optionsLoaderScript;

    @JmixProperty
    protected String recalculationScript;

    @JmixProperty
    protected Integer columnNumber;

    @JmixProperty
    protected Integer rowNumber;

    @Transient
    protected List<String> dependsOnAttributeCodes;

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

    public List<String> getDependsOnAttributeCodes() {
        return dependsOnAttributeCodes;
    }

    public void setDependsOnAttributeCodes(List<String> dependsOnAttributeCodes) {
        this.dependsOnAttributeCodes = dependsOnAttributeCodes;
    }

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

    @Override
    public Object clone() {
        try {
            CategoryAttributeConfiguration configuration = (CategoryAttributeConfiguration) super.clone();
            configuration.setId(UUID.randomUUID().toString());
            ((Entity) configuration).__copyEntityEntry();
            return configuration;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Error while clone object", e);
        }
    }
}