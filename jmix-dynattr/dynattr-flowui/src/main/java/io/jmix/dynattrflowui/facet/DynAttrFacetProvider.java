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

package io.jmix.dynattrflowui.facet;

import io.jmix.core.annotation.Internal;
import io.jmix.dynattrflowui.DynAttrEmbeddingStrategies;
import io.jmix.dynattrflowui.impl.AttributeDefaultValues;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.xml.FacetProvider;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

@Internal
@org.springframework.stereotype.Component("dynat_DynamicAttributeFacetProvider")
public class DynAttrFacetProvider implements FacetProvider<DynAttrFacet> {

    @Autowired
    protected DynAttrEmbeddingStrategies embeddingStrategies;

    @Autowired
    protected AttributeDefaultValues attributeDefaultValues;

    @Override
    public Class<DynAttrFacet> getFacetClass() {
        return DynAttrFacet.class;
    }

    @Override
    public DynAttrFacet create() {
        DynAttrFacetImpl dynAttrFacet = new DynAttrFacetImpl();
        dynAttrFacet.setAttributeDefaultValues(attributeDefaultValues);
        return dynAttrFacet;
    }

    @Override
    public String getFacetTag() {
        return DynAttrFacet.FACET_NAME;
    }

    @Override
    public void loadFromXml(DynAttrFacet facet, Element element, ComponentLoader.ComponentContext context) {
        context.addInitTask((context1, window) ->
                ComponentsHelper.walkComponents(window,
                        (component, name) -> embeddingStrategies.embedAttributes(component, context1.getFrame())));
    }
}
