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

package com.haulmont.cuba.core.config;

import com.google.common.base.Strings;
import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import io.jmix.core.Entity;
import io.jmix.core.UuidProvider;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.impl.NumberDatatype;
import io.jmix.core.metamodel.datatype.impl.StringDatatype;

import java.util.UUID;

@SystemLevel
@JmixEntity(name = "sys$AppPropertyEntity")
public class AppPropertyEntity implements Entity, Comparable<AppPropertyEntity> {

    private static final long serialVersionUID = 546387295440108557L;

    @JmixGeneratedValue
    @JmixProperty(mandatory = true)
    @JmixId
    protected UUID id;

    public AppPropertyEntity() {
        this.id =  UuidProvider.createUuid();
    }

    private AppPropertyEntity parent;

    private String name;

    private String defaultValue;

    private String currentValue;

    private Boolean category = true;

    private Boolean overridden = false;

    private String dataTypeName;

    private String enumValues;

    private Boolean secret = false;


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AppPropertyEntity getParent() {
        return parent;
    }

    public void setParent(AppPropertyEntity parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public Boolean getCategory() {
        return category;
    }

    public void setCategory(Boolean category) {
        this.category = category;
    }

    public Boolean getOverridden() {
        return overridden;
    }

    public void setOverridden(Boolean overridden) {
        this.overridden = overridden;
    }

    public String getDataTypeName() {
        return dataTypeName;
    }

    public void setDataTypeName(String dataTypeName) {
        this.dataTypeName = dataTypeName;
    }

    public String getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(String enumValues) {
        this.enumValues = enumValues;
    }

    public Boolean getSecret() {
        return secret;
    }

    public void setSecret(Boolean secret) {
        this.secret = secret;
    }

    @Override
    public int compareTo(AppPropertyEntity o) {
        return name.compareTo(o.name);
    }

    /**
     * Method returns the current value that should be displayed in the UI. The method takes into account the {@link #secret}
     * property value and displays a value placeholder if it should not be visible
     */
    @JmixProperty
    public String getDisplayedCurrentValue() {
        if (Boolean.TRUE.equals(secret) && isDatatypeMayBeHidden()) {
            return AppBeans.get(Messages.class).getMessage(AppPropertyEntity.class, "AppPropertyEntity.valueIsSecret");
        } else {
            return currentValue;
        }
    }

    /**
     * Method returns the default value that should be displayed in the UI. The method takes into account the {@link #secret}
     * property value and displays a value placeholder if it should not be visible
     */
    @JmixProperty
    public String getDisplayedDefaultValue() {
        if (Boolean.TRUE.equals(secret) && isDatatypeMayBeHidden()) {
            return AppBeans.get(Messages.class).getMessage(AppPropertyEntity.class, "AppPropertyEntity.valueIsSecret");
        } else {
            return defaultValue;
        }
    }

    protected boolean isDatatypeMayBeHidden() {
        if (Strings.isNullOrEmpty(dataTypeName)) return false;
        Datatype datatype = Datatypes.get(dataTypeName);
        return datatype instanceof StringDatatype || datatype instanceof NumberDatatype;
    }
}
