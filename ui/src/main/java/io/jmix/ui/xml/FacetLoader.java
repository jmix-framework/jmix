/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.xml;

import io.jmix.ui.component.Facet;
import io.jmix.ui.Facets;
import io.jmix.ui.xml.layout.ComponentLoader.ComponentContext;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Component(FacetLoader.NAME)
@ParametersAreNonnullByDefault
public class FacetLoader {

    public static final String NAME = "jmix_FacetLoader";

    protected Map<String, FacetProvider> registrations = new HashMap<>();

    @Inject
    protected Facets facets;

    @Autowired(required = false)
    protected void setFacetRegistrations(List<FacetProvider> registrations) {
        this.registrations = registrations.stream()
                .collect(toMap(FacetProvider::getFacetTag, identity()));
    }

    @Nonnull
    public Facet load(Element element, ComponentContext context) {
        @SuppressWarnings("unchecked")
        FacetProvider<Facet> facetProvider = registrations.get(element.getName());
        if (facetProvider == null) {
            throw new IllegalArgumentException("There is no such facet for XML tag " + element.getName());
        }

        Facet facet = facets.create(facetProvider.getFacetClass());
        facetProvider.loadFromXml(facet, element, context);

        return facet;
    }
}