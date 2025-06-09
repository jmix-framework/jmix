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

import io.jmix.masquerade.sys.Composite;

import java.util.Arrays;

/**
 * POJO class to store menu item information.
 *
 * @param <T> menu item composite web-element wrapper class type
 */
public class MenuItem<T extends Composite<T>> {

    protected Class<T> viewClass;
    protected String[] path;

    public MenuItem(Class<T> viewClass, String... path) {
        this.viewClass = viewClass;
        this.path = path;
    }

    /**
     * @return menu item composite web-element wrapper class
     */
    public Class<T> getViewClass() {
        return viewClass;
    }

    /**
     * @return path to menu item web-element
     */
    public String[] getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "Menu{" +
                "viewClass=" + viewClass +
                ", path=" + Arrays.toString(path) +
                '}';
    }
}
