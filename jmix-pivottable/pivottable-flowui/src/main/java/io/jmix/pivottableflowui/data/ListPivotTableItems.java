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

package io.jmix.pivottableflowui.data;

import com.vaadin.flow.shared.Registration;
import io.jmix.core.metamodel.model.utils.MethodsCache;
import io.jmix.core.metamodel.model.utils.ObjectPathUtils;
import io.jmix.flowui.data.BindingState;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.kit.event.EventBus;
import io.jmix.pivottableflowui.component.PivotTable;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Data provider for a {@link PivotTable} component in which all items are stored in {@link List}
 */
public class ListPivotTableItems<T> implements PivotTableItems<T> {

    private EventBus eventBus;
    private static final Map<Class<?>, MethodsCache> methodCacheMap = new ConcurrentHashMap<>();

    protected final List<T> items = new ArrayList<>();
    protected final String idAttribute;
    protected final Class<?> idType;

    public ListPivotTableItems(String idAttribute, Class<?> idType) {
        this.idAttribute = idAttribute;
        this.idType = idType;
    }

    public ListPivotTableItems(String idAttribute, Class<?> idType, Collection<T> items) {
        this(idAttribute, idType);
        this.items.addAll(items);
    }

    @SafeVarargs
    public ListPivotTableItems(String idAttribute, Class<?> idType, T... items) {
        this(idAttribute, idType, List.of(items));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Registration addItemsChangeListener(Consumer<ItemsChangeEvent<T>> listener) {
        return getEventBus().addListener(ItemsChangeEvent.class, ((Consumer) listener));
    }

    @Override
    public Collection<T> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Nullable
    @Override
    public <V> V getItemValue(T item, String propertyPath) {
        //noinspection unchecked
        return (V) getValueInternal(item, propertyPath);
    }

    @Override
    public void setItemValue(T item, String propertyPath, @Nullable Object value) {
        setValueInternal(item, propertyPath, value);
    }

    @Nullable
    @Override
    public Object getItemId(T item) {
        return getItemValue(item, idAttribute);
    }

    @Override
    public T getItem(Object itemId) {
        return items.stream()
                .filter(dataItem -> Objects.equals(itemId, getItemValue(dataItem, idAttribute)))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Collection doesn't have an item with the given id '%s'".formatted(itemId))
                );
    }

    @Override
    public T getItem(String stringId) {
        Object id = deserializeId(stringId);
        return getItem(id);
    }

    /**
     * Adds passed item to the list.
     *
     * @param item item to add
     */
    public void addItem(T item) {
        items.add(item);
        fireChangeEvent(ItemsChangeType.ADD, Collections.singletonList(item));
    }

    /**
     * Adds passed items to the list.
     *
     * @param items items to add
     */
    @SafeVarargs
    public final void addItems(T... items) {
        List<T> itemsToAdd = List.of(items);
        this.items.addAll(itemsToAdd);
        fireChangeEvent(ItemsChangeType.ADD, itemsToAdd);
    }

    /**
     * Adds passed items to the list.
     *
     * @param items items to add
     */
    public void addItems(Collection<T> items) {
        List<T> itemsToAdd = List.copyOf(items);
        this.items.addAll(itemsToAdd);
        fireChangeEvent(ItemsChangeType.ADD, itemsToAdd);
    }

    @Override
    public void updateItem(T item) {
        if (items.contains(item)) {
            items.set(items.indexOf(item), item);
            fireChangeEvent(ItemsChangeType.UPDATE, Collections.singleton(item));
        } else {
            throw new IllegalArgumentException("No such element for %s".formatted(getClass().getSimpleName()));
        }
    }

    /**
     * Removes the passed item from the list.
     *
     * @param item item to remove
     */
    public void removeItem(T item) {
        items.remove(item);
        fireChangeEvent(ItemsChangeType.REMOVE, Collections.singletonList(item));
    }

    /**
     * Removes all items.
     */
    public void removeAll() {
        items.clear();
        fireChangeEvent(ItemsChangeType.REFRESH, Collections.emptyList());
    }

    @Override
    public boolean containsItem(T item) {
        return items.contains(item);
    }

    @Override
    public Class<T> getType() {
        throw new UnsupportedOperationException("%s doesn't support type".formatted(getClass().getSimpleName()));
    }

    @Override
    public BindingState getState() {
        return BindingState.ACTIVE;
    }

    @Override
    public Registration addStateChangeListener(Consumer<DataUnit.StateChangeEvent> listener) {
        return getEventBus().addListener(DataUnit.StateChangeEvent.class, listener);
    }

    @Nullable
    protected Object getValueInternal(@Nullable T item, String path) {
        String[] properties = ObjectPathUtils.parseValuePath(path);

        Object currentValue = null;
        Object currentObject = item;

        for (String property : properties) {
            if (currentObject == null) {
                break;
            }

            //noinspection unchecked
            currentValue = getMethodsCache(currentObject).getGetter(property).apply(currentObject);

            if (currentValue == null) {
                break;
            }

            if (currentValue instanceof Collection<?> collectionValue) {
                return collectionValue;
            }

            currentObject = currentValue;
        }

        return currentValue;
    }

    @SuppressWarnings("unchecked")
    protected void setValueInternal(T item, String path, @Nullable Object value) {
        String[] properties = ObjectPathUtils.parseValuePath(path);

        Object currentObject = item;

        for (String property : Arrays.copyOf(properties, properties.length - 1)) {
            if (currentObject == null) {
                return;
            }

            //noinspection unchecked
            currentObject = getMethodsCache(currentObject).getGetter(property).apply(currentObject);
        }


        getMethodsCache(currentObject).getSetter(properties[properties.length - 1]).accept(currentObject, value);
    }

    protected Object deserializeId(String stringValue) {
        if (String.class == idType) {
            return stringValue;
        } else if (Integer.class == idType) {
            return Integer.valueOf(stringValue);
        } else if (Long.class == idType) {
            return Long.valueOf(stringValue);
        } else if (UUID.class == idType) {
            return UUID.fromString(stringValue);
        } else {
            throw new IllegalArgumentException(
                    String.format("Unable to deserialize id '%s' of type '%s'", stringValue, idType));
        }
    }

    protected MethodsCache getMethodsCache(Object object) {
        Class<?> cls = object.getClass();
        return methodCacheMap.computeIfAbsent(cls, k -> MethodsCache.getOrCreate(cls));
    }

    protected void fireChangeEvent(ItemsChangeType operationType, Collection<T> items) {
        getEventBus().fireEvent(new ItemsChangeEvent<>(this, operationType, items));
    }

    protected EventBus getEventBus() {
        if (eventBus == null) {
            eventBus = new EventBus();
        }

        return eventBus;
    }
}

