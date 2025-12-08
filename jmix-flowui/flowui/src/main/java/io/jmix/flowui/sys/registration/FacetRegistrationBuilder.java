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

package io.jmix.flowui.sys.registration;

import io.jmix.flowui.facet.Facet;
import io.jmix.flowui.xml.facet.loader.FacetLoader;

/**
 * Builds registration object used for adding new facets registration or overriding UI facets in the framework.
 * <p>

 * For example:
 * <pre>{@code
 * @Configuration
 * public class FacetConfiguration {
 *
 *     @Bean
 *     public FacetRegistration extTimerFacet() {
 *         return FacetRegistrationBuilder.create(ExtTimerFacetImpl.class)
 *                 .replaceFacet(Timer.class)
 *                 .withFacetLoader("timer", ExtTimerFacetLoader.class)
 *                 .build();
 *     }
 * }
 * }</pre>
 *
 * @see FacetRegistration
 * @see CustomFacetsRegistry
 */
public class FacetRegistrationBuilder {

    protected Class<? extends Facet> facet;
    protected Class<? extends Facet> replacedFacet;
    protected String tag;
    protected Class<? extends FacetLoader<?>> facetLoader;

    protected FacetRegistrationBuilder(Class<? extends Facet> facet) {
        this.facet = facet;
    }

    /**
     * Creates a builder instance.
     *
     * @param facet facet class to register
     * @return builder instance
     */
    public static FacetRegistrationBuilder create(Class<? extends Facet> facet) {
        return new FacetRegistrationBuilder(facet);
    }

    /**
     * Sets the facet class that should be replaced.
     *
     * @param facet facet class to replace
     * @return builder instance
     */
    public FacetRegistrationBuilder replaceFacet(Class<? extends Facet> facet) {
        replacedFacet = facet;
        return this;
    }

    /**
     * Sets facet loader class.
     *
     * @param tag         facet name in the view XML descriptor
     * @param facetLoader component loader class
     * @return builder instance
     */
    public FacetRegistrationBuilder withFacetLoader(String tag, Class<? extends FacetLoader<?>> facetLoader) {
        this.tag = tag;
        this.facetLoader = facetLoader;
        return this;
    }

    /**
     * @return instance of a registration object
     */
    public FacetRegistration build() {
        return new FacetRegistrationImpl(facet, replacedFacet, tag, facetLoader);
    }
}
