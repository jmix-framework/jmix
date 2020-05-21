/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.core.model.common;

import io.jmix.core.AppBeans;
import io.jmix.core.MessageTools;
import io.jmix.data.entity.BaseUuidEntity;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.ModelObject;
import io.jmix.core.metamodel.annotation.ModelProperty;

/**
 * Diff between properties in entity snapshots
 */
@ModelObject(name = "sys$EntityPropertyDiff")
@SystemLevel
public abstract class EntityPropertyDiff extends BaseUuidEntity {
    private static final long serialVersionUID = -6467322033937742101L;

    public enum ItemState {
        Normal,
        Modified,
        Added,
        Removed
    }

    protected String propertyCaption;
    protected String label = "";
    protected String metaClassName;
    protected String propertyName;

    protected static final int CAPTION_CHAR_COUNT = 30;

    protected EntityPropertyDiff(io.jmix.core.metamodel.model.MetaProperty metaProperty) {
        metaClassName = metaProperty.getDomain().getName();
        propertyName = metaProperty.getName();

        MessageTools messageTools = AppBeans.get(MessageTools.NAME);
        propertyCaption = messageTools.getPropertyCaption(metaProperty.getDomain(), metaProperty.getName());

    }

    public String getMetaClassName() {
        return metaClassName;
    }

    @ModelProperty
    public String getName() {
        return propertyCaption;
    }

    @ModelProperty
    public void setName(String name) {
        propertyCaption = name;
    }

    @ModelProperty
    public String getLabel() {
        return label;
    }

    @ModelProperty
    public void setLabel(String label) {
        this.label = label;
    }

    public boolean hasStateValues() {
        return false;
    }

    public Object getBeforeValue() {
        return null;
    }

    public Object getAfterValue() {
        return null;
    }

    @ModelProperty
    public String getBeforeString() {
        return "";
    }

    @ModelProperty
    public String getAfterString() {
        return "";
    }

    @ModelProperty
    public String getBeforeCaption() {
        return getBeforeString();
    }

    @ModelProperty
    public String getAfterCaption() {
        return getAfterString();
    }

    @ModelProperty
    public ItemState getItemState() {
        return ItemState.Normal;
    }

    @ModelProperty
    public void setItemState(ItemState itemState) {
    }

    public String getPropertyName() {
        return propertyName;
    }

    public boolean itemStateVisible() {
        return false;
    }
}
