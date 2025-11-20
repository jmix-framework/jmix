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

package io.jmix.flowui.xml.facet.loader;

import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.facet.urlqueryparameters.UrlQueryParametersBinderProvider;
import org.dom4j.Element;
import org.springframework.core.OrderComparator;

import java.util.List;

public class UrlQueryParametersFacetLoader extends AbstractFacetLoader<UrlQueryParametersFacet> {

    protected List<UrlQueryParametersBinderProvider> binderProviders;

    @Override
    protected UrlQueryParametersFacet createFacet() {
        UrlQueryParametersFacet facet = facets.create(UrlQueryParametersFacet.class);
        facet.setOwner(context.getView());
        return facet;
    }

    @Override
    public void loadFacet() {
        loaderSupport.loadString(element, "id", resultFacet::setId);

        for (Element binderElement : element.elements()) {
            loadBinder(binderElement);
        }
    }

    protected void loadBinder(Element element) {
        for (UrlQueryParametersBinderProvider binderProvider : getBinderProviders()) {
            if (binderProvider.supports(element)) {
                binderProvider.load(resultFacet, element, context);
                return;
            }
        }

        throw new GuiDevelopmentException(
                "Unsupported nested element in 'urlQueryParameters': %s".formatted(element.getName()), context
        );
    }

    protected List<UrlQueryParametersBinderProvider> getBinderProviders() {
        if (binderProviders == null) {
            binderProviders = applicationContext.getBeansOfType(UrlQueryParametersBinderProvider.class)
                    .values()
                    .stream()
                    .sorted(OrderComparator.INSTANCE)
                    .toList();
        }

        return binderProviders;
    }
}
