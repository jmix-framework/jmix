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

package io.jmix.ui.component.impl;

import io.jmix.ui.component.SingleSelectList;
import io.jmix.ui.widget.listselect.JmixSingleListSelect;

import javax.annotation.Nullable;
import java.util.*;

public class SingleSelectListImpl<V> extends AbstractSelectList<V, V, JmixSingleListSelect<V>>
        implements SingleSelectList<V> {

    @Override
    public void setNullOptionVisible(boolean nullOptionVisible) {
        component.setNullOptionVisible(nullOptionVisible);
    }

    @Override
    public boolean isNullOptionVisible() {
        return component.isNullOptionVisible();
    }

    @Override
    protected JmixSingleListSelect<V> createComponent() {
        return new JmixSingleListSelect<>();
    }

    @Override
    protected Collection<V> getCollectionValue() {
        return getValue() != null ?
                Collections.singletonList(getValue()) : Collections.emptyList();
    }

    @Override
    protected V convertToModel(@Nullable Set<V> componentRawValue) {
        Set<V> rawValue = componentRawValue != null ? componentRawValue : Collections.emptySet();

        // Set must contain only one item
        if (rawValue.size() > 1) {
            throw new IllegalStateException("Field does not work with multiple values");
        }

        Optional<V> optional = getOptions() == null ? Optional.empty()
                : getOptions().getOptions().filter(rawValue::contains).findFirst();

        return optional.orElse(null);
    }

    @Override
    protected Set<V> convertToPresentation(@Nullable V modelValue) {
        return modelValue != null ? Collections.singleton(modelValue) : Collections.emptySet();
    }
}
