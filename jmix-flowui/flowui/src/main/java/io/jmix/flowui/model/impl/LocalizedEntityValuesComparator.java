/*
 * Copyright 2026 Haulmont.
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

import io.jmix.core.LocalizedStringSupport;
import io.jmix.core.comparator.EntityValuesComparator;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.impl.LocalizedStringDatatype;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.BeanFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * An {@link EntityValuesComparator} that compares a localized string by the text of the current user's locale
 * rather than by the stored string: the value of a sorted localized property, and a localized property of an
 * instance name when entities are compared by their instance names, at any depth. Any other value is compared the
 * way {@link EntityValuesComparator} compares it.
 * <p>
 * A {@code @Lob} property and an element collection are compared as stored, because the database cannot resolve
 * them either. The text of a stored value is derived once for the life of the comparator, which is one sort.
 *
 * @param <T> the type of the compared values
 */
public class
LocalizedEntityValuesComparator<T> extends EntityValuesComparator<T> {

    protected final LocalizedStringSupport localizedStringSupport;
    protected final Locale locale;
    protected final Map<String, String> texts = new HashMap<>();

    public LocalizedEntityValuesComparator(boolean asc, MetaClass metaClass, BeanFactory beanFactory) {
        super(asc, metaClass, beanFactory);

        localizedStringSupport = beanFactory.getBean(LocalizedStringSupport.class);
        locale = localizedStringSupport.getCurrentLocale();
    }

    /**
     * @param item the sorted item
     * @param path the sorted property path
     * @return the value the item is compared by: the text of a localized string, the stored value otherwise
     */
    @Nullable
    public Object getComparedValue(Object item, MetaPropertyPath path) {
        return resolveIfLocalized(EntityValues.getValueEx(item, path), path.getMetaProperty());
    }

    @Nullable
    @Override
    protected Object getInstanceNamePropertyValue(Object entity, MetaProperty property) {
        return resolveIfLocalized(super.getInstanceNamePropertyValue(entity, property), property);
    }

    /**
     * A null value stays null, so that the null ordering of the store applies as for any other property.
     */
    @Nullable
    protected Object resolveIfLocalized(@Nullable Object value, MetaProperty property) {
        return value instanceof String raw && isResolvedByLocale(property)
                ? texts.computeIfAbsent(raw, key -> localizedStringSupport.resolve(key, locale))
                : value;
    }

    protected boolean isResolvedByLocale(MetaProperty property) {
        Range range = property.getRange();
        if (!range.isDatatype()) {
            return false;
        }

        Datatype<?> datatype = range.asDatatype();
        return datatype instanceof LocalizedStringDatatype
                && !metadataTools.isLob(property)
                && !metadataTools.isElementCollection(property);
    }
}
