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

import io.jmix.ui.model.DataLoader;

/**
 * PropertyFilter is a UI component used for filtering entities returned by the {@link DataLoader}. The component is
 * related to entity property and can automatically render proper layout for setting a condition value. In general case
 * a PropertyFilter layout contains a label with entity property caption, operation label or selector (=, contains,
 * &#62;, etc.) and a field for editing a property value.
 */
public interface PropertyFilter<V> extends Component, Component.BelongToFrame, HasValue<V>,
        Component.HasCaption, Component.HasIcon, Component.Focusable, Component.Editable,
        HasHtmlCaption, HasHtmlDescription, HasHtmlSanitizer, SupportsCaptionPosition {

    String NAME = "propertyFilter";

    /**
     * @return a {@link DataLoader} related to the current PropertyFilter
     */
    DataLoader getDataLoader();

    /**
     * Sets a {@link DataLoader} related to the current PropertyFilter.
     *
     * @param dataLoader a {@link DataLoader} to set
     */
    void setDataLoader(DataLoader dataLoader);

    /**
     * @return related entity property name
     */
    String getProperty();

    /**
     * Sets related entity property name.
     *
     * @param property entity property name
     */
    void setProperty(String property);

    /**
     * @return a filtering operation
     */
    Operation getOperation();

    /**
     * Sets a filtering operation.
     *
     * @param operation a filtering operation
     */
    void setOperation(Operation operation);

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
    HasValue<V> getValueComponent();

    /**
     * Sets the field for editing a property value.
     *
     * @param valueComponent a field for editing a property value
     */
    void setValueComponent(HasValue<V> valueComponent);

    /**
     * @return whether an operation selector is visible.
     */
    boolean isOperationEditable();

    /**
     * Sets whether an operation selector is visible.
     *
     * @param operationEditable whether an operation selector is visible
     */
    void setOperationEditable(boolean operationEditable);

    /**
     * @return a caption width value in {@link #getWidthSizeUnit()}
     */
    float getCaptionWidth();

    /**
     * @return units used in the caption width property
     */
    SizeUnit getCaptionWidthSizeUnit();

    /**
     * Sets the caption width.
     *
     * @param captionWidth a string width representation
     */
    void setCaptionWidth(String captionWidth);

    /**
     * @return {@code true} if the filter should be automatically applied to the
     * {@link DataLoader} when the value component value is changed
     */
    boolean isAutoApply();

    /**
     * Sets whether the filter should be automatically applied to the {@link DataLoader}
     * when the value component value is changed.
     *
     * @param autoApply {@code true} if the filter should be automatically applied to the
     *                  {@link DataLoader} when the value component value is changed
     */
    void setAutoApply(boolean autoApply);

    /**
     * Operation representing corresponding filtering condition.
     */
    enum Operation {
        EQUAL(Type.VALUE),
        NOT_EQUAL(Type.VALUE),
        GREATER(Type.VALUE),
        GREATER_OR_EQUAL(Type.VALUE),
        LESS(Type.VALUE),
        LESS_OR_EQUAL(Type.VALUE),
        CONTAINS(Type.VALUE),
        NOT_CONTAINS(Type.VALUE),
        STARTS_WITH(Type.VALUE),
        ENDS_WITH(Type.VALUE),
        IS_SET(Type.UNARY),
        IS_NOT_SET(Type.UNARY),
//        IN_LIST(Type.LIST),
//        NOT_IN_LIST(Type.LIST),
//        DATE_INTERVAL(Type.INTERVAL),
        ;

        private final Type type;

        Operation(Type type) {
            this.type = type;
        }

        public Type getType() {
            return type;
        }

        /**
         * Operation type representing the required field type for editing
         * a property value with the given operation.
         */
        public enum Type {

            /**
             * Requires a field suitable for editing a property value, e.g.
             * {@link TextField} for String, {@link ComboBox} for enum.
             */
            VALUE,

            /**
             * Requires a field suitable for choosing unary value, e.g. true/false, YES/NO.
             */
            UNARY,

            /**
             * Requires a field suitable for selecting multiple values of
             * the same type as the property value.
             */
            LIST,

            /**
             * Requires a field suitable for selecting a range of values of
             * the same type as the property value.
             */
            INTERVAL
        }
    }
}
