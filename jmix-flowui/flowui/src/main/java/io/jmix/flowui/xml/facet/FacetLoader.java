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
import io.jmix.flowui.facet.Facet;
import io.jmix.flowui.xml.layout.ComponentLoader.ComponentContext;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component("flowui_FacetLoader")
public class FacetLoader {

    protected Multimap<String, FacetProvider<?>> registrations = HashMultimap.create();

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

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Facet load(Element element, ComponentContext context) {
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
