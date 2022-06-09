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

package io.jmix.flowui;

import io.jmix.flowui.facet.Facet;
import org.springframework.context.ApplicationContext;

/**
 * Factory to create UI facets.
 * <br>
 * An instance of the factory can be injected into screen controllers or obtained through {@link ApplicationContext}.
 */
public interface Facets {

    /**
     * Create a facet instance by its class.
     *
     * @param facetClass facet class
     * @param <T>        type of facet
     * @return facet instance
     */
    <T extends Facet> T create(Class<T> facetClass);
}
