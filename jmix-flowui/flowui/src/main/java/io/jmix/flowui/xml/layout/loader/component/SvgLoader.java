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

import com.google.common.base.Strings;
import com.vaadin.flow.component.Svg;
import io.jmix.core.Resources;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import org.dom4j.Element;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;

public class SvgLoader extends AbstractComponentLoader<Svg> {

    protected static final String SVG_CONTENT_ELEMENT_NAME = "content";

    @Override
    protected Svg createComponent() {
        return factory.create(Svg.class);
    }

    @Override
    public void loadComponent() {
        loadSvg(element);

        componentLoader().loadClassNames(resultComponent, element);
    }

    protected void loadSvg(Element element) {
        String svgContent = loadSvgContent(element);
        if (!Strings.isNullOrEmpty(svgContent)) {
            resultComponent.setSvg(svgContent);
            return;
        }

        InputStream fileContent = loadFileContent(element);
        if (fileContent != null) {
            resultComponent.setSvg(fileContent);
        }
    }

    @Nullable
    protected String loadSvgContent(Element element) {
        Element contentElement = element.element(SVG_CONTENT_ELEMENT_NAME);
        if (contentElement == null) {
            return null;
        }

        String text = contentElement.getText();
        if (Strings.isNullOrEmpty(text) || !text.contains("<svg")) {
            // Markup that is not wrapped in CDATA is parsed as elements, so getText() leaves only whitespace.
            throw new GuiDevelopmentException(
                    String.format("'%s' element must contain SVG markup wrapped in CDATA", SVG_CONTENT_ELEMENT_NAME),
                    context);
        }

        return text;
    }

    @Nullable
    protected InputStream loadFileContent(Element element) {
        return loadString(element, "file")
                .map(path -> {
                    InputStream stream = applicationContext.getBean(Resources.class).getResourceAsStream(path);
                    if (stream == null) {
                        throw new GuiDevelopmentException(
                                String.format("Resource '%s' not found", path), context);
                    }
                    return stream;
                })
                .orElse(null);
    }
}
