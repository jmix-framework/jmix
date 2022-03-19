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

package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.DatasourceComponent;
import com.haulmont.cuba.gui.components.Field;
import com.haulmont.cuba.gui.components.HasCaptionMode;
import com.haulmont.cuba.gui.components.PickerField;
import com.haulmont.cuba.gui.components.SuggestionPickerField;
import com.haulmont.cuba.gui.xml.data.ComponentLoaderHelper;
import com.haulmont.cuba.gui.xml.data.DatasourceLoaderHelper;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.ActionsHolder;
import io.jmix.ui.component.EntitySuggestionField;
import io.jmix.ui.xml.layout.loader.EntitySuggestionFieldLoader;
import org.dom4j.Element;

import java.util.Optional;

import static com.haulmont.cuba.gui.xml.data.ComponentLoaderHelper.loadInvokeAction;
import static com.haulmont.cuba.gui.xml.data.ComponentLoaderHelper.loadLegacyPickerAction;
import static org.apache.commons.lang3.StringUtils.trimToNull;

public class SuggestionPickerFieldLoader extends EntitySuggestionFieldLoader {

    @Override
    public void createComponent() {
        UiComponents uiComponents = applicationContext.getBean(UiComponents.class);
        resultComponent = uiComponents.create(SuggestionPickerField.NAME);
        loadId(resultComponent, element);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void loadComponent() {
        super.loadComponent();

        ComponentLoaderHelper.loadValidators((Field) resultComponent, element, context, getClassManager(), getMessages());
    }

    @Override
    public SuggestionPickerField getResultComponent() {
        return (SuggestionPickerField) super.getResultComponent();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void loadData(EntitySuggestionField component, Element element) {
        super.loadData(component, element);

        DatasourceLoaderHelper
                .loadDatasourceIfValueSourceNull((DatasourceComponent) resultComponent, element, context,
                        (ComponentLoaderContext) getComponentContext())
                .ifPresent(component::setValueSource);
    }

    @Override
    protected void addDefaultActions() {
        if (ComponentLoaderHelper.isLegacyFrame(context)) {
            getResultComponent().addLookupAction();
            getResultComponent().addOpenAction();
        } else {
            super.addDefaultActions();
        }
    }

    @Override
    protected Action loadDeclarativeAction(ActionsHolder actionsHolder, Element element) {
        String id = loadActionId(element);
        Optional<Action> actionOpt = loadLegacyPickerAction(((PickerField) actionsHolder), element, context, id);
        if (actionOpt.isPresent()) {
            return actionOpt.get();
        }

        actionOpt = loadInvokeAction(
                context,
                actionsHolder,
                element,
                loadActionId(element),
                loadResourceString(element.attributeValue("caption")),
                loadResourceString(element.attributeValue("description")),
                getIconPath(element.attributeValue("icon")),
                loadShortcut(trimToNull(element.attributeValue("shortcut"))));

        return actionOpt.orElseGet(() ->
                super.loadDeclarativeAction(actionsHolder, element));
    }

    @Override
    protected void loadCaptionProperty(EntitySuggestionField suggestionField, Element element) {
        ComponentLoaderHelper.loadCaptionProperty((HasCaptionMode) suggestionField, element);
    }

    @Override
    protected String loadFetchPlan(Element queryElement) {
        return queryElement.attributeValue("view");
    }
}
