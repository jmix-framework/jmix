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

import io.jmix.core.annotation.Internal;
import org.springframework.core.env.PropertySource;

import org.springframework.lang.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Describes a Jmix module which the current application depends on.
 */
public class JmixModuleDescriptor {

    private final String id;
    private final String basePackage;
    private final List<JmixModuleDescriptor> dependencies = new ArrayList<>();
    private PropertySource<?> propertySource;

    public JmixModuleDescriptor(String id, String basePackage) {
        this.id = id;
        this.basePackage = basePackage;
    }

    public JmixModuleDescriptor(String id) {
        this.id = id;
        this.basePackage = id;
    }

    /**
     * @return module Id
     */
    public String getId() {
        return id;
    }

    /**
     * @return base package of the module
     */
    public String getBasePackage() {
        return basePackage;
    }

    /**
     * INTERNAL.
     * Add a dependency to the module.
     */
    @Internal
    public void addDependency(JmixModuleDescriptor other) {
        if (dependencies.contains(other))
            return;
        if (other.dependsOn(this))
            throw new RuntimeException("Circular dependency between modules '" + this + "' and '" + other + "'");

        dependencies.add(other);
    }

    /**
     * Returns an unmodifiable list of dependent modules.
     */
    public List<JmixModuleDescriptor> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    /**
     * Check if this module depends on the given module.
     */
    public boolean dependsOn(JmixModuleDescriptor other) {
        for (JmixModuleDescriptor dependency : dependencies) {
            if (dependency.equals(other) || dependency.dependsOn(other))
                return true;
        }
        return false;
    }

    /**
     * INTERNAL.
     * Set the module's PropertySource.
     */
    @Internal
    public void setPropertySource(@Nullable PropertySource<?> propertySource) {
        this.propertySource = propertySource;
    }

    /**
     * Get the module's PropertySource.
     */
    @Nullable
    public PropertySource<?> getPropertySource() {
        return propertySource;
    }

    /**
     * @return a property defined in this module's PropertySource or null if not found
     */
    @Nullable
    public String getProperty(String property) {
        return propertySource == null ? null : (String) propertySource.getProperty(property);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JmixModuleDescriptor that = (JmixModuleDescriptor) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
