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

package com.haulmont.cuba.gui.data.impl;

import com.haulmont.cuba.core.global.AppBeans;
import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.ui.model.impl.AbstractComparator;

import javax.annotation.Nullable;
import java.util.Comparator;

/**
 * @deprecated Use {@link io.jmix.ui.model.impl.EntityValuesComparator} instead
 */
@Deprecated
public class EntityValuesComparator<T> extends AbstractComparator<T> {
    public static final Comparator<Object> NATURAL_ORDER = new EntityValuesComparator<>(true);
    public static final Comparator<Object> REVERSE_ORDER = new EntityValuesComparator<>(false);

    public static Comparator<Object> asc(boolean asc) {
        return asc ? NATURAL_ORDER : REVERSE_ORDER;
    }

    protected EntityValuesComparator(boolean asc) {
        super(asc);

        metadata = AppBeans.get(Metadata.class);
        metadataTools = AppBeans.get(MetadataTools.class);
    }

    @Override
    public int compare(T o1, T o2) {
        return __compare(transformValue(o1), transformValue(o2));
    }

    @Nullable
    protected Object transformValue(@Nullable T value) {
        Object newValue = value;
        if (!(value == null || value instanceof Comparable || value instanceof Entity)) {
            newValue = value.toString();
        }
        return newValue;
    }
}
