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

package filter_configuration_persistence.view;

import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.flowui.component.filter.FilterComponent;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A configuration that is neither a run-time nor a design-time one: an application is free to implement the
 * public {@link Configuration} interface, and such an implementation may be immutable. Delegates to a
 * {@link DesignTimeConfiguration}, so every mutating method answers with {@code UnsupportedOperationException}.
 */
@NullMarked
public class TestImmutableConfiguration implements Configuration {

    protected final DesignTimeConfiguration delegate;

    public TestImmutableConfiguration(String id, @Nullable String name,
                                      LogicalFilterComponent<?> rootLogicalFilterComponent, GenericFilter owner) {
        delegate = new DesignTimeConfiguration(id, name, rootLogicalFilterComponent, owner);
    }

    @Override
    public GenericFilter getOwner() {
        return delegate.getOwner();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Nullable
    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public void setName(@Nullable String name) {
        delegate.setName(name);
    }

    @Override
    public LogicalFilterComponent<?> getRootLogicalFilterComponent() {
        return delegate.getRootLogicalFilterComponent();
    }

    @Override
    public void setRootLogicalFilterComponent(LogicalFilterComponent<?> rootLogicalFilterComponent) {
        delegate.setRootLogicalFilterComponent(rootLogicalFilterComponent);
    }

    @Override
    public LogicalCondition getQueryCondition() {
        return delegate.getQueryCondition();
    }

    @Override
    public boolean isModified() {
        return delegate.isModified();
    }

    @Override
    public void setModified(boolean modified) {
        delegate.setModified(modified);
    }

    @Override
    public boolean isFilterComponentModified(FilterComponent filterComponent) {
        return delegate.isFilterComponentModified(filterComponent);
    }

    @Override
    public void setFilterComponentModified(FilterComponent filterComponent, boolean modified) {
        delegate.setFilterComponentModified(filterComponent, modified);
    }

    @Override
    public void setFilterComponentDefaultValue(String parameterName, @Nullable Object defaultValue) {
        delegate.setFilterComponentDefaultValue(parameterName, defaultValue);
    }

    @Override
    public void resetFilterComponentDefaultValue(String parameterName) {
        delegate.resetFilterComponentDefaultValue(parameterName);
    }

    @Nullable
    @Override
    public Object getFilterComponentDefaultValue(String parameterName) {
        return delegate.getFilterComponentDefaultValue(parameterName);
    }

    @Override
    public void resetAllDefaultValues() {
        delegate.resetAllDefaultValues();
    }

    @Override
    public int compareTo(Configuration other) {
        return delegate.compareTo(other);
    }
}
