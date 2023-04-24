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

package io.jmix.flowui.component.genericfilter.configuration;

import io.jmix.core.common.util.Preconditions;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.flowui.component.filer.FilterComponent;
import io.jmix.flowui.component.filer.SingleFilterComponentBase;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RunTimeConfiguration implements Configuration {

    protected final String id;
    protected final GenericFilter owner;

    protected String name;
    protected LogicalFilterComponent<?> rootLogicalFilterComponent;
    protected Set<FilterComponent> modifiedFilterComponents = new HashSet<>();
    protected Map<String, Object> defaultValuesMap = new HashMap<>();

    public RunTimeConfiguration(String id, LogicalFilterComponent<?> rootLogicalFilterComponent, GenericFilter owner) {
        this.id = id;
        this.rootLogicalFilterComponent = rootLogicalFilterComponent;
        this.owner = owner;
    }

    @Override
    public GenericFilter getOwner() {
        return owner;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(@Nullable String name) {
        this.name = name;
    }

    @Override
    public LogicalFilterComponent<?> getRootLogicalFilterComponent() {
        return rootLogicalFilterComponent;
    }

    @Override
    public void setRootLogicalFilterComponent(LogicalFilterComponent<?> rootLogicalFilterComponent) {
        Preconditions.checkNotNullArgument(rootLogicalFilterComponent);
        this.rootLogicalFilterComponent = rootLogicalFilterComponent;
    }

    @Override
    public LogicalCondition getQueryCondition() {
        return rootLogicalFilterComponent.getQueryCondition();
    }

    @Override
    public boolean isModified() {
        return !modifiedFilterComponents.isEmpty();
    }

    @Override
    public void setModified(boolean modified) {
        for (FilterComponent filterComponent : rootLogicalFilterComponent.getOwnFilterComponents()) {
            setFilterComponentModified(filterComponent, modified);
        }
    }

    @Override
    public boolean isFilterComponentModified(FilterComponent filterComponent) {
        return modifiedFilterComponents.contains(filterComponent);
    }

    @Override
    public void setFilterComponentModified(FilterComponent filterComponent, boolean modified) {
        if (modified) {
            modifiedFilterComponents.add(filterComponent);
        } else {
            modifiedFilterComponents.remove(filterComponent);
        }

        if (filterComponent instanceof LogicalFilterComponent) {
            ((LogicalFilterComponent<?>) filterComponent)
                    .getOwnFilterComponents()
                    .forEach(ownFilterComponent ->
                            setFilterComponentModified(ownFilterComponent, modified));
        }
    }

    @Override
    public void setFilterComponentDefaultValue(String parameterName, @Nullable Object defaultValue) {
        Preconditions.checkNotNullArgument(parameterName);
        if (isFilterComponentExist(parameterName)) {
            defaultValuesMap.put(parameterName, defaultValue);
        }
    }

    @Override
    public void resetFilterComponentDefaultValue(String parameterName) {
        Preconditions.checkNotNullArgument(parameterName);
        if (isFilterComponentExist(parameterName)) {
            defaultValuesMap.remove(parameterName);
        }
    }

    @Nullable
    @Override
    public Object getFilterComponentDefaultValue(String parameterName) {
        Preconditions.checkNotNullArgument(parameterName);
        if (isFilterComponentExist(parameterName)) {
            return defaultValuesMap.get(parameterName);
        }

        return null;
    }

    @Override
    public void resetAllDefaultValues() {
        defaultValuesMap = new HashMap<>();
    }

    protected boolean isFilterComponentExist(String parameterName) {
        return rootLogicalFilterComponent.getFilterComponents().stream()
                .anyMatch(filterComponent -> filterComponent instanceof SingleFilterComponentBase
                        && parameterName.equals(((SingleFilterComponentBase<?>) filterComponent).getParameterName()));
    }

    @Override
    public int compareTo(Configuration other) {
        return id.compareTo(other.getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RunTimeConfiguration)) {
            return false;
        }

        return id.equals(((RunTimeConfiguration) obj).getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
