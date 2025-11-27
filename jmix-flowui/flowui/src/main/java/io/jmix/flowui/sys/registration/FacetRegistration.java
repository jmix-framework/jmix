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
import org.springframework.lang.Nullable;

/**
 * Interface representing the registration of a new facet or override an existing facet.
 *
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
 * @see FacetRegistrationBuilder
 * @see CustomFacetsRegistry
 */
public interface FacetRegistration {

    /**
     * @return facet interface class
     */
    Class<? extends Facet> getFacet();

    /**
     * @param <T> type of the new facet class
     * @return facet class that should be replaced by {@link #getFacet()} or {@code null} if not set
     */
    @Nullable
    <T extends Facet> Class<T> getReplacedFacet();

    /**
     * @return facet tag
     */
    @Nullable
    String getTag();

    /**
     * @return facet loader class
     */
    @Nullable
    Class<? extends FacetLoader<?>> getFacetLoader();
}
