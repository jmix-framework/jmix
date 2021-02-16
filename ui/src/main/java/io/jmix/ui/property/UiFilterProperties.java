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

package io.jmix.ui.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Properties related to {@link io.jmix.ui.component.Filter} component
 */
@ConfigurationProperties(prefix = "jmix.ui.filter")
@ConstructorBinding
public class UiFilterProperties {

    boolean autoApply;
    int propertiesHierarchyDepth;
    int columnsCount;
    boolean showConfigurationIdField;

    public UiFilterProperties(
            @DefaultValue("true") boolean autoApply,
            @DefaultValue("2") int propertiesHierarchyDepth,
            @DefaultValue("3") int columnsCount,
            @DefaultValue("false") boolean showConfigurationIdField) {
        this.autoApply = autoApply;
        this.propertiesHierarchyDepth = propertiesHierarchyDepth;
        this.columnsCount = columnsCount;
        this.showConfigurationIdField = showConfigurationIdField;
    }

    /**
     * @return default value for the autoApply attribute of the {@link io.jmix.ui.component.Filter} component
     */
    public boolean isAutoApply() {
        return autoApply;
    }

    /**
     * @return a number of nested properties in the {@link io.jmix.ui.app.filter.condition.AddConditionScreen}.
     * I.e. if the depth is 2, then you'll be able to select a property "contractor.city.country",
     * if the value is 3, then "contractor.city.country.name", etc.
     */
    public int getPropertiesHierarchyDepth() {
        return propertiesHierarchyDepth;
    }

    /**
     * @return the number of columns to be displayed on one row in {@link io.jmix.ui.component.Filter}
     */
    public int getColumnsCount() {
        return columnsCount;
    }

    /**
     * @return whether field for filter configuration id should be visible in the filter configuration edit screens
     */
    public boolean isShowConfigurationIdField() {
        return showConfigurationIdField;
    }
}
