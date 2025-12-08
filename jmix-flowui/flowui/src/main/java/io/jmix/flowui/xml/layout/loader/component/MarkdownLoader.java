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
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import org.dom4j.Element;
import org.springframework.lang.Nullable;

public class MarkdownLoader extends AbstractComponentLoader<Markdown> {

    protected static final String MARKDOWN_CONTENT_ELEMENT_NAME = "content";

    @Override
    protected Markdown createComponent() {
        String htmlContent = loadMarkdownContent(element);
        if (!Strings.isNullOrEmpty(htmlContent)) {
            return new Markdown(htmlContent);
        }

        htmlContent = getLoaderSupport().loadResourceString(element, "content", context.getMessageGroup())
                .orElseThrow(() -> new GuiDevelopmentException("Content cannot be null or empty", context));

        return new Markdown(htmlContent);
    }

    @Override
    public void loadComponent() {
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
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
}
