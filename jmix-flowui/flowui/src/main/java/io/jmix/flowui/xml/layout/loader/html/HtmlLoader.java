/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.html;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Html;
import io.jmix.core.Resources;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import org.dom4j.Element;
import org.springframework.lang.Nullable;

import java.io.InputStream;

public class HtmlLoader extends AbstractComponentLoader<Html> {

    protected static final String HTML_CONTENT_ELEMENT_NAME = "content";

    @Override
    protected Html createComponent() {
        String htmlContent = loadHtmlContent(element);
        if (!Strings.isNullOrEmpty(htmlContent)) {
            return new Html(htmlContent);
        }

        InputStream inputStream = loadFileContent(element);
        if (inputStream != null) {
            return new Html(inputStream);
        }

        htmlContent = getLoaderSupport().loadResourceString(element, "content", context.getMessageGroup())
                .orElseThrow(() -> new GuiDevelopmentException("Content cannot be null or empty", context));
        return new Html(htmlContent);
    }

    @Nullable
    protected String loadHtmlContent(Element element) {
        Element contentElement = element.element(HTML_CONTENT_ELEMENT_NAME);
        if (contentElement == null) {
            return null;
        }

        String text = contentElement.getText();
        if (Strings.isNullOrEmpty(text)) {
            throw new GuiDevelopmentException(
                    String.format("'%s' element cannot be empty", HTML_CONTENT_ELEMENT_NAME), context);
        }

        return text;
    }

    @Nullable
    protected InputStream loadFileContent(Element element) {
        return getLoaderSupport().loadResourceString(element, "file", context.getMessageGroup())
                .map(path ->
                        applicationContext.getBean(Resources.class).getResourceAsStream(path))
                .orElse(null);
    }

    @Override
    public void loadComponent() {
        componentLoader().loadClassNames(resultComponent, element);
    }
}
