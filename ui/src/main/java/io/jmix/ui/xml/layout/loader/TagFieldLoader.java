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

package io.jmix.ui.xml.layout.loader;

import io.jmix.ui.component.TagField;

public class TagFieldLoader extends AbstractSuggestionFieldLoader<TagField> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(TagField.NAME);
        loadId(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadTabIndex(resultComponent, element);

        loadAsyncSearchDelayMs(resultComponent, element);
        loadMinSearchStringLength(resultComponent, element);
        loadSuggestionsLimit(resultComponent, element);

        loadCaptionProperty(resultComponent, element);
        loadInputPrompt(resultComponent, element);

        loadPopupWidth(resultComponent, element);

        loadQuery(resultComponent, element);

        loadFormatter(resultComponent, element);

        loadBoolean(element, "clearAllVisible",
                resultComponent::setClearAllVisible);
    }
}
