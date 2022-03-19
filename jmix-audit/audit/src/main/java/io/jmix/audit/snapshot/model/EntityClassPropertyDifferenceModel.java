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

import java.util.ArrayList;
import java.util.List;

@JmixEntity(name = "audit_EntityClassPropertyDifferenceModel", annotatedPropertiesOnly = true)
public class EntityClassPropertyDifferenceModel extends EntityPropertyDifferenceModel {

    private Object beforeValue;

    private Object afterValue;

    private ItemState itemState = ItemState.Normal;

    private boolean isLinkChange;

    private List<EntityPropertyDifferenceModel> propertyDiffs = new ArrayList<>();

    private String beforeString = "";

    private String afterString = "";

    private void setLabel(){
        if (afterValue != null && isLinkChange) {
            label = instanceNameProvider.getInstanceName(afterValue);
        } else {
            label = "";
        }
    }

    public void setLinkChange(boolean linkChange) {
        isLinkChange = linkChange;
        setLabel();
    }

    @Override
    public boolean hasStateValues() {
        return true;
    }

    public void setBeforeValue(Object beforeValue) {
        this.beforeValue = beforeValue;
        if (beforeValue != null) {
            beforeString = instanceNameProvider.getInstanceName(beforeValue);
        }
    }

    public void setAfterValue(Object afterValue) {
        this.afterValue = afterValue;
        if (afterValue != null) {
            afterString = instanceNameProvider.getInstanceName(afterValue);
        }
        setLabel();
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
    public ItemState getItemState() {
        return itemState;
    }

    @Override
    public void setItemState(ItemState itemState) {
        this.itemState = itemState;
    }

    public List<EntityPropertyDifferenceModel> getPropertyDiffs() {
        return propertyDiffs;
    }

    public void setPropertyDiffs(List<EntityPropertyDifferenceModel> propertyDiffs) {
        this.propertyDiffs = propertyDiffs;
    }

    public boolean isLinkChange() {
        return isLinkChange;
    }

    @Override
    public String getBeforeString() {
        if (itemState != ItemState.Added && isLinkChange)
            return beforeString;
        else
            return "";
    }

    @Override
    public String getAfterString() {
        if (itemState != ItemState.Removed && isLinkChange)
            return afterString;
        else
            return "";
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
    public boolean itemStateVisible() {
        return itemState != ItemState.Normal;
    }

    @Override
    public String getLabel() {
        if (itemState == ItemState.Normal)
            return super.getLabel();
        else
            return "";
    }
}