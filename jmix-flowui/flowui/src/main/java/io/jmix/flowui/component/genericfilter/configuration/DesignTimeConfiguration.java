/*
 * Copyright 2022 Haulmont.
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

import org.springframework.lang.Nullable;
import java.util.HashMap;
import java.util.Map;

public class DesignTimeConfiguration implements Configuration {

    protected final String id;
    protected final String name;
    protected final LogicalFilterComponent<?> rootLogicalFilterComponent;
    protected final GenericFilter owner;

    protected Map<String, Object> defaultValuesMap = new HashMap<>();

    public DesignTimeConfiguration(String id,
                                   @Nullable String name,
                                   LogicalFilterComponent<?> rootLogicalFilterComponent,
                                   GenericFilter owner) {
        this.id = id;
        this.name = name;
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
        throw new UnsupportedOperationException("You cannot set name attribute for design-time configuration. " +
                "Use FilterCopyAction to create a modifiable copy of configuration");
    }

    @Override
    public LogicalFilterComponent<?> getRootLogicalFilterComponent() {
        return rootLogicalFilterComponent;
    }

    @Override
    public void setRootLogicalFilterComponent(LogicalFilterComponent<?> rootLogicalFilterComponent) {
        throw new UnsupportedOperationException("You cannot set root component for design-time configuration. " +
                "Use FilterCopyAction to create a modifiable copy of configuration");
    }

    @Override
    public LogicalCondition getQueryCondition() {
        return rootLogicalFilterComponent.getQueryCondition();
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void setModified(boolean modified) {
        throw new UnsupportedOperationException("You cannot set modified attribute for design-time configuration. " +
                "Use FilterCopyAction to create a modifiable copy of configuration");
    }

    @Override
    public boolean isFilterComponentModified(FilterComponent filterComponent) {
        return false;
    }

    @Override
    public void setFilterComponentModified(FilterComponent filterComponent, boolean modified) {
        throw new UnsupportedOperationException("You cannot set modified attribute for design-time configuration. " +
                "Use FilterCopyAction to create a modifiable copy of configuration");
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
        throw new UnsupportedOperationException("You cannot remove default value for design-time configuration. " +
                "Use FilterCopyAction to create a modifiable copy of configuration");
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
        throw new UnsupportedOperationException("You cannot remove default values for design-time configuration. " +
                "Use FilterCopyAction to create a modifiable copy of configuration");
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
        if (!(obj instanceof DesignTimeConfiguration)) {
            return false;
        }

        return id.equals(((DesignTimeConfiguration) obj).getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
