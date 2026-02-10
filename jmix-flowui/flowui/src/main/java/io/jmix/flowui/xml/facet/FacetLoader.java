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

package io.jmix.flowui.xml.facet;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.jmix.core.JmixModulesAwareBeanSelector;
import io.jmix.flowui.Facets;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.facet.Facet;
import io.jmix.flowui.xml.layout.ComponentLoader.ComponentContext;
import org.dom4j.Element;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

@Component("flowui_FacetLoader")
public class FacetLoader {

    protected Multimap<String, FacetProvider<?>> registrations = HashMultimap.create();
    protected FacetLoaderResolver facetLoaderResolver;

    protected ApplicationContext applicationContext;
    protected JmixModulesAwareBeanSelector beanSelector;
    protected Facets facets;

    public FacetLoader(JmixModulesAwareBeanSelector beanSelector, Facets facets) {
        this.beanSelector = beanSelector;
        this.facets = facets;
    }

    @Autowired(required = false)
    protected void setFacetRegistrations(List<FacetProvider<?>> facetProviders) {
        for (FacetProvider<?> facetProvider : facetProviders) {
            registrations.put(facetProvider.getFacetTag(), facetProvider);
        }
    }

    @Autowired
    protected void setFacetLoaderResolver(FacetLoaderResolver facetLoaderResolver) {
        this.facetLoaderResolver = facetLoaderResolver;
    }

    @Autowired
    protected void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Creates and loads a facet from XML by the passed XML element.
     *
     * @param element element to create a facet from
     * @param context loading context
     * @return loaded facet
     */
    public Facet load(Element element, ComponentContext context) {
        io.jmix.flowui.xml.facet.loader.FacetLoader<?> facetLoader = getLoader(element, context);
        if (facetLoader == null) {
            // fallback
            return _load(element, context);
        }

        facetLoader.initFacet();
        facetLoader.loadFacet();
        return facetLoader.getResultFacet();
    }

    protected io.jmix.flowui.xml.facet.loader.@Nullable FacetLoader<?> getLoader(Element element,
                                                                                 ComponentContext context) {
        Class<? extends io.jmix.flowui.xml.facet.loader.FacetLoader<?>> loaderClass =
                facetLoaderResolver.getLoader(element);

        if (loaderClass == null) {
            return null;
        }

        return initLoader(element, loaderClass, context);
    }

    protected io.jmix.flowui.xml.facet.loader.FacetLoader<?> initLoader(
            Element element,
            Class<? extends io.jmix.flowui.xml.facet.loader.FacetLoader<?>> loaderClass,
            ComponentContext context
    ) {
        Constructor<? extends io.jmix.flowui.xml.facet.loader.FacetLoader<?>> constructor;

        try {
            constructor = loaderClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new GuiDevelopmentException("Unable to get constructor for facet loader: " + e, context);
        }

        io.jmix.flowui.xml.facet.loader.FacetLoader<?> loader;
        try {
            loader = constructor.newInstance();
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new GuiDevelopmentException("Loader instantiation error: " + e, context);
        }

        loader.setApplicationContext(applicationContext);
        loader.setContext(context);
        loader.setElement(element);

        return loader;
    }

    // for backward compatibility
    @Deprecated(since = "3.0", forRemoval = true)
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Facet _load(Element element, ComponentContext context) {
        Collection<FacetProvider<?>> facetProviders = registrations.get(element.getName());
        FacetProvider facetProvider = beanSelector.selectFrom(facetProviders);
        if (facetProvider == null) {
            throw new IllegalArgumentException("There is no facet for XML tag " + element.getName());
        }

        Facet facet = facets.create(facetProvider.getFacetClass());
        facetProvider.loadFromXml(facet, element, context);

        return facet;
    }
}
