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

import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.facet.impl.UrlQueryParametersFacetImpl;
import io.jmix.flowui.facet.urlqueryparameters.UrlQueryParametersBinderProvider;
import io.jmix.flowui.view.navigation.RouteSupport;
import io.jmix.flowui.xml.layout.ComponentLoader.ComponentContext;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.apache.commons.collections4.CollectionUtils;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

@org.springframework.stereotype.Component("flowui_UrlQueryParametersFacetProvider")
public class UrlQueryParametersFacetProvider implements FacetProvider<UrlQueryParametersFacet> {

    protected LoaderSupport loaderSupport;
    protected RouteSupport routeSupport;

    protected List<UrlQueryParametersBinderProvider> binderProviders;

    public UrlQueryParametersFacetProvider(LoaderSupport loaderSupport,
                                           RouteSupport routeSupport,
                                           @Autowired(required = false)
                                           List<UrlQueryParametersBinderProvider> binderProviders) {
        this.loaderSupport = loaderSupport;
        this.routeSupport = routeSupport;
        this.binderProviders = CollectionUtils.isEmpty(binderProviders) ? Collections.emptyList() : binderProviders;
    }

    @Override
    public Class<UrlQueryParametersFacet> getFacetClass() {
        return UrlQueryParametersFacet.class;
    }

    @Override
    public UrlQueryParametersFacet create() {
        return new UrlQueryParametersFacetImpl(routeSupport);
    }

    @Override
    public String getFacetTag() {
        return UrlQueryParametersFacet.NAME;
    }

    @Override
    public void loadFromXml(UrlQueryParametersFacet facet, Element element, ComponentContext context) {
        facet.setOwner(context.getView());

        loaderSupport.loadString(element, "id", facet::setId);

        for (Element binderEl : element.elements()) {
            loadBinder(facet, binderEl, context);
        }
    }

    protected void loadBinder(UrlQueryParametersFacet facet, Element element, ComponentContext context) {
        for (UrlQueryParametersBinderProvider binderProvider : binderProviders) {
            if (binderProvider.supports(element)) {
                binderProvider.load(facet, element, context);
                return;
            }
        }

        throw new GuiDevelopmentException(
                String.format("Unsupported nested element in '%s': %s",
                        getFacetTag(), element.getName()), context);
    }
}
