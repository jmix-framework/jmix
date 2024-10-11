/*
 * Copyright 2024 Haulmont.
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

package io.jmix.pivottableflowui.kit.meta;

import com.vaadin.flow.shared.Registration;
import io.jmix.pivottableflowui.kit.data.JmixPivotTableItems;
import jakarta.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

class StudioListPivotTableItems implements JmixPivotTableItems<StudioPivotTableShape> {

    private List<StudioPivotTableShape> items;

    public StudioListPivotTableItems(List<StudioPivotTableShape> items) {
        this.items = items;
    }

    @Override
    public Collection<StudioPivotTableShape> getItems() {
        return items;
    }

    @Override
    public StudioPivotTableShape getItem(Object itemId) {
        return null;
    }

    @Nullable
    @Override
    public Object getItemValue(StudioPivotTableShape item, String propertyPath) {
        try {
            Field declaredField = item.getClass().getDeclaredField(propertyPath);
            declaredField.setAccessible(true);
            return declaredField.get(item);
        } catch (Exception e) {
            return "";
        }
    }

    @Nullable
    @Override
    public Object getItemId(StudioPivotTableShape item) {
        return item.getId();
    }

    @Override
    public void setItemValue(StudioPivotTableShape item, String propertyPath, @Nullable Object value) {

    }

    @Override
    public StudioPivotTableShape getItem(String stringId) {
        return items.stream().filter(i -> i.getId().toString().equals(stringId)).findFirst().orElse(null);
    }

    @Override
    public void updateItem(StudioPivotTableShape item) {

    }

    @Override
    public boolean containsItem(StudioPivotTableShape item) {
        return false;
    }

    @Override
    public Registration addItemsChangeListener(Consumer listener) {
        return null;
    }
}
