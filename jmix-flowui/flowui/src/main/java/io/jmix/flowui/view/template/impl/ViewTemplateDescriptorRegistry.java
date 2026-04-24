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

package io.jmix.flowui.view.template.impl;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores XML descriptors rendered for template-generated views.
 */
@Component("flowui_ViewTemplateDescriptorRegistry")
public class ViewTemplateDescriptorRegistry {

    /**
     * Prefix used for synthetic descriptor paths stored in this registry.
     */
    public static final String PATH_PREFIX = "view-template:";

    protected Map<String, String> descriptors = new ConcurrentHashMap<>();

    /**
     * Creates a synthetic descriptor path for the given view id.
     *
     * @param viewId generated view id
     * @return synthetic descriptor path
     */
    public String createPath(String viewId) {
        return PATH_PREFIX + viewId;
    }

    /**
     * Stores a rendered descriptor under the given synthetic path.
     *
     * @param path       synthetic descriptor path
     * @param descriptor rendered XML descriptor
     */
    public void put(String path, String descriptor) {
        descriptors.put(path, descriptor);
    }

    /**
     * Returns a rendered descriptor by its synthetic path.
     *
     * @param path synthetic descriptor path
     * @return descriptor if present
     */
    public Optional<String> getDescriptor(String path) {
        return Optional.ofNullable(descriptors.get(path));
    }

    /**
     * Checks whether the given path belongs to this registry.
     *
     * @param path descriptor path
     * @return {@code true} if the path uses {@link #PATH_PREFIX}
     */
    public boolean isTemplatePath(String path) {
        return path.startsWith(PATH_PREFIX);
    }
}
