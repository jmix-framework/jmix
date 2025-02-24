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

package io.jmix.tabbedmode.xml.facet;

import io.jmix.core.JmixOrder;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.xml.facet.FacetProvider;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.support.LoaderSupport;
import org.dom4j.Element;
import org.springframework.core.annotation.Order;

//@Primary
@Order(JmixOrder.LOWEST_PRECEDENCE - 100)
@org.springframework.stereotype.Component("tabmod_TabbedModeUrlQueryParametersFacetProvider")
public class TabbedModeUrlQueryParametersFacetProvider implements FacetProvider<UrlQueryParametersFacet> {

    protected final LoaderSupport loaderSupport;

    public TabbedModeUrlQueryParametersFacetProvider(LoaderSupport loaderSupport) {
        this.loaderSupport = loaderSupport;
    }

    @Override
    public String getFacetTag() {
        return UrlQueryParametersFacet.NAME;
    }

    @Override
    public UrlQueryParametersFacet create() {
        return new NoopUrlQueryParametersFacetImpl();
    }

    @Override
    public Class<UrlQueryParametersFacet> getFacetClass() {
        return UrlQueryParametersFacet.class;
    }

    @Override
    public void loadFromXml(UrlQueryParametersFacet facet, Element element, ComponentLoader.ComponentContext context) {
        facet.setOwner(context.getView());

        loaderSupport.loadString(element, "id", facet::setId);
    }
}
