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

import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.ui.Actions;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.entitypicker.LookupAction;
import io.jmix.ui.action.entitypicker.OpenAction;
import io.jmix.ui.component.ActionsHolder;
import io.jmix.ui.component.EntitySuggestionField;
import io.jmix.ui.component.compatibility.CaptionAdapter;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

public class EntitySuggestionFieldLoader extends SuggestionFieldQueryLoader<EntitySuggestionField> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(EntitySuggestionField.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadTabIndex(resultComponent, element);

        loadMetaClass(resultComponent, element);

        loadCaptionProperty(resultComponent, element);

        loadActions(resultComponent);

        loadAsyncSearchDelayMs(resultComponent, element);
        loadMinSearchStringLength(resultComponent, element);
        loadSuggestionsLimit(resultComponent, element);

        loadInputPrompt(resultComponent, element);

        loadPopupWidth(resultComponent, element);

        loadQuery(resultComponent, element);
    }

    protected Metadata getMetadata() {
        return (Metadata) applicationContext.getBean(Metadata.NAME);
    }

    protected void loadPopupWidth(EntitySuggestionField suggestionField, Element element) {
        String popupWidth = element.attributeValue("popupWidth");
        if (StringUtils.isNotEmpty(popupWidth)) {
            suggestionField.setPopupWidth(popupWidth);
        }
    }

    protected void loadCaptionProperty(EntitySuggestionField suggestionField, Element element) {
        String captionProperty = element.attributeValue("captionProperty");
        if (!StringUtils.isEmpty(captionProperty)) {
            suggestionField.setOptionCaptionProvider(
                    new CaptionAdapter(captionProperty, applicationContext.getBean(Metadata.class), applicationContext.getBean(MetadataTools.class)));
        }
    }

    protected void loadActions(EntitySuggestionField suggestionField) {
        loadActions(suggestionField, element);
        if (suggestionField.getActions().isEmpty()) {
            addDefaultActions();
        }
    }

    protected void addDefaultActions() {
        Actions actions = getActions();

        getResultComponent().addAction(actions.create(LookupAction.ID));
        getResultComponent().addAction(actions.create(OpenAction.ID));
    }

    protected Actions getActions() {
        return (Actions) applicationContext.getBean(Actions.NAME);
    }

    protected void loadMetaClass(EntitySuggestionField suggestionField, Element element) {
        String metaClass = element.attributeValue("metaClass");
        if (!StringUtils.isEmpty(metaClass)) {
            suggestionField.setMetaClass(getMetadata().findClass(metaClass));
        }
    }

    @Override
    protected Action loadDeclarativeAction(ActionsHolder actionsHolder, Element element) {
        return loadValuePickerDeclarativeAction(actionsHolder, element);
    }

    protected void loadSuggestionsLimit(EntitySuggestionField suggestionField, Element element) {
        String suggestionsLimit = element.attributeValue("suggestionsLimit");
        if (StringUtils.isNotEmpty(suggestionsLimit)) {
            suggestionField.setSuggestionsLimit(Integer.parseInt(suggestionsLimit));
        }
    }

    protected void loadMinSearchStringLength(EntitySuggestionField suggestionField, Element element) {
        String minSearchStringLength = element.attributeValue("minSearchStringLength");
        if (StringUtils.isNotEmpty(minSearchStringLength)) {
            suggestionField.setMinSearchStringLength(Integer.parseInt(minSearchStringLength));
        }
    }

    protected void loadAsyncSearchDelayMs(EntitySuggestionField suggestionField, Element element) {
        String asyncSearchDelayMs = element.attributeValue("asyncSearchDelayMs");
        if (StringUtils.isNotEmpty(asyncSearchDelayMs)) {
            suggestionField.setAsyncSearchDelayMs(Integer.parseInt(asyncSearchDelayMs));
        }
    }
}
