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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.jmix.core.DevelopmentException;
import io.jmix.core.Resources;
import io.jmix.core.common.xmlparsing.Dom4jTools;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component("ui_CompositeDescriptorLoader")
public class CompositeDescriptorLoader {

    protected static final int CACHE_DESCRIPTORS_COUNT = 20;

    protected Cache<String, Document> cache;

    @Autowired
    protected Resources resources;
    @Autowired
    protected Dom4jTools dom4JTools;

    public Element load(String path) {
        String descriptor = loadDescriptor(path);
        Document document = getDocument(descriptor);
        return document.getRootElement();
    }

    protected String loadDescriptor(String resourcePath) {
        try (InputStream stream = resources.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new DevelopmentException("Composite component descriptor not found " + resourcePath,
                        "Path", resourcePath);
            }
            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read composite component descriptor");
        }
    }

    protected Document getDocument(String descriptor) {
        if (cache == null) {
            cache = CacheBuilder.newBuilder()
                    .maximumSize(CACHE_DESCRIPTORS_COUNT)
                    .build();
        }

        Document document = cache.getIfPresent(descriptor);
        if (document == null) {
            document = createDocument(descriptor);
            cache.put(descriptor, document);
        }

        return document;
    }

    protected Document createDocument(String descriptor) {
        return dom4JTools.readDocument(descriptor);
    }
}
