/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.web.gui.facets;

import com.haulmont.cuba.gui.components.OptionDialogFacet;
import com.haulmont.cuba.web.gui.components.WebOptionDialogFacet;
import io.jmix.ui.facet.OptionDialogFacetProvider;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component("cuba_OptionDialogFacetProvider")
public class CubaOptionDialogFacetProvider extends OptionDialogFacetProvider {

    @Override
    public Class getFacetClass() {
        return OptionDialogFacet.class;
    }

    @Override
    public OptionDialogFacet create() {
        return new WebOptionDialogFacet();
    }

    @Override
    public void loadFromXml(io.jmix.ui.component.OptionDialogFacet facet, Element element, ComponentLoader.ComponentContext context) {
        super.loadFromXml(facet, element, context);

        if (facet instanceof OptionDialogFacet) {
            loadMaximized((OptionDialogFacet) facet, element);
        }
    }

    protected void loadMaximized(OptionDialogFacet facet, Element element) {
        String maximized = element.attributeValue("maximized");
        if (isNotEmpty(maximized)) {
            facet.setMaximized(Boolean.parseBoolean(maximized));
        }
    }

    @Override
    protected void loadStyleName(io.jmix.ui.component.OptionDialogFacet facet, Element element) {
        String styleName = element.attributeValue("styleName");
        if (isNotEmpty(styleName)) {
            facet.setStyleName(styleName);
        }
    }
}
