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
import io.jmix.flowui.component.filter.FilterComponent;
import io.jmix.flowui.component.filter.SingleFilterComponentBase;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.MutableConfiguration;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;

import org.jspecify.annotations.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A runtime (user-created) filter configuration that supports dynamic modification of
 * its filter components.
 * <p>
 * Implements {@link MutableConfiguration} — use that interface as the declared type when
 * you need compile-time safety against calling mutating methods on a read-only
 * {@link DesignTimeConfiguration}.
 * <p>
 * <b>Auto-tracking of modified state:</b> this implementation subscribes to
 * {@link LogicalFilterComponent.FilterComponentsChangeEvent} from its root component.
 * Every filter component added to the root after construction is automatically marked as
 * modified (so the per-condition remove button appears). Components removed from the root
 * are automatically unmarked. Explicit calls to {@link #setModified(boolean)} and
 * {@link #setFilterComponentModified(FilterComponent, boolean)} still work as before.
 */
public class RunTimeConfiguration implements MutableConfiguration {

    protected final String id;
    protected final GenericFilter owner;

    protected String name;
    protected boolean availableForAllUsers;
    protected LogicalFilterComponent<?> rootLogicalFilterComponent;
    protected Set<FilterComponent> modifiedFilterComponents = new HashSet<>();
    protected Map<String, Object> defaultValuesMap = new HashMap<>();

    /**
     * Tracks the set of direct children of the root component as seen at the last
     * {@link LogicalFilterComponent.FilterComponentsChangeEvent}. Used to detect which
     * components were added or removed so their modified state can be updated automatically.
     */
    protected Set<FilterComponent> trackedComponents = new HashSet<>();

    public RunTimeConfiguration(String id, LogicalFilterComponent<?> rootLogicalFilterComponent, GenericFilter owner) {
        this.id = id;
        this.rootLogicalFilterComponent = rootLogicalFilterComponent;
        this.owner = owner;
        // Snapshot the current direct children so that only components added *after*
        // construction are auto-marked as modified.
        this.trackedComponents = new HashSet<>(rootLogicalFilterComponent.getOwnFilterComponents());
        // Subscribe to changes in the root component's direct children.
        rootLogicalFilterComponent.addFilterComponentsChangeListener(
                event -> syncModifiedStateFromRoot());
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

    /**
     * Synchronises the modified state with the current direct children of the root.
     * Called automatically when a {@link LogicalFilterComponent.FilterComponentsChangeEvent}
     * fires on the root component.
     * <ul>
     *   <li>Components that appeared since the last snapshot → marked as modified
     *       (so the remove button becomes visible).</li>
     *   <li>Components that disappeared → removed from the modified set.</li>
     * </ul>
     */
    protected void syncModifiedStateFromRoot() {
        Set<FilterComponent> currentComponents =
                new HashSet<>(rootLogicalFilterComponent.getOwnFilterComponents());

        // Newly added components → mark as modified (recursively for nested groups).
        for (FilterComponent fc : currentComponents) {
            if (!trackedComponents.contains(fc)) {
                setFilterComponentModified(fc, true);
            }
        }

        // Removed components → unmark (recursively for nested groups).
        for (FilterComponent fc : trackedComponents) {
            if (!currentComponents.contains(fc)) {
                setFilterComponentModified(fc, false);
            }
        }

        trackedComponents = currentComponents;
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
    public boolean isAvailableForAllUsers() {
        return availableForAllUsers;
    }

    @Override
    public void setAvailableForAllUsers(boolean availableForAllUsers) {
        this.availableForAllUsers = availableForAllUsers;
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
