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

import io.jmix.core.common.event.Subscription;
import io.jmix.flowui.kit.component.menu.MenuItem;

import java.util.Collection;
import java.util.EventObject;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents menu item source which can be bound to a menu
 *
 * @param <T> menu item type
 */
public interface MenuItemProvider<T extends MenuItem> {

    /**
     * Loads menu items.
     */
    void load();

    /**
     * Adds a listener for an event of menu item collection change.
     *
     * @param listener a listener to add
     * @return subscription for the listener
     */
    Subscription addCollectionChangedListener(Consumer<CollectionChangeEvent<T>> listener);

    /**
     * @return loaded menu items.
     */
    List<T> getMenuItems();

    /**
     * Adds a transform function which will be applied to menu items after load. It can be used to add some
     * customizations to loaded items (for example, expand or collapse specific items).
     *
     * @param itemsTransformer a transform function to add
     */
    void addMenuItemsTransformer(Function<List<T>, List<T>> itemsTransformer);

    /**
     * Removes menu item transform function
     *
     * @param transformer transform function to remove
     * @see #addMenuItemsTransformer(Function)
     */
    void removeMenuItemsTransformer(Function<List<T>, List<T>> transformer);

    /**
     * Menu item collection change event.
     *
     * @param <T> menu item type
     */
    class CollectionChangeEvent<T> extends EventObject {

        protected Collection<? extends T> items;

        public CollectionChangeEvent(Object source, Collection<? extends T> items) {
            super(source);
            this.items = items;
        }

        /**
         * @return new menu item collection
         */
        public Collection<? extends T> getItems() {
            return items;
        }
    }
}
