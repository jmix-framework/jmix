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

package io.jmix.flowui.xml.facet.loader;

import io.jmix.flowui.facet.Facet;
import io.jmix.flowui.xml.layout.ComponentLoader;
import org.dom4j.Element;
import org.springframework.context.ApplicationContext;

/**
 * Defined the contract for loading and initializing facets (non-UI components) from XML descriptors.
 *
 * @param <F> the type of facet being loaded
 */
public interface FacetLoader<F extends Facet> {

    /**
     * Creates result facet by XML-element.
     */
    void initFacet();

    /**
     * Loads facet properties by XML definition.
     */
    void loadFacet();

    /**
     * @return result facet
     */
    F getResultFacet();

    /**
     * Sets the application context for the current component loader.
     *
     * @param applicationContext the {@link ApplicationContext} instance to set
     */
    void setApplicationContext(ApplicationContext applicationContext);

    /**
     * @return XML element associated with the facet loader
     */
    Element getElement();

    /**
     * Sets the specified XML element to be associated with the facet loader.
     *
     * @param element the {@link Element} instance representing the XML configuration
     *                for a facet component
     */
    void setElement(Element element);

    /**
     * Returns the context associated with the current facet loading and initialization process.
     *
     * @return the {@link ComponentLoader.ComponentContext} instance
     */
    ComponentLoader.ComponentContext getContext();

    /**
     * Sets the context for loading and initializing facet components.
     *
     * @param context the {@link ComponentLoader.ComponentContext} instance to set
     */
    void setContext(ComponentLoader.ComponentContext context);
}
