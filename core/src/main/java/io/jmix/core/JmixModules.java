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

package io.jmix.core;

import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;

/**
 * Holds the list of {@link JmixModuleDescriptor}s.
 */
public class JmixModules {

    public static final Pattern SEPARATOR_PATTERN = Pattern.compile("\\s");

    private final List<JmixModuleDescriptor> components;

    private final Environment environment;

    public JmixModules(Environment environment, List<JmixModuleDescriptor> components) {
        this.environment = environment;
        this.components = components;
    }

    /**
     * @return the list of components
     */
    public List<JmixModuleDescriptor> getComponents() {
        return Collections.unmodifiableList(components);
    }

    /**
     * Get a component by Id.
     * @return component or null if not found
     */
    @Nullable
    public JmixModuleDescriptor get(String componentId) {
        for (JmixModuleDescriptor component : components) {
            if (component.getId().equals(componentId))
                return component;
        }
        return null;
    }

    @Nullable
    public String getProperty(String name) {
        List<String> values = new ArrayList<>();

        List<JmixModuleDescriptor> components = getComponents();
        ListIterator<JmixModuleDescriptor> iterator = components.listIterator(components.size());

        int index;
        while (iterator.hasPrevious()) {
            JmixModuleDescriptor component = iterator.previous();

            String compValue = component.getProperty(name);
            if (StringUtils.isNotEmpty(compValue)) {
                if (component.isAdditiveProperty(name)) {
                    index = 0;
                    for (String valuePart : split(compValue)) {
                        if (!values.contains(valuePart)) {
                            values.add(index, valuePart);
                            index++;
                        }
                    }
                } else {
                    values.add(0, compValue);
                    // we found overwrite, stop iteration
                    break;
                }
            }
        }

        return values.isEmpty() ? null : String.join(" ", values);
    }

    private Iterable<String> split(String compValue) {
        return Splitter.on(SEPARATOR_PATTERN).omitEmptyStrings().split(compValue);
    }
}