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

import io.jmix.masquerade.sys.ByLocator;
import org.openqa.selenium.By;

import static io.jmix.masquerade.JSelectors.byChained;
import static io.jmix.masquerade.JSelectors.byPath;
import static io.jmix.masquerade.Masquerade.$j;

/**
 * Interface for container web-elements wrappers that have a child web-elements.
 */
public interface Container extends ByLocator {

    /**
     * @param childClazz child web-element wrapper class
     * @param childPath  path to find child web-element
     * @param <C>        type of the child web-element wrapper class
     * @return child web-element wrapper which was found by the passed
     * {@link io.jmix.masquerade.Masquerade#UI_TEST_ID UI_TEST_ID} attribute values path
     */
    default <C> C child(Class<C> childClazz, String... childPath) {
        return child(childClazz, byPath(childPath));
    }

    /**
     * @param childClazz child web-element wrapper class
     * @param childBy    {@link By} selector to find child web-element
     * @param <C>        type of the child web-element wrapper class
     * @return child web-element wrapper which was found by the passed {@link By} selector
     */
    default <C> C child(Class<C> childClazz, By childBy) {
        return $j(childClazz, byChained(getBy(), childBy));
    }
}