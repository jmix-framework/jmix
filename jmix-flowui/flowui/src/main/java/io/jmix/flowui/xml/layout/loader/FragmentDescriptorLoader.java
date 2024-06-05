/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.xml.layout.loader;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.jmix.core.DevelopmentException;
import io.jmix.core.Resources;
import io.jmix.core.common.util.Dom4j;
import io.jmix.flowui.fragment.Fragment;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component("flowui_FragmentDescriptorLoader")
public class FragmentDescriptorLoader {

    protected static final int CACHE_DESCRIPTORS_COUNT = 20;

    protected final Resources resources;

    protected Cache<String, Document> cache;

    public FragmentDescriptorLoader(Resources resources) {
        this.resources = resources;
    }

    public Element load(String path) {
        String descriptor = loadTemplate(path);
        Document document = getDocument(descriptor);
        return document.getRootElement();
    }

    protected String loadTemplate(String resourcePath) {
        try (InputStream stream = resources.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new DevelopmentException(Fragment.class.getSimpleName() +
                        " descriptor not found " + resourcePath, "Path", resourcePath);
            }
            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Unable to read %s descriptor",
                    Fragment.class.getSimpleName()), e);
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
        return Dom4j.readDocument(descriptor);
    }
}
