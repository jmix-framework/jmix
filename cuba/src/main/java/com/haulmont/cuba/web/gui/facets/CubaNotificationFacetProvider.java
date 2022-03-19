/*
 * Copyright 2021 Haulmont.
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

import com.haulmont.cuba.gui.components.NotificationFacet;
import com.haulmont.cuba.web.gui.components.WebNotificationFacet;
import io.jmix.ui.facet.NotificationFacetProvider;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component("cuba_NotificationFacetProvider")
public class CubaNotificationFacetProvider extends NotificationFacetProvider {

    @Override
    public Class getFacetClass() {
        return NotificationFacet.class;
    }

    @Override
    public io.jmix.ui.component.NotificationFacet create() {
        return new WebNotificationFacet();
    }

    @Override
    protected void loadStyleName(io.jmix.ui.component.NotificationFacet facet, Element element) {
        String styleName = element.attributeValue("styleName");
        if (isNotEmpty(styleName)) {
            facet.setStyleName(styleName);
        }
    }
}
