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

package io.jmix.flowui.sys;

import io.jmix.core.DevelopmentException;
import io.jmix.core.Resources;
import io.jmix.flowui.view.View;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Loads view XML descriptors.
 */
@Component("flowui_ViewXmlLoader")
public class ViewXmlLoader {

    protected Resources resources;
    protected ViewXmlDocumentCache viewXmlDocumentCache;
    protected ViewXmlParser viewXmlParser;

    @Autowired
    public void setResources(Resources resources) {
        this.resources = resources;
    }

    @Autowired
    public void setViewXmlDocumentCache(ViewXmlDocumentCache viewXmlDocumentCache) {
        this.viewXmlDocumentCache = viewXmlDocumentCache;
    }

    @Autowired
    public void setViewXmlParser(ViewXmlParser viewXmlParser) {
        this.viewXmlParser = viewXmlParser;
    }

    /**
     * Loads a descriptor.
     *
     * @param resourcePath path to the resource containing the XML
     * @return root XML element
     */
    public Element load(String resourcePath) {
        String template = loadTemplate(resourcePath);
        Document document = getDocument(template);

        return document.getRootElement();
    }

    private String loadTemplate(String resourcePath) {
        try (InputStream stream = resources.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new DevelopmentException("Template is not found " + resourcePath, "Path", resourcePath);
            }

            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Unable to read %s template", View.class.getSimpleName()));
        }
    }

    protected Document getDocument(String template) {
        Document document = viewXmlDocumentCache.get(template);
        if (document == null) {
            document = createDocument(template);
            viewXmlDocumentCache.put(template, document);
        }

        return document;
    }

    protected Document createDocument(String template) {
        return viewXmlParser.parseDescriptor(template);
    }
}
