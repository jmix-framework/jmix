/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.xml.layout.loader;

import io.jmix.ui.Actions;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.entitypicker.EntityLookupAction;
import io.jmix.ui.action.entitypicker.EntityOpenAction;
import io.jmix.ui.component.ActionsHolder;
import io.jmix.ui.component.EntitySuggestionField;
import org.dom4j.Element;

public class EntitySuggestionFieldLoader extends AbstractSuggestionFieldLoader<EntitySuggestionField> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(EntitySuggestionField.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadTabIndex(resultComponent, element);

        loadMetaClass(element, resultComponent::setMetaClass);

        loadCaptionProperty(resultComponent, element);

        loadActions(resultComponent);

        loadAsyncSearchDelayMs(resultComponent, element);
        loadMinSearchStringLength(resultComponent, element);
        loadSuggestionsLimit(resultComponent, element);

        loadInputPrompt(resultComponent, element);

        loadPopupWidth(resultComponent, element);

        loadQuery(resultComponent, element);

        loadFormatter(resultComponent, element);
    }

    protected void loadActions(EntitySuggestionField suggestionField) {
        loadActions(suggestionField, element);
        if (suggestionField.getActions().isEmpty()) {
            addDefaultActions();
        }
    }

    protected void addDefaultActions() {
        Actions actions = getActions();

        getResultComponent().addAction(actions.create(EntityLookupAction.ID));
        getResultComponent().addAction(actions.create(EntityOpenAction.ID));
    }

    @Override
    protected Action loadDeclarativeAction(ActionsHolder actionsHolder, Element element) {
        return loadValuePickerDeclarativeAction(actionsHolder, element);
    }
}
