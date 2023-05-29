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

package io.jmix.core.impl;

import io.jmix.core.Resources;
import org.apache.commons.io.IOUtils;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component("core_Resources")
public class ResourcesImpl implements Resources, ResourceLoaderAware {

    private final Environment environment;

    private ResourceLoader delegate;

    @Autowired
    public ResourcesImpl(Environment environment) {
        this.environment = environment;
    }

    @Override
    @Nullable
    public InputStream getResourceAsStream(String location) {
        try {
            Resource resource = getResource(location);
            if (resource.exists())
                return resource.getInputStream();
            else
                return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Nullable
    public String getResourceAsString(String location) {
        try (InputStream stream = getResourceAsStream(location)) {
            if (stream == null)
                return null;
            return IOUtils.toString(stream, StandardCharsets.UTF_8);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Search for a resource according to the following rules:
     * <ul>
     * <li>If the location represents an URL, return a new {@link org.springframework.core.io.UrlResource} for
     * this URL.</li>
     * <li>Try to find a file below the {@code conf} directory using {@code location} as relative path.
     * If found, return a new {@link org.springframework.core.io.UrlResource} for this file.</li>
     * <li> Otherwise return a new {@link org.springframework.core.io.ClassPathResource} to retrieve content
     * from classpath.</li>
     * </ul>
     *
     * @param location resource location
     * @return resource reference
     */
    @Override
    public Resource getResource(String location) {
        if (ResourceUtils.isUrl(location)) {
            return delegate.getResource(location);
        } else {
            if (location.startsWith("/"))
                location = location.substring(1);
            File file = new File(environment.getProperty("jmix.core.conf-dir"), location);
            if (file.exists()) {
                location = file.toURI().toString();
            } else {
                location = "classpath:" + location;
            }
            return delegate.getResource(location);
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        return delegate.getClassLoader();
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.delegate = resourceLoader;
    }
}