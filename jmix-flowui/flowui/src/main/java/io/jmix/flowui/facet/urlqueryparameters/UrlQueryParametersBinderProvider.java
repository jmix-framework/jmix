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

package io.jmix.flowui.facet.urlqueryparameters;

import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.xml.layout.ComponentLoader;
import org.dom4j.Element;

/**
 * Interface to be implemented by Spring beans that should load binder for specific component from
 * {@link UrlQueryParametersFacet} XML description.
 */
public interface UrlQueryParametersBinderProvider {

    /**
     * Checks whether the provided element can be loaded by provider.
     *
     * @param element element to check
     * @return {@code true} if element is supported
     */
    boolean supports(Element element);

    /**
     * Loads parameters from XML to create a binder that binds the parameters of the URL query
     * and the state of the component.
     * <p>
     * See example: {@link PaginationUrlQueryParametersBinderProvider}.
     *
     * @param facet   the facet
     * @param element element to load
     * @param context loader context
     */
    void load(UrlQueryParametersFacet facet, Element element, ComponentLoader.ComponentContext context);
}
