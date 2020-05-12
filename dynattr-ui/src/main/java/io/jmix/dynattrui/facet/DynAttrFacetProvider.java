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

package io.jmix.dynattrui.facet;

import io.jmix.ui.xml.FacetProvider;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.dom4j.Element;

import javax.inject.Inject;

@org.springframework.stereotype.Component(DynAttrFacetProvider.NAME)
public class DynAttrFacetProvider implements FacetProvider<DynAttrFacet> {
    public static final String NAME = "dynattrui_DynamicAttributeFacetProvider";

    @Inject
    protected DynAttrInitTask initTask;

    @Override
    public Class<DynAttrFacet> getFacetClass() {
        return DynAttrFacet.class;
    }

    @Override
    public DynAttrFacet create() {
        return new WebDynAttrFacet();
    }

    @Override
    public String getFacetTag() {
        return "dynamicAttributes";
    }

    @Override
    public void loadFromXml(DynAttrFacet facet, Element element, ComponentLoader.ComponentContext context) {
        context.addInitTask(initTask);
    }
}
