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

package io.jmix.flowui.model.impl;

import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.SameAsUi;
import org.springframework.beans.factory.BeanFactory;

import javax.annotation.Nullable;

/**
 * A comparison function, which imposes a ordering for entity attribute values.
 * <p>
 * For example, to obtain a {@code Comparator} that compares {@code io.jmix.core.Entity} objects
 * by some property that is specified by {@code io.jmix.core.metamodel.model.MetaPropertyPath}:
 * <pre>{@code Comparator.comparing(e -> e.getValueEx(propertyPath), EntityValuesComparator.of(asc))}</pre>
 */
@SameAsUi
public class EntityValuesComparator<T> extends AbstractComparator<T> {
    public EntityValuesComparator(boolean asc, MetaClass metaClass, BeanFactory beanFactory) {
        super(asc);

        metadata = beanFactory.getBean(Metadata.class);
        metadataTools = beanFactory.getBean(MetadataTools.class);

        nullsLast = metaClass.getStore().isNullsLastSorting() ? 1 : -1;
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
