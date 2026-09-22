/*
 * Copyright 2022 Haulmont.
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
import io.jmix.core.MetadataTools;
import io.jmix.core.Sort;
import io.jmix.core.comparator.EntityValuesComparator;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.impl.LocalizedStringDatatype;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.Range;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.Sorter;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.BeanFactory;

import java.util.*;

/**
 * Base implementation of sorting collection containers.
 */
public abstract class BaseContainerSorter implements Sorter {

    protected BeanFactory beanFactory;

    private final CollectionContainer<?> container;

    @Nullable
    private Map<String, Comparator<?>> propertyComparators;

    public BaseContainerSorter(CollectionContainer<?> container, BeanFactory beanFactory) {
        this.container = container;
        this.beanFactory = beanFactory;
    }

    /**
     * Returns the container holding a collection of entity instances.
     *
     * @return the {@link CollectionContainer} instance
     */
    public CollectionContainer<?> getContainer() {
        return container;
    }

    /**
     * Sets property comparators. Comparators are used in in-memory sorting.
     * <p>
     * The key of the map is a property name and value is comparator.
     *
     * @param propertyComparators the map of property comparators
     */
    public void setPropertyComparators(@Nullable Map<String, Comparator<?>> propertyComparators) {
        this.propertyComparators = propertyComparators != null
                ? new HashMap<>(propertyComparators)
                : null;
    }

    /**
     * @param property a property name
     * @return the comparator set for the property, or {@code null} if none is set
     */
    @Nullable
    protected Comparator<?> findPropertyComparator(String property) {
        return propertyComparators != null ? propertyComparators.get(property) : null;
    }

    @Override
    public void sort(Sort sort) {
        sortInMemory(sort);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void sortInMemory(Sort sort) {
        List<Sort.Order> orders = sort.getOrders();

        if (orders.isEmpty() || container.getItems().isEmpty()) {
            return;
        }

        List containerItems = new ArrayList<>(container.getItems());
        MetaClass metaClass = container.getEntityMetaClass();

        Comparator comparator = createComparator(orders.get(0), metaClass);
        for (int i = 1; i < orders.size(); i++) {
            comparator = comparator.thenComparing(createComparator(orders.get(i), metaClass));
        }

        containerItems.sort(comparator);
        setItemsToContainer(containerItems);
    }

    protected abstract void setItemsToContainer(List<?> list);

    protected Comparator<?> createComparator(Sort.Order sortOrder, MetaClass metaClass) {
        Comparator<?> customComparator = findPropertyComparator(sortOrder.getProperty());

        if (customComparator != null) {
            return sortOrder.getDirection() == Sort.Direction.ASC ? customComparator : customComparator.reversed();
        }

        MetaPropertyPath propertyPath = metaClass.getPropertyPath(sortOrder.getProperty());
        if (propertyPath == null) {
            throw new IllegalArgumentException("Property " + sortOrder.getProperty() + " is invalid");
        }

        boolean asc = sortOrder.getDirection() == Sort.Direction.ASC;

        Comparator<?> localizedComparator = createLocalizedStringComparator(propertyPath, metaClass, asc);
        if (localizedComparator != null) {
            return localizedComparator;
        }

        EntityValuesComparator<Object> comparator = new EntityValuesComparator<>(asc, metaClass, beanFactory);
        return Comparator.comparing(e -> EntityValues.getValueEx(e, propertyPath), comparator);
    }

    /**
     * A property of the {@link LocalizedStringDatatype} type is compared by the text of the current user's
     * locale, so that in-memory sorting compares the same text the database compares, and the text the user
     * sees. The two still order that text by different rules: this comparison ignores case, the database
     * follows its collation.
     * <p>
     * The predicate repeats the one of the database expression: a {@code @Lob} property and an element
     * collection are left to the standard comparator, because the database cannot resolve them either.
     *
     * @param propertyPath the sorted property
     * @param metaClass    the meta class of the sorted items
     * @param asc          whether the sorting is ascending
     * @return the comparator by the resolved text, or {@code null} if the property is not a localized string
     */
    @Nullable
    protected Comparator<?> createLocalizedStringComparator(MetaPropertyPath propertyPath, MetaClass metaClass,
                                                            boolean asc) {
        MetaProperty property = propertyPath.getMetaProperty();
        Range range = property.getRange();
        if (!range.isDatatype()) {
            return null;
        }

        Datatype<?> datatype = range.asDatatype();
        if (!(datatype instanceof LocalizedStringDatatype)) {
            return null;
        }

        MetadataTools metadataTools = beanFactory.getBean(MetadataTools.class);
        if (metadataTools.isLob(property) || metadataTools.isElementCollection(property)) {
            return null;
        }

        LocalizedStringSupport localizedStringSupport = beanFactory.getBean(LocalizedStringSupport.class);
        Locale locale = localizedStringSupport.getCurrentLocale();
        EntityValuesComparator<Object> comparator = new EntityValuesComparator<>(asc, metaClass, beanFactory);

        // The key extractor of Comparator.comparing runs on every comparison, and resolving a value parses it,
        // so each item's text is resolved once and kept for the duration of the sort.
        Map<Object, String> resolvedTexts = new IdentityHashMap<>();
        // A null value keeps a null key, so that the store's null ordering is applied as for any other property.
        return Comparator.comparing(entity -> {
            String raw = EntityValues.getValueEx(entity, propertyPath);
            return raw == null
                    ? null
                    : resolvedTexts.computeIfAbsent(entity, item -> localizedStringSupport.resolve(raw, locale));
        }, comparator);
    }
}
