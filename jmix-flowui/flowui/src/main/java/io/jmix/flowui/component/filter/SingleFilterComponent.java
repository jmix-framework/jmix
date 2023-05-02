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

package io.jmix.flowui.component.filter;

import com.vaadin.flow.component.HasValueAndElement;
import io.jmix.flowui.component.jpqlfilter.JpqlFilter;
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.model.DataLoader;

import javax.annotation.Nullable;

/**
 * Component which cannot contain other filter component but can be used for filtering entities
 * returned by the {@link DataLoader}.
 *
 * @param <V> value type
 * @see PropertyFilter
 * @see JpqlFilter
 * @see LogicalFilterComponent
 */
public interface SingleFilterComponent<V> extends FilterComponent {

    /**
     * @return the name of the associated query parameter name
     */
    String getParameterName();

    /**
     * Sets the name of the associated query parameter name.
     *
     * @param parameterName a name of the associated query parameter name
     */
    void setParameterName(String parameterName);

    /**
     * @return a field for editing a property value
     */
    HasValueAndElement<?, V> getValueComponent();

    /**
     * Sets the field for editing a property value.
     *
     * @param valueComponent a field for editing a property value
     */
    void setValueComponent(HasValueAndElement<?, V> valueComponent);

    /**
     * @return the width which has been set for the label
     */
    @Nullable
    String getLabelWidth();

    /**
     * Sets the label width.
     * <p>
     * The width should be in a format understood by the browser, e.g. "100px"
     * or "2.5em".
     * <p>
     * If the provided {@code width} value is {@literal null} then width is
     * removed.
     *
     * @param labelWidth the width to set, may be {@code null}
     */
    void setLabelWidth(@Nullable String labelWidth);

    /**
     * @return {@code true} if label is visible
     */
    boolean isLabelVisible();

    /**
     * Sets label visibility. Default value is {@code true}.
     *
     * @param labelVisible whether to show label or not
     */
    void setLabelVisible(boolean labelVisible);
}
