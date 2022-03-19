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

package io.jmix.audit.snapshot.model;

import io.jmix.core.InstanceNameProvider;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.core.metamodel.model.MetaProperty;

import javax.annotation.PostConstruct;
import javax.persistence.Id;
import java.util.UUID;

/**
 * Diff between properties in entity snapshots
 */
@JmixEntity(name = "audit_EntityPropertyDifferenceModel")
public abstract class EntityPropertyDifferenceModel {
    public enum ItemState {
        Normal,
        Modified,
        Added,
        Removed;
    }

    @Id
    @JmixGeneratedValue
    protected UUID id;
    protected String propertyCaption;
    protected String label = "";
    protected String metaClassName;
    protected String propertyName;
    private MessageTools messageTools;
    protected InstanceNameProvider instanceNameProvider;
    protected Messages messages;
    protected EntityPropertyDifferenceModel parentProperty;

    protected static final int CAPTION_CHAR_COUNT = 30;

    public void setMetaProperty(MetaProperty metaProperty){
        metaClassName = metaProperty.getDomain().getName();
        propertyName = metaProperty.getName();
        propertyCaption = messageTools.getPropertyCaption(metaProperty.getDomain(), metaProperty.getName());
    }

    @PostConstruct
    private void init(MessageTools messageTools, Messages messages, InstanceNameProvider instanceNameProvider){
        this.messageTools = messageTools;
        this.messages = messages;
        this.instanceNameProvider = instanceNameProvider;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public EntityPropertyDifferenceModel getParentProperty() {
        return parentProperty;
    }

    public void setParentProperty(EntityPropertyDifferenceModel parentProperty) {
        this.parentProperty = parentProperty;
    }

    public String getMetaClassName() {
        return metaClassName;
    }

    @JmixProperty
    public String getName() {
        return propertyCaption;
    }

    @JmixProperty
    public void setName(String name) {
        propertyCaption = name;
    }

    @JmixProperty
    public String getLabel() {
        return label;
    }

    @JmixProperty
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

    @JmixProperty
    public String getBeforeString() {
        return "";
    }

    @JmixProperty
    public String getAfterString() {
        return "";
    }

    @JmixProperty
    public String getBeforeCaption() {
        return getBeforeString();
    }

    @JmixProperty
    public String getAfterCaption() {
        return getAfterString();
    }

    @JmixProperty
    public ItemState getItemState() {
        return ItemState.Normal;
    }

    @JmixProperty
    public void setItemState(ItemState itemState) {
    }

    public String getPropertyName() {
        return propertyName;
    }

    public boolean itemStateVisible() {
        return false;
    }
}