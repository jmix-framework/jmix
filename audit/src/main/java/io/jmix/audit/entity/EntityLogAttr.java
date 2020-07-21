/*
 * Copyright 2019 Haulmont.
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

package io.jmix.audit.entity;

import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.ModelObject;
import io.jmix.core.metamodel.annotation.ModelProperty;
import io.jmix.data.entity.BaseUuidEntity;

/**
 * Record containing changed entity attribute.
 */
@ModelObject(name = "audit_EntityLogAttr")
@SystemLevel
public class EntityLogAttr extends BaseUuidEntity {

    private static final long serialVersionUID = 4258700403293876630L;

    public static final String VALUE_ID_SUFFIX = "-id";
    public static final String MP_SUFFIX = "-mp";
    public static final String OLD_VALUE_SUFFIX = "-oldVl";
    public static final String OLD_VALUE_ID_SUFFIX = "-oldVlId";

    @ModelProperty
    private EntityLogItem logItem;

    @ModelProperty
    private String name;

    @ModelProperty
    private String value;

    @ModelProperty
    private String oldValue;

    @ModelProperty
    private String valueId;

    @ModelProperty
    private String oldValueId;

    @ModelProperty
    private String messagesPack;

    public EntityLogItem getLogItem() {
        return logItem;
    }

    public void setLogItem(EntityLogItem logItem) {
        this.logItem = logItem;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getValueId() {
        return valueId;
    }

    public void setValueId(String valueId) {
        this.valueId = valueId;
    }

    public String getOldValueId() {
        return oldValueId;
    }

    public void setOldValueId(String oldValueId) {
        this.oldValueId = oldValueId;
    }

    public String getMessagesPack() {
        return messagesPack;
    }

    public void setMessagesPack(String messagesPack) {
        this.messagesPack = messagesPack;
    }
}
