/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.component;

import io.jmix.flowui.component.markdowneditor.MarkdownEditor;
import io.jmix.flowui.kit.component.markdowneditor.MarkdownEditorMode;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;

public class MarkdownEditorLoader extends AbstractComponentLoader<MarkdownEditor> {

    protected DataLoaderSupport dataLoaderSupport;

    @Override
    protected MarkdownEditor createComponent() {
        return factory.create(MarkdownEditor.class);
    }

    /**
     * Loads component properties by XML definition.
     */
    @Override
    public void loadComponent() {
        getDataLoaderSupport().loadData(resultComponent, element);

        loadEnum(element, MarkdownEditorMode.class, "mode", resultComponent::setMode);

        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadLabel(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadRequired(resultComponent, element, context);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadValidationAttributes(resultComponent, element, context);
        componentLoader().loadHelperText(resultComponent, element);
        componentLoader().loadPlaceholder(resultComponent, element);
        componentLoader().loadValueChangeMode(resultComponent, element);
        componentLoader().loadValueAndElementAttributes(resultComponent, element);
        componentLoader().loadAriaLabel(resultComponent, element);
        componentLoader().loadTooltip(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadFocusableAttributes(resultComponent, element);
        componentLoader().loadAutofocus(resultComponent, element);
    }

    protected DataLoaderSupport getDataLoaderSupport() {
        if (dataLoaderSupport == null) {
            dataLoaderSupport = applicationContext.getBean(DataLoaderSupport.class, context);
        }
        return dataLoaderSupport;
    }
}
