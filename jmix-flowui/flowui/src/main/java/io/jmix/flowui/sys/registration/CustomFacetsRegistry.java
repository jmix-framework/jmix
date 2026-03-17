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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.jmix.core.JmixOrder;
import io.jmix.flowui.facet.Facet;
import io.jmix.flowui.impl.FacetsImpl;
import io.jmix.flowui.xml.facet.loader.FacetLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * A registry for managing custom facets tags and their corresponding loaders.
 * <p>
 * For instance, in the spring {@link Configuration} class create {@link FacetRegistration} bean.
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
 * @see FacetRegistration
 */
@Component("flowui_CustomFacetsRegistry")
public class CustomFacetsRegistry {

    private static final Logger log = LoggerFactory.getLogger(CustomFacetsRegistry.class);

    protected List<FacetRegistration> facetRegistrations;
    protected CustomFacetsLoaderConfig customFacetsLoaderConfig;
    protected FacetsImpl facets;

    public CustomFacetsRegistry(@Nullable List<FacetRegistration> facetRegistrations,
                                CustomFacetsLoaderConfig customFacetsLoaderConfig,
                                FacetsImpl facets) {
        this.facetRegistrations = facetRegistrations;
        this.customFacetsLoaderConfig = customFacetsLoaderConfig;
        this.facets = facets;
    }

    @EventListener
    @Order(JmixOrder.HIGHEST_PRECEDENCE + 100)
    public void init(ContextRefreshedEvent event) {
        registerComponents();
    }

    protected void registerComponents() {
        if (CollectionUtils.isEmpty(facetRegistrations)) {
            return;
        }

        for (FacetRegistration registration : Lists.reverse(facetRegistrations)) {
            registerFacet(registration);
        }
    }

    protected void registerFacet(@Nullable FacetRegistration registration) {
        if (registration == null) {
            return;
        }

        Class<? extends Facet> facet = registration.getFacet();
        Class<? extends Facet> replacedFacet = registration.getReplacedFacet();
        String tag = registration.getTag() != null ? registration.getTag().trim() : null;
        Class<? extends FacetLoader<?>> facetLoader = registration.getFacetLoader();

        if (facetLoader == null
                && Strings.isNullOrEmpty(tag)
                && replacedFacet == null) {
            throw new IllegalArgumentException(String.format("You have to provide at least replaced facet class"
                    + " or tag with facetLoader class for custom facet %s", facet));
        }

        if (replacedFacet != null) {
            log.trace("Register facet {} class provider {}", facet, replacedFacet.getCanonicalName());

            facets.register(facet, replacedFacet);
        }

        if (facetLoader != null && !Strings.isNullOrEmpty(tag)) {
            log.trace("Register tag {} loader {}", tag, facetLoader.getCanonicalName());

            customFacetsLoaderConfig.registerLoader(tag, facetLoader);
        }
    }
}
