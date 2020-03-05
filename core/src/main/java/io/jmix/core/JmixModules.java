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

    private final List<JmixModuleDescriptor> moduleDescriptors;

    public JmixModules(List<JmixModuleDescriptor> moduleDescriptors) {
        this.moduleDescriptors = moduleDescriptors;
    }

    /**
     * @return the list of module descriptors sorted according to dependencies
     */
    public List<JmixModuleDescriptor> getAll() {
        return Collections.unmodifiableList(moduleDescriptors);
    }

    /**
     * @return module descriptor by its id or null if not found
     */
    @Nullable
    public JmixModuleDescriptor get(String moduleId) {
        for (JmixModuleDescriptor module : moduleDescriptors) {
            if (module.getId().equals(moduleId))
                return module;
        }
        return null;
    }

    /**
     * @return the last module descriptor which normally corresponds to the application
     */
    public JmixModuleDescriptor getLast() {
        if (moduleDescriptors.isEmpty()) {
            throw new IllegalStateException("No Jmix modules found");
        }
        return moduleDescriptors.get(moduleDescriptors.size() - 1);
    }

    @Nullable
    public String getProperty(String name) {
        List<String> values = new ArrayList<>();

        List<JmixModuleDescriptor> descriptors = getAll();
        ListIterator<JmixModuleDescriptor> iterator = descriptors.listIterator(descriptors.size());

        int index;
        while (iterator.hasPrevious()) {
            JmixModuleDescriptor module = iterator.previous();

            String moduleValue = module.getProperty(name);
            if (StringUtils.isNotEmpty(moduleValue)) {
                if (module.isAdditiveProperty(name)) {
                    index = 0;
                    for (String valuePart : split(moduleValue)) {
                        if (!values.contains(valuePart)) {
                            values.add(index, valuePart);
                            index++;
                        }
                    }
                } else {
                    values.add(0, moduleValue);
                    // we found overwrite, stop iteration
                    break;
                }
            }
        }

        return values.isEmpty() ? null : String.join(" ", values);
    }

    private Iterable<String> split(String moduleValue) {
        return Splitter.on(SEPARATOR_PATTERN).omitEmptyStrings().split(moduleValue);
    }
}
