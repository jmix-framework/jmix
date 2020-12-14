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

package io.jmix.ui.component.filter.configuration;

import io.jmix.core.common.util.Preconditions;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.FilterComponent;
import io.jmix.ui.component.LogicalFilterComponent;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public class RunTimeConfiguration implements Filter.Configuration {

    protected final String code;
    protected final Filter owner;

    protected String caption;
    protected LogicalFilterComponent rootLogicalFilterComponent;
    protected Set<FilterComponent> modifiedFilterComponents = new HashSet<>();

    public RunTimeConfiguration(String code, LogicalFilterComponent rootLogicalFilterComponent, Filter owner) {
        this.code = code;
        this.rootLogicalFilterComponent = rootLogicalFilterComponent;
        this.owner = owner;
    }

    @Override
    public Filter getOwner() {
        return owner;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getCaption() {
        return caption != null
                ? caption
                : code;
    }

    @Override
    public void setCaption(@Nullable String caption) {
        this.caption = caption;
    }

    @Override
    public LogicalFilterComponent getRootLogicalFilterComponent() {
        return rootLogicalFilterComponent;
    }

    @Override
    public void setRootLogicalFilterComponent(LogicalFilterComponent rootLogicalFilterComponent) {
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
            setModified(filterComponent, modified);
        }
    }

    @Override
    public boolean isModified(FilterComponent filterComponent) {
        return modifiedFilterComponents.contains(filterComponent);
    }

    @Override
    public void setModified(FilterComponent filterComponent, boolean modified) {
        if (modified) {
            modifiedFilterComponents.add(filterComponent);
        } else {
            modifiedFilterComponents.remove(filterComponent);
        }

        if (filterComponent instanceof LogicalFilterComponent) {
            for (FilterComponent ownFilterComponent : ((LogicalFilterComponent) filterComponent).getOwnFilterComponents()) {
                setModified(ownFilterComponent, modified);
            }
        }
    }
}
