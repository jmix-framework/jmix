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

import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.model.DataLoader;

/**
 * Component which cannot contain other filter component but can be used for filtering entities
 * returned by the {@link DataLoader}.
 *
 * @param <V> value type
 * @see PropertyFilter
 * @see JpqlFilter
 * @see LogicalFilterComponent
 */
@StudioProperties(
        properties = {
                @StudioProperty(name = "defaultValue", type = PropertyType.STRING)
        }
)
public interface SingleFilterComponent<V> extends FilterComponent, Component.BelongToFrame, HasValue<V>,
        Component.HasCaption, Component.HasIcon, Component.Focusable, Component.Editable, HasHtmlCaption,
        HasHtmlDescription, HasHtmlSanitizer, SupportsCaptionPosition, Requirable, Validatable,
        HasContextHelp {

    /**
     * @return the name of the associated query parameter name
     */
    String getParameterName();

    /**
     * Sets the name of the associated query parameter name.
     *
     * @param parameterName a name of the associated query parameter name
     */
    @StudioProperty(type = PropertyType.STRING)
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
    @StudioProperty(type = PropertyType.SIZE, defaultValue = "100%")
    void setCaptionWidth(String captionWidth);

    /**
     * @return {@code true} if caption is visible
     */
    boolean isCaptionVisible();

    /**
     * Sets caption visibility. Default value is {@code true}.
     *
     * @param captionVisible whether to show caption or not
     */
    @StudioProperty(type = PropertyType.BOOLEAN, defaultValue = "true")
    void setCaptionVisible(boolean captionVisible);
}
