/*
 * Copyright 2023 Haulmont.
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

import io.jmix.flowui.component.codeeditor.CodeEditor;
import io.jmix.flowui.kit.component.codeeditor.CodeEditorMode;
import io.jmix.flowui.kit.component.codeeditor.CodeEditorTheme;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;

public class CodeEditorLoader extends AbstractComponentLoader<CodeEditor> {

    protected DataLoaderSupport dataLoaderSupport;

    @Override
    protected CodeEditor createComponent() {
        return factory.create(CodeEditor.class);
    }

    @Override
    public void loadComponent() {
        getDataLoaderSupport().loadData(resultComponent, element);

        loadBoolean(element, "highlightActiveLine", resultComponent::setHighlightActiveLine);
        loadBoolean(element, "highlightGutterLine", resultComponent::setHighlightGutterLine);
        loadBoolean(element, "showGutter", resultComponent::setShowGutter);
        loadBoolean(element, "showLineNumbers", resultComponent::setShowLineNumbers);
        loadBoolean(element, "showPrintMargin", resultComponent::setShowPrintMargin);
        loadInteger(element, "printMarginColumn", resultComponent::setPrintMarginColumn);
        loadString(element, "fontSize", resultComponent::setFontSize);

        loadEnum(element, CodeEditorMode.class, "mode", resultComponent::setMode);
        loadEnum(element, CodeEditorTheme.class, "theme", resultComponent::setTheme);

        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadValidationAttributes(resultComponent, element, context);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadHelperText(resultComponent, element);
        componentLoader().loadLabel(resultComponent, element);
        componentLoader().loadValueAndElementAttributes(resultComponent, element);
        componentLoader().loadRequired(resultComponent, element, context);
        componentLoader().loadTabIndex(resultComponent, element);
        componentLoader().loadTitle(resultComponent, element, context);
        componentLoader().loadTooltip(resultComponent, element);
    }

    protected DataLoaderSupport getDataLoaderSupport() {
        if (dataLoaderSupport == null) {
            dataLoaderSupport = applicationContext.getBean(DataLoaderSupport.class, context);
        }
        return dataLoaderSupport;
    }
}
