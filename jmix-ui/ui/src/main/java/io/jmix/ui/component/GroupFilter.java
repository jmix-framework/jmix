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

import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.UiComponentProperties;

/**
 * GroupFilter is a UI component that has a {@link GroupBoxLayout} with a {@link ResponsiveGridLayout}
 * as its root element. This component can contain {@link FilterComponent}s and can be used for filtering entities
 * returned by the {@link DataLoader}.
 */
@StudioElement(
        caption = "GroupFilter",
        xmlElement = "groupFilter",
        defaultProperty = "operation",
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/filter-components.html#group-filter",
        unsupportedProperties = {"dataLoader", "autoApply", "captionPosition", "columnsCount", "contextHelpText",
                "contextHelpTextHtmlEnabled", "width"},
        icon = "io/jmix/ui/icon/component/groupFilter.svg"
)
public interface GroupFilter extends LogicalFilterComponent, Component.BelongToFrame, CompositeWithHtmlCaption,
        CompositeWithHtmlDescription, CompositeWithIcon, CompositeWithContextHelp, HasHtmlSanitizer,
        SupportsCaptionPosition, SupportsColumnsCount {

    String NAME = "groupFilter";

    /**
     * @return caption position of logical filter child components
     */
    @Override
    CaptionPosition getCaptionPosition();

    /**
     * Sets caption position of logical filter child components.
     *
     * <ul>
     *     <li>{@link CaptionPosition#LEFT} - component captions will be placed
     *     in a separate column on the left side of the components</li>
     *     <li>{@link CaptionPosition#TOP} - component captions will be placed
     *     above the components</li>
     * </ul>
     *
     * @param position caption position of logical filter child components
     */
    @Override
    void setCaptionPosition(CaptionPosition position);

    /**
     * Returns the number of columns to be displayed on one row.
     * The default value is taken from {@link UiComponentProperties#getFilterColumnsCount()}.
     *
     * @return the number of columns to be displayed on one row
     */
    @Override
    int getColumnsCount();

    /**
     * Sets the number of columns to be displayed on one row.
     * The default value is taken from {@link UiComponentProperties#getFilterColumnsCount()}.
     *
     * @param columnsCount the number of columns to be displayed on one row
     */
    @Override
    void setColumnsCount(int columnsCount);
}
