/*
 * Copyright 2025 Haulmont.
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

import com.google.common.base.Strings;
import com.vaadin.flow.component.markdown.Markdown;
import io.jmix.flowui.data.binding.TextComponentReadonlyDataBinding;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;
import org.dom4j.Element;
import org.jspecify.annotations.Nullable;

public class MarkdownLoader extends AbstractComponentLoader<Markdown> {

    protected static final String MARKDOWN_CONTENT_ELEMENT_NAME = "content";

    protected DataLoaderSupport dataLoaderSupport;
    protected TextComponentReadonlyDataBinding textComponentReadonlyDataBinding;

    @Override
    protected Markdown createComponent() {
        return factory.create(Markdown.class);
    }

    @Override
    public void loadComponent() {
        loadContent();

        loadBoolean(element, "lineBreaks", resultComponent::setLineBreaks);

        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);

        loadData();
    }

    protected void loadContent() {
        String markdownContent = loadMarkdownContent(element);
        if (markdownContent == null) {
            markdownContent = getLoaderSupport().loadResourceString(element, "content", context.getMessageGroup())
                    .orElse(null);
        }

        if (markdownContent != null) {
            resultComponent.setContent(markdownContent);
        } else if (Strings.isNullOrEmpty(element.attributeValue("property"))) {
            throw new GuiDevelopmentException("Content cannot be null or empty", context);
        }
    }

    protected void loadData() {
        String property = element.attributeValue("property");
        if (!Strings.isNullOrEmpty(property)) {
            getDataLoaderSupport().loadContainer(element, property)
                    .ifPresent(container -> getTextComponentDataBinding().bind(
                            resultComponent, container, property, resultComponent::setContent));
        }
    }

    @Nullable
    protected String loadMarkdownContent(Element element) {
        Element contentElement = element.element(MARKDOWN_CONTENT_ELEMENT_NAME);
        if (contentElement == null) {
            return null;
        }

        String text = contentElement.getText();
        if (Strings.isNullOrEmpty(text)) {
            throw new GuiDevelopmentException(
                    String.format("'%s' element cannot be empty", MARKDOWN_CONTENT_ELEMENT_NAME), context);
        }

        return text;
    }

    protected DataLoaderSupport getDataLoaderSupport() {
        if (dataLoaderSupport == null) {
            dataLoaderSupport = applicationContext.getBean(DataLoaderSupport.class, context);
        }
        return dataLoaderSupport;
    }

    protected TextComponentReadonlyDataBinding getTextComponentDataBinding() {
        if (textComponentReadonlyDataBinding == null) {
            textComponentReadonlyDataBinding = applicationContext.getBean(TextComponentReadonlyDataBinding.class);
        }
        return textComponentReadonlyDataBinding;
    }
}
