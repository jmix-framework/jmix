/*
 * Copyright 2019 Haulmont.
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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.CubaProperties;
import com.haulmont.cuba.gui.components.Embedded;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import io.jmix.ui.component.impl.AbstractComponent;
import io.jmix.ui.download.DownloadDataProvider;
import io.jmix.ui.sys.ControllerUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Deprecated
public class WebEmbedded extends AbstractComponent<com.vaadin.ui.Embedded> implements Embedded {

    protected Map<String, String> parameters = null;
    protected Type type = Type.IMAGE;
    protected Resource resource;

    @Autowired
    protected CubaProperties cubaProperties;

    public WebEmbedded() {
        component = new com.vaadin.ui.Embedded();
        provideType();
    }

    @Override
    public void setSource(@Nullable URL src) {
        if (src != null) {
            resource = new ExternalResource(src);
            component.setSource(resource);
            setType(Type.BROWSER);
        } else {
            resetSource();
        }
    }

    @Override
    public void setSource(@Nullable String src) {
        if (src != null) {
            if (src.startsWith("http") || src.startsWith("https")) {
                try {
                    setSource(new URL(src));
                } catch (MalformedURLException e) {
                    throw new RuntimeException("Unable to parse url for embedded source", e);
                }
            } else if (src.startsWith("theme://")) {
                String themeResource = src.substring("theme://".length());
                resource = new ThemeResource(themeResource);
                component.setSource(resource);
            } else {
                File file = new File(src);
                if (!file.isAbsolute()) {
                    String root = cubaProperties.getEmbeddedResourcesRoot();
                    if (root != null) {
                        if (!root.endsWith(File.separator)) {
                            root += File.separator;
                        }
                        file = new File(root + file.getPath());
                    }
                }

                resource = new FileResource(file);
                component.setSource(resource);
            }
        } else {
            resetSource();
        }
    }

    @Override
    public void setSource(String fileName, @Nullable final InputStream src) {
        if (src != null) {
            resource = new StreamResource((StreamResource.StreamSource) () -> {
                try {
                    src.reset();
                } catch (IOException e) {
                    Logger log = LoggerFactory.getLogger(WebEmbedded.this.getClass());
                    log.debug("Ignored IOException on stream reset", e);
                }
                return src;
            }, fileName);
            component.setSource(resource);
        } else {
            resetSource();
        }
    }

    @Override
    public void setSource(String fileName, @Nullable final DownloadDataProvider dataProvider) {
        if (dataProvider != null) {
            resource = new StreamResource(
                    dataProvider::provide,
                    fileName
            );
            component.setSource(resource);
        } else {
            resetSource();
        }
    }

    @Override
    public void setRelativeSource(@Nullable String src) {
        if (src != null) {
            try {
                URL context = new URL(ControllerUtils.getLocationWithoutParams());
                resource = new ExternalResource(new URL(context, src));
                component.setSource(resource);
                setType(Type.BROWSER);
            } catch (MalformedURLException e) {
                throw new RuntimeException("Unable to get external resource for given relative source");
            }
        } else {
            resetSource();
        }
    }

    @Override
    public void resetSource() {
        resource = null;
        component.markAsDirty();
        component.setMimeType("image/png");
        component.setType(com.vaadin.ui.Embedded.TYPE_IMAGE);
        component.setSource(new StreamResource(new EmptyStreamSource(), UUID.randomUUID() + ".png"));
    }

    @Override
    public void setMIMEType(String mt) {
        component.setMimeType(mt);
    }

    @Override
    public void addParameter(String name, String value) {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        component.setParameter(name, value);
        parameters.put(name, value);
    }

    @Override
    public void removeParameter(String name) {
        component.removeParameter(name);
        if (parameters != null) {
            parameters.remove(name);
        }
    }

    @Override
    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    @Override
    public void setType(Type t) {
        type = t;
        provideType();
    }

    @Override
    public Type getType() {
        return type;
    }

    protected void provideType() {
        switch (type) {
            case OBJECT:
                component.setType(com.vaadin.ui.Embedded.TYPE_OBJECT);
                break;
            case IMAGE:
                component.setType(com.vaadin.ui.Embedded.TYPE_IMAGE);
                break;
            case BROWSER:
                component.setType(com.vaadin.ui.Embedded.TYPE_BROWSER);
                if (resource == null) {
                    component.setSource(new StreamResource(() -> {
                        return new ByteArrayInputStream("<html></html>".getBytes(StandardCharsets.UTF_8));
                    }, UUID.randomUUID() + ".html"));
                }
                break;
        }
    }

    protected static class EmptyStreamSource implements StreamResource.StreamSource {
        public static final String EMPTY_IMAGE_PATH = "/com/haulmont/cuba/web/gui/components/resources/empty.png";

        protected byte[] emptyImage;

        @Override
        public InputStream getStream() {
            if (emptyImage == null) {
                InputStream stream = WebEmbedded.class.getResourceAsStream(EMPTY_IMAGE_PATH);
                try {
                    emptyImage = IOUtils.toByteArray(stream);
                } catch (IOException e) {
                    throw new RuntimeException("Unable to read empty.png from classpath", e);
                }
            }

            return new ByteArrayInputStream(emptyImage);
        }
    }
}
