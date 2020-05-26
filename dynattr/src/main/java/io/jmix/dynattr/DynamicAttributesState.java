/*
 * Copyright 2020 Haulmont.
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

package io.jmix.dynattr;

import com.google.common.base.Preconditions;
import io.jmix.core.EntityEntry;
import io.jmix.core.EntityEntryExtraState;
import io.jmix.core.EntityValuesProvider;
import io.jmix.core.impl.EntityInternals;

import javax.annotation.Nullable;
import java.util.Objects;

public class DynamicAttributesState<K> implements EntityEntryExtraState, EntityValuesProvider {
    protected EntityEntry entityEntry;
    protected DynamicAttributes dynamicModel;

    public DynamicAttributesState(EntityEntry entityEntry) {
        this.entityEntry = entityEntry;
    }

    @Nullable
    public DynamicAttributes getDynamicAttributes() {
        return dynamicModel;
    }

    @Nullable
    public void setDynamicAttributes(DynamicAttributes dynamicModel) {
        this.dynamicModel = dynamicModel;
    }

    @Override
    public boolean supportAttribute(String name) {
        return name.startsWith("+");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttributeValue(String name) {
        return (T) dynamicModel.getValue(DynAttrUtils.getAttributeCodeFromProperty(name));
    }

    @Override
    public void setAttributeValue(String name, @Nullable Object value, boolean checkEquals) {
        Preconditions.checkState(dynamicModel != null, "Dynamic attributes should be loaded explicitly");

        String code = DynAttrUtils.getAttributeCodeFromProperty(name);
        Object oldValue = dynamicModel.getValue(code);
        //todo: check sequence
        dynamicModel.setValue(code, value);

        if (value != null && !Objects.equals(value, oldValue)) {
            EntityInternals.fireListeners(getEntityEntry().getSource(), name, oldValue, value);
        }
    }

    @Override
    public EntityEntry getEntityEntry() {
        return entityEntry;
    }
}
