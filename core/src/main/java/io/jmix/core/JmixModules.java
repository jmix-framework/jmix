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

import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Holds the list of {@link JmixModuleDescriptor}s.
 */
public class JmixModules {

    private final List<JmixModuleDescriptor> moduleDescriptors;
    private final Environment environment;

    public JmixModules(List<JmixModuleDescriptor> moduleDescriptors, Environment environment) {
        this.moduleDescriptors = moduleDescriptors;
        this.environment = environment;
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

    /**
     * Returns the list of property values from all modules in the order of their dependencies, from the core
     * to the application. The last item in the list is the value obtained from {@link Environment}.
     * <p>
     * This method is convenient for getting values of "additive" properties like {@code jmix.core.fetchPlanConfig}.
     */
    public List<String> getPropertyValues(String propertyName) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (JmixModuleDescriptor module : moduleDescriptors) {
            PropertySource<?> propertySource = module.getPropertySource();
            if (propertySource != null) {
                String value = (String) propertySource.getProperty(propertyName);
                if (value != null) {
                    set.add(value);
                }
            }
        }
        String value = environment.getProperty(propertyName);
        if (value != null) {
            set.add(value);
        }
        return new ArrayList<>(set);
    }

}
