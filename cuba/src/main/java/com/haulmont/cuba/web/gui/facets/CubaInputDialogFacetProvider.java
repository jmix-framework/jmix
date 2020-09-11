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

import com.haulmont.cuba.web.gui.components.CubaInputDialogFacet;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.app.inputdialog.DialogActions;
import io.jmix.ui.component.InputDialogFacet;
import io.jmix.ui.facet.InputDialogFacetProvider;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component(CubaInputDialogFacetProvider.NAME)
public class CubaInputDialogFacetProvider extends InputDialogFacetProvider {

    public static final String NAME = "cuba_InputDialogFacetProvider";

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Class getFacetClass() {
        return CubaInputDialogFacet.class;
    }

    @Override
    public InputDialogFacet create() {
        return new CubaInputDialogFacet();
    }

    @Override
    protected void loadDialogActions(InputDialogFacet facet, Element element, ComponentLoader.ComponentContext context) {
        loadDialogActions(facet, element, "dialogActions");
        loadDialogActions(facet, element, "defaultActions"); // for backward compatibility

        Element actions = element.element("actions");
        if (actions != null) {
            if (facet.getDialogActions() == null) {
                loadActions(facet, element, context);
            } else {
                throw new GuiDevelopmentException(
                        "Predefined and custom actions cannot be used for InputDialog at the same time",
                        context);
            }
        }
    }

    protected void loadDialogActions(InputDialogFacet facet, Element element, String attributeName) {
        String actions = element.attributeValue(attributeName);
        if (isNotEmpty(actions)) {
            facet.setDialogActions(DialogActions.valueOf(actions));
        }
    }
}
