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
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.xml.facet.FacetProvider;
import io.jmix.flowui.xml.layout.ComponentLoader;
import jakarta.validation.constraints.NotNull;
import org.dom4j.Element;

@Internal
@org.springframework.stereotype.Component("dynat_DynamicAttributeFacetProvider")
public class DynAttrFacetProvider implements FacetProvider<DynAttrFacet> {

    protected final DynAttrEmbeddingStrategies embeddingStrategies;
    protected final AttributeDefaultValues attributeDefaultValues;

    public DynAttrFacetProvider(DynAttrEmbeddingStrategies embeddingStrategies, AttributeDefaultValues attributeDefaultValues) {
        this.embeddingStrategies = embeddingStrategies;
        this.attributeDefaultValues = attributeDefaultValues;
    }


    @Override
    @SuppressWarnings("NullableProblems")
    public Class<DynAttrFacet> getFacetClass() {
        return DynAttrFacet.class;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public DynAttrFacet create() {
        DynAttrFacetImpl dynAttrFacet = new DynAttrFacetImpl();
        dynAttrFacet.setAttributeDefaultValues(attributeDefaultValues);
        return dynAttrFacet;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public String getFacetTag() {
        return DynAttrFacet.FACET_NAME;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public void loadFromXml(DynAttrFacet facet, Element element, ComponentLoader.ComponentContext context) {
        context.addInitTask((context1, view) ->
                UiComponentUtils.walkComponents(view,
                        result -> embeddingStrategies.embedAttributes(result.getComponent(), view)));
    }
}
