/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.menu.provider;

import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.flowui.kit.component.menu.MenuItem;
import io.jmix.flowui.menu.MenuConfig;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Provides base functionality for menu item providers which use MenuConfig as menu item source
 * @param <T> menu item type
 */
public abstract class MenuConfigMenuItemProvider<T extends MenuItem>
        implements MenuItemProvider<T> {

    protected MenuConfig menuConfig;
    protected List<Function<List<T>, List<T>>> transformers;
    protected List<T> menuItems = new ArrayList<>();
    protected EventHub events = new EventHub();


    public MenuConfigMenuItemProvider(MenuConfig menuConfig) {
        this.menuConfig = menuConfig;
    }

    @Override
    public void load() {
        List<T> menuItems = convertToMenuItems(menuConfig.getRootItems());

        menuItems = transformItems(menuItems);

        events.publish(CollectionChangeEvent.class, new CollectionChangeEvent<>(this, menuItems));
    }

    /**
     * Converts menu config item descriptors to menu items of specific implementation
     * @param menuConfigItems menu config item descriptors
     * @return menu items of specific implementation
     */
    protected abstract List<T> convertToMenuItems(Collection<io.jmix.flowui.menu.MenuItem> menuConfigItems);

    protected List<T> transformItems(List<T> menuItems) {
        if (CollectionUtils.isEmpty(transformers)) {
            return menuItems;
        } else {
            List<T> transformedItems = menuItems;
            for (Function<List<T>, List<T>> transformer : transformers) {
                transformedItems = transformer.apply(transformedItems);
            }
            return transformedItems;
        }
    }

    @Override
    public Subscription addCollectionChangedListener(Consumer<CollectionChangeEvent<T>> listener) {
        //noinspection unchecked,rawtypes
        return events.subscribe(CollectionChangeEvent.class, (Consumer) listener);
    }

    @Override
    public List<T> getMenuItems() {
        return Collections.unmodifiableList(menuItems);
    }

    @Override
    public void addMenuItemsTransformer(Function<List<T>, List<T>> itemsTransformer) {
        if (transformers == null) {
            transformers = new ArrayList<>();
        }
        transformers.add(itemsTransformer);
    }

    @Override
    public void removeMenuItemsTransformer(Function<List<T>, List<T>> transformer) {
        if (transformers != null) {
            transformers.remove(transformer);
        }
    }
}
