/*
 * Copyright 2019 Haulmont.
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

import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.PropertiesConstraint;
import io.jmix.ui.meta.PropertiesGroup;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;

import javax.annotation.Nullable;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

/**
 * A component with two lists: left list for available options, right list for selected values.
 *
 * @param <V> value and options type for the component
 */
@StudioComponent(
        caption = "TwinColumn",
        category = "Components",
        xmlElement = "twinColumn",
        icon = "io/jmix/ui/icon/component/twinColumn.svg",
        canvasBehaviour = CanvasBehaviour.TWIN_COLUMN,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/twin-column.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "dataContainer", type = PropertyType.DATACONTAINER_REF),
                @StudioProperty(name = "property", type = PropertyType.PROPERTY_PATH_REF, typeParameter = "V"),
                @StudioProperty(name = "optionsContainer", type = PropertyType.COLLECTION_DATACONTAINER_REF,
                        typeParameter = "V"),
                @StudioProperty(name = "captionProperty", type = PropertyType.PROPERTY_PATH_REF)
        },
        groups = {
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "property"}),
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"optionsContainer", "captionProperty"}),
        }
)
public interface TwinColumn<V> extends OptionsField<Collection<V>, V>, Component.Focusable, HasOptionStyleProvider<V> {

    String NAME = "twinColumn";

    /**
     * @return the number of visible rows
     */
    int getRows();

    /**
     * Sets the number of visible rows.
     *
     * @param rows number of visible rows
     */
    @StudioProperty(defaultValue = "0")
    @PositiveOrZero
    void setRows(int rows);

    /**
     * Allows you to configure whether items should be reordered after selection.
     * <p>
     * Reordering is enabled by default.
     *
     * @param reorderable pass 'true' to enable reordering or 'false' otherwise
     */
    void setReorderable(boolean reorderable);

    /**
     * @return true if items are reordered or false otherwise
     */
    boolean isReorderable();

    /**
     * Enables "Add all" and "Remove all" buttons.
     *
     * @param enabled true if buttons should be enabled
     */
    @StudioProperty(name = "addAllBtnEnabled", defaultValue = "false")
    void setAddAllBtnEnabled(boolean enabled);

    /**
     * @return true if buttons are enabled
     */
    boolean isAddAllBtnEnabled();

    /**
     * Sets caption for the left column.
     *
     * @param leftColumnCaption a caption for the left column
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING)
    void setLeftColumnCaption(@Nullable String leftColumnCaption);

    /**
     * Returns caption of the left column.
     *
     * @return caption text or null if not set.
     */
    @Nullable
    String getLeftColumnCaption();

    /**
     * Sets caption for the right column.
     *
     * @param rightColumnCaption a caption for the right column
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING)
    void setRightColumnCaption(@Nullable String rightColumnCaption);

    /**
     * Returns caption of the right column.
     *
     * @return caption text or null if not set.
     */
    @Nullable
    String getRightColumnCaption();
}
