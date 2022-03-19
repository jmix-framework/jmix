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

package io.jmix.ui.component.data.options;

import io.jmix.core.Metadata;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.event.sys.VoidSubscription;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.component.data.Options;
import io.jmix.ui.component.data.meta.EntityOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Options based on a map that contains entities.
 *
 * @param <E> entity type
 */
public class MapEntityOptions<E> extends MapOptions<E> implements Options<E>, EntityOptions<E> {

    private static final Logger log = LoggerFactory.getLogger(MapEntityOptions.class);

    protected Metadata metadata;

    protected E selectedItem = null;

    public MapEntityOptions(Map<String, E> options, Metadata metadata) {
        super(options);
        this.metadata = metadata;
    }

    @Override
    public void setSelectedItem(@Nullable E item) {
        this.selectedItem = item;
    }

    @Nullable
    public E getSelectedItem() {
        return selectedItem;
    }

    @Override
    public boolean containsItem(@Nullable E item) {
        return getItemsCollection().containsValue(item);
    }

    @Override
    public void updateItem(E item) {
        // do nothing
        log.debug("The 'updateItem' method is ignored, because underlying collection may be unmodifiable");
    }

    @Override
    public void refresh() {
        // do nothing
        log.debug("The 'refresh' method is ignored because the underlying collection contains static data");
    }

    @Override
    public Subscription addValueChangeListener(Consumer<ValueChangeEvent<E>> listener) {
        return VoidSubscription.INSTANCE;
    }

    @Nullable
    @Override
    public MetaClass getEntityMetaClass() {
        MetaClass metaClass = null;
        if (selectedItem != null) {
            metaClass = metadata.getClass(selectedItem);
        } else {
            List<E> itemsCollection = new ArrayList<>(getItemsCollection().values());
            if (!itemsCollection.isEmpty()) {
                metaClass = metadata.getClass(itemsCollection.get(0));
            } else {
                Type collectionType = itemsCollection.getClass().getGenericSuperclass();
                if (collectionType instanceof ParameterizedType
                        && ((ParameterizedType) collectionType).getActualTypeArguments().length > 0) {
                    Type entityType = ((ParameterizedType) collectionType).getActualTypeArguments()[0];
                    if (entityType instanceof Class) {
                        metaClass = metadata.getClass((Class<?>) entityType);
                    }
                }
            }
        }
        return metaClass;
    }
}
