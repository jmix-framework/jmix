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

package io.jmix.flowui.xml.facet;

import org.dom4j.Element;
import io.jmix.flowui.xml.facet.loader.FacetLoader;

/**
 * Configuration interface for {@link FacetLoader} implementations.
 */
public interface FacetLoaderConfig {

    /**
     * Checks whether the config contains a loader that supports the given {@code element}.
     *
     * @param element element to check
     * @return {@code true} if the config contains suitable loader, {@code false} otherwise
     */
    boolean supports(Element element);

    /**
     * @param element element to load
     * @return {@link FacetLoader} instance for loading the given {@code element}
     */
    Class<? extends FacetLoader<?>> getLoader(Element element);
}
