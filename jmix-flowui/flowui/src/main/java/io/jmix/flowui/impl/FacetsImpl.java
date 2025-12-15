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

package io.jmix.flowui.impl;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.di.Instantiator;
import io.jmix.flowui.Facets;
import io.jmix.flowui.facet.*;
import io.jmix.flowui.facet.Timer;
import io.jmix.flowui.facet.impl.*;
import io.jmix.flowui.xml.facet.FacetProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the {@link Facets} interface responsible for creating and managing UI facets.
 */
@Component("flowui_Facets")
public class FacetsImpl implements Facets, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(FacetsImpl.class);

    protected ApplicationContext applicationContext;

    protected Map<Class<? extends Facet>, FacetProvider> registrations = new HashMap<>();
    protected Set<FacetInfo> facets = ConcurrentHashMap.newKeySet();

    {
        register(DataLoadCoordinatorImpl.class, DataLoadCoordinator.class);
        register(UrlQueryParametersFacetImpl.class, UrlQueryParametersFacet.class);
        register(TimerImpl.class, Timer.class);

        // use view settings by default
        register(ViewSettingsFacetImpl.class, SettingsFacet.class);
        register(ViewSettingsFacetImpl.class, ViewSettingsFacet.class);
        register(FragmentSettingsFacetImpl.class, FragmentSettingsFacet.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @SuppressWarnings("unchecked")
    @Deprecated(since = "3.0", forRemoval = true)
    @Nullable
    public <T extends Facet> FacetProvider<T> getProvider(Class<T> facetClass) {
        return registrations.get(facetClass);
    }

    @Deprecated(since = "3.0", forRemoval = true)
    @Autowired(required = false)
    protected void setFacetRegistrations(List<FacetProvider<?>> facetProviders) {
        for (FacetProvider<?> facetProvider : facetProviders) {
            this.registrations.putIfAbsent(facetProvider.getFacetClass(), facetProvider);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Facet> T create(Class<T> facetClass) {
        Class<? extends Facet> facetToCreate = facetClass;

        Optional<FacetInfo> facetInfo = getFacetInfo(facetClass);
        if (facetInfo.isPresent()) {
            facetToCreate = getFacetToCreate(facetInfo.get());
        } else {
            // for backward compatibility
            FacetProvider<T> registration = registrations.get(facetClass);
            if (registration != null) {
                return registration.create();
            }
        }

        log.trace("Creating {} facet", facetToCreate.getName());

        return (T) Instantiator.get(UI.getCurrent()).getOrCreate(facetToCreate);
    }

    public void register(Class<? extends Facet> facetClass, Class<? extends Facet> replacedFacet) {
        if (getFacetInfo(facetClass).isPresent()) {
            log.trace("Facet with `{}` class has already registered", facetClass);
            return;
        }

        FacetInfo replacedFacetInfo = getFacetInfo(replacedFacet)
                .orElseGet(() -> {
                    FacetInfo facetInfo = new FacetInfo(replacedFacet, null);
                    facets.add(facetInfo);
                    return facetInfo;
                });

        FacetInfo facetInfo = new FacetInfo(facetClass, replacedFacetInfo);
        facets.add(facetInfo);

        replacedFacetInfo.setReplacement(facetInfo);
    }

    protected Optional<FacetInfo> getFacetInfo(Class<? extends Facet> facetClass) {
        return facets.stream()
                .filter(info -> info.getOriginal().equals(facetClass))
                .findAny();
    }

    protected Class<? extends Facet> getFacetToCreate(FacetInfo facetInfo) {
        FacetInfo currentReplacement = facetInfo.getReplacement();
        if (currentReplacement == null) {
            return facetInfo.getOriginal();
        }

        Class<? extends Facet> typeToCreate = currentReplacement.getOriginal();

        while (currentReplacement != null) {
            FacetInfo replacement = currentReplacement.getReplacement();
            if (replacement == null) {
                typeToCreate = currentReplacement.getOriginal();
            }
            currentReplacement = replacement;
        }

        return typeToCreate;
    }

    /**
     * POJO class to store information about replaced facets.
     */
    protected static class FacetInfo {

        protected Class<? extends Facet> original;
        protected FacetInfo replacedFacet;
        protected FacetInfo replacement;

        public FacetInfo(Class<? extends Facet> original, @Nullable FacetInfo replacedFacet) {
            this.original = original;
            this.replacedFacet = replacedFacet;
        }

        /**
         * @return the {@link #original} facet
         */
        public Class<? extends Facet> getOriginal() {
            return original;
        }

        /**
         * @return the facet that should be replaced by {@link #original} or {@code null} if not set
         */
        @Nullable
        public FacetInfo getReplacedFacet() {
            return replacedFacet;
        }

        /**
         * @return the facet that should be created for the {@link #original} or {@code null} if not set
         */
        @Nullable
        public FacetInfo getReplacement() {
            return replacement;
        }

        /**
         * Sets the facet that should be created for the {@link #original}.
         *
         * @param replacement replacement
         */
        public void setReplacement(@Nullable FacetInfo replacement) {
            this.replacement = replacement;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }

            FacetInfo facetInfo = (FacetInfo) obj;
            return this.original.equals(facetInfo.getOriginal());
        }

        @Override
        public int hashCode() {
            return original.hashCode();
        }

        @Override
        public String toString() {
            return "{\"original\": \"%s\"}".formatted(original.getName());
        }
    }
}
