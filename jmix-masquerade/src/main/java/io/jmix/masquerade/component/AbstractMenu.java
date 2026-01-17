/*
 * Copyright 2025 Haulmont.
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

package io.jmix.masquerade.component;

import io.jmix.masquerade.Masquerade;
import io.jmix.masquerade.sys.Composite;
import org.openqa.selenium.By;

import static io.jmix.masquerade.Masquerade.$j;

/**
 * Abstract class for menu web-element wrappers. Supports opening menu items by {@link Masquerade#UI_TEST_ID UI_TEST_ID}
 * path.
 *
 * @param <M> inheritor menu class type
 */
public abstract class AbstractMenu<M> extends AbstractComponent<M> {

    protected AbstractMenu(By by) {
        super(by);
    }

    /**
     * Clicks on the menu items in sequence along the passed path and returns the open composite (view).
     *
     * @param compositeClass composite class to wire and return
     * @param path           path of the menu items {@link Masquerade#UI_TEST_ID UI_TEST_ID} attributes
     * @param <T>            composite class type
     * @return wired web-element wrapper for opened composite
     */
    public <T extends Composite<T>> T openItem(Class<T> compositeClass, String... path) {
        openItem(path);
        return $j(compositeClass);
    }

    /**
     * Clicks on the menu items in sequence along the passed {@link MenuItem} and returns the open composite (view).
     *
     * @param menuItem menu item information object
     * @param <T>      composite class type
     * @return wired web-element wrapper for opened composite
     */
    public <T extends Composite<T>> T openItem(MenuItem<T> menuItem) {
        openItem(menuItem.getPath());
        return $j(menuItem.getViewClass());
    }

    /**
     * Clicks on the menu items in sequence along the passed path
     *
     * @param path path of menu items
     */
    abstract public void openItem(String... path);
}
