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

import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.FilterComponent;
import io.jmix.ui.component.LogicalFilterComponent;

import javax.annotation.Nullable;

public class DesignTimeConfiguration implements Filter.Configuration {

    protected final String code;
    protected final String caption;
    protected final LogicalFilterComponent rootLogicalFilterComponent;
    protected final Filter owner;


    public DesignTimeConfiguration(String code,
                                   @Nullable String caption,
                                   LogicalFilterComponent rootLogicalFilterComponent,
                                   Filter owner) {
        this.code = code;
        this.caption = caption;
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
        throw new UnsupportedOperationException("You cannot set caption attribute for design-time configuration. " +
                "Use FilterCopyAction to create a modifiable copy of configuration");
    }

    @Override
    public LogicalFilterComponent getRootLogicalFilterComponent() {
        return rootLogicalFilterComponent;
    }

    @Override
    public void setRootLogicalFilterComponent(LogicalFilterComponent rootLogicalFilterComponent) {
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
    public boolean isModified(FilterComponent filterComponent) {
        return false;
    }

    @Override
    public void setModified(FilterComponent filterComponent, boolean modified) {
        throw new UnsupportedOperationException("You cannot set modified attribute for design-time configuration. " +
                "Use FilterCopyAction to create a modifiable copy of configuration");
    }
}
