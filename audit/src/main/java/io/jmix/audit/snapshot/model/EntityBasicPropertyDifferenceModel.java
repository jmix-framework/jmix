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

import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.datatype.impl.EnumClass;

import java.util.Collection;

@JmixEntity(name = "audit_EntityBasicPropertyDifferenceModel", annotatedPropertiesOnly = true)
public class EntityBasicPropertyDifferenceModel extends EntityPropertyDifferenceModel {

    private Object beforeValue;

    private Object afterValue;

    @Override
    public boolean hasStateValues() {
        return true;
    }

    public void setBeforeValue(Object beforeValue) {
        this.beforeValue = beforeValue;
    }

    public void setAfterValue(Object afterValue) {
        this.afterValue = afterValue;
    }

    @Override
    public Object getBeforeValue() {
        return beforeValue;
    }

    @Override
    public Object getAfterValue() {
        return afterValue;
    }

    @Override
    public String getLabel() {
        if (afterValue != null)
            return afterValue.toString();
        else
            return super.getLabel();
    }

    @Override
    public String getBeforeString() {
        if (beforeValue != null) {
            if (beforeValue instanceof EnumClass) {
                return getEnumItemName(beforeValue);
            } else if (beforeValue instanceof Collection) {
                return getCollectionString(beforeValue);
            }
            return String.valueOf(beforeValue);
        }
        return super.getBeforeString();
    }

    @Override
    public String getAfterString() {
        if (afterValue != null) {
            if (afterValue instanceof EnumClass) {
                return getEnumItemName(afterValue);
            } else if (afterValue instanceof Collection) {
                return getCollectionString(afterValue);
            }
            return String.valueOf(afterValue);
        }
        return super.getAfterString();
    }

    private String getEnumItemName(Object enumItem) {
        String nameKey = enumItem.getClass().getSimpleName() + "." + enumItem.toString();
        return messages.getMessage(enumItem.getClass(), nameKey);
    }

    private String getCollectionString(Object collection) {
        return String.valueOf(beforeValue);
    }

    @Override
    public String getBeforeCaption() {
        String value = getBeforeString();
        if (value.length() > EntityPropertyDifferenceModel.CAPTION_CHAR_COUNT)
            return value.substring(0, EntityPropertyDifferenceModel.CAPTION_CHAR_COUNT) + "...";
        return super.getBeforeCaption();
    }

    @Override
    public String getAfterCaption() {
        String value = getAfterString();
        if (value.length() > EntityPropertyDifferenceModel.CAPTION_CHAR_COUNT)
            return value.substring(0, EntityPropertyDifferenceModel.CAPTION_CHAR_COUNT) + "...";
        return super.getAfterCaption();
    }

    @Override
    public ItemState getItemState() {
        return ItemState.Modified;
    }
}