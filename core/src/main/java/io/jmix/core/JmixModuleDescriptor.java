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

import org.springframework.core.env.PropertySource;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes a Jmix module which the current application depends on.
 */
public class JmixModuleDescriptor implements Comparable<JmixModuleDescriptor> {

    private final String id;
    private final List<JmixModuleDescriptor> dependencies = new ArrayList<>();
    private PropertySource<?> propertySource;

    public JmixModuleDescriptor(String id) {
        this.id = id;
    }

    /**
     * @return module Id
     */
    public String getId() {
        return id;
    }

    /**
     * @return base package of the module. It is normally equal to {@link #getId()}
     */
    public String getBasePackage() {
        return id;
    }

    /**
     * INTERNAL.
     * Add a dependency to the module.
     */
    public void addDependency(JmixModuleDescriptor other) {
        if (dependencies.contains(other))
            return;
        if (other.dependsOn(this))
            throw new RuntimeException("Circular dependency between modules '" + this + "' and '" + other + "'");

        dependencies.add(other);
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
    public int compareTo(JmixModuleDescriptor other) {
        if (this.dependsOn(other))
            return 1;
        if (other.dependsOn(this)) {
            return -1;
        }
        return 0;
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
