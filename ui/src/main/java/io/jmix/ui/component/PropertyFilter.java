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

package io.jmix.ui.component;

import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.ui.model.DataLoader;

/**
 * PropertyFilter is a UI component used for filtering entities returned by the {@link DataLoader}. The component is
 * related to entity property and can automatically render proper layout for setting a condition value. In general case
 * a PropertyFilter layout contains a label with entity property caption, operation label (=, contains, &#62;, etc.) and
 * a field for editing a property value.
 */
public interface PropertyFilter<V> extends Component, Component.BelongToFrame, HasValue<V>, Component.HasCaption {

    String NAME = "propertyFilter";

    /**
     * @return a {@link DataLoader} related to the current PropertyFilter
     */
    DataLoader getDataLoader();

    void setDataLoader(DataLoader dataLoader);

    /**
     * @return related entity property name
     */
    String getProperty();

    void setProperty(String property);

    /**
     * @return a filtering operation
     * @see PropertyCondition.Operation
     */
    String getOperation();

    void setOperation(String operation);

    /**
     * @return a position of the caption
     */
    CaptionPosition getCaptionPosition();

    void setCaptionPosition(CaptionPosition captionPosition);

    /**
     * @return true if the operation label is visible
     */
    boolean isOperationCaptionVisible();

    void setOperationCaptionVisible(boolean operationCaptionVisible);

    /**
     * @return the name of the associated query parameter name
     */
    String getParameterName();

    void setParameterName(String parameterName);

    HasValue<V> getValueComponent();

    void setValueComponent(HasValue<V> valueComponent);

    String getCaptionWidth();

    void setCaptionWidth(String captionWidth);

    /**
     * @return associated {@link PropertyCondition} object.
     */
    PropertyCondition getPropertyCondition();

    void setPropertyCondition(PropertyCondition propertyCondition);

    /**
     * @return true if the filter should be automatically applied to the {@link DataLoader} when the value component
     * value is changed
     */
    boolean isAutoApply();

    void setAutoApply(boolean autoApply);

    /**
     * Method builds the component layout after all required properties are set
     */
    void createLayout();

    /**
     * Caption position of the value component.
     */
    enum CaptionPosition {
        /**
         * Component caption will be placed on the left side of component.
         */
        LEFT,

        /**
         * Component caption will be placed above the component.
         */
        TOP
    }
}
