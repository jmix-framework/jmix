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

package io.jmix.ui.component.impl;


import com.vaadin.event.MouseEvents;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.shared.ui.Orientation;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.shared.ui.datefield.DateResolution;
import com.vaadin.shared.ui.grid.ColumnResizeMode;
import com.vaadin.shared.ui.grid.GridStaticCellType;
import com.vaadin.shared.ui.grid.ScrollDestination;
import com.vaadin.shared.ui.slider.SliderOrientation;
import com.vaadin.ui.Dependency;
import com.vaadin.v7.shared.ui.combobox.FilteringMode;
import io.jmix.core.annotation.Internal;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.ui.component.*;
import io.jmix.ui.component.Component.Alignment;
import io.jmix.ui.component.DataGrid.DataGridStaticCellType;
import io.jmix.ui.component.HasFilterMode.FilterMode;
import io.jmix.ui.widget.JmixResponsiveGridLayout.*;
import io.jmix.ui.widget.client.fieldgrouplayout.CaptionAlignment;
import io.jmix.ui.widget.client.popupview.PopupPosition;
import io.jmix.ui.widget.client.resizabletextarea.ResizeDirection;
import io.jmix.ui.widget.client.timefield.TimeMode;
import io.jmix.ui.widget.client.timefield.TimeResolution;
import io.jmix.ui.widget.data.AggregationContainer;

import javax.annotation.Nullable;

import static com.vaadin.v7.ui.AbstractTextField.TextChangeEventMode;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Convenient class for methods that converts values from Vaadin to Jmix instances and vice versa.
 */
@Internal
public final class WrapperUtils {

    public static final String AUTO_SIZE = "AUTO";

    private WrapperUtils() {
    }

    public static ContentMode toContentMode(com.vaadin.shared.ui.ContentMode contentMode) {
        checkNotNullArgument(contentMode);

        switch (contentMode) {
            case TEXT:
                return ContentMode.TEXT;
            case PREFORMATTED:
                return ContentMode.PREFORMATTED;
            case HTML:
                return ContentMode.HTML;
            default:
                throw new IllegalArgumentException("Unknown content mode: " + contentMode);
        }
    }

    public static com.vaadin.shared.ui.ContentMode toVaadinContentMode(ContentMode contentMode) {
        checkNotNullArgument(contentMode);

        switch (contentMode) {
            case TEXT:
                return com.vaadin.shared.ui.ContentMode.TEXT;
            case PREFORMATTED:
                return com.vaadin.shared.ui.ContentMode.PREFORMATTED;
            case HTML:
                return com.vaadin.shared.ui.ContentMode.HTML;
            default:
                throw new IllegalArgumentException("Unknown content mode: " + contentMode);
        }
    }

    @Nullable
    public static FilteringMode toVaadinFilterMode(@Nullable FilterMode filterMode) {
        if (filterMode == null) {
            return null;
        }

        switch (filterMode) {
            case NO:
                return FilteringMode.OFF;
            case STARTS_WITH:
                return FilteringMode.STARTSWITH;
            case CONTAINS:
                return FilteringMode.CONTAINS;
            default:
                throw new UnsupportedOperationException("Unsupported FilterMode");
        }
    }

    @Nullable
    public static FilterMode toFilterMode(@Nullable FilteringMode filterMode) {
        if (filterMode == null) {
            return null;
        }

        switch (filterMode) {
            case OFF:
                return FilterMode.NO;
            case CONTAINS:
                return FilterMode.CONTAINS;
            case STARTSWITH:
                return FilterMode.STARTS_WITH;
            default:
                throw new UnsupportedOperationException("Unsupported Vaadin FilteringMode");
        }
    }

    @Nullable
    public static com.vaadin.ui.Alignment toVaadinAlignment(@Nullable Alignment alignment) {
        if (alignment == null) {
            return null;
        }

        switch (alignment) {
            case TOP_LEFT:
                return com.vaadin.ui.Alignment.TOP_LEFT;
            case TOP_CENTER:
                return com.vaadin.ui.Alignment.TOP_CENTER;
            case TOP_RIGHT:
                return com.vaadin.ui.Alignment.TOP_RIGHT;
            case MIDDLE_LEFT:
                return com.vaadin.ui.Alignment.MIDDLE_LEFT;
            case MIDDLE_CENTER:
                return com.vaadin.ui.Alignment.MIDDLE_CENTER;
            case MIDDLE_RIGHT:
                return com.vaadin.ui.Alignment.MIDDLE_RIGHT;
            case BOTTOM_LEFT:
                return com.vaadin.ui.Alignment.BOTTOM_LEFT;
            case BOTTOM_CENTER:
                return com.vaadin.ui.Alignment.BOTTOM_CENTER;
            case BOTTOM_RIGHT:
                return com.vaadin.ui.Alignment.BOTTOM_RIGHT;
            default:
                throw new UnsupportedOperationException("Unsupported Alignment");
        }
    }

    public static TextInputField.TextChangeEventMode toTextChangeEventMode(ValueChangeMode mode) {
        checkNotNullArgument(mode);

        switch (mode) {
            case BLUR:
                return TextInputField.TextChangeEventMode.BLUR;
            case EAGER:
                return TextInputField.TextChangeEventMode.EAGER;
            case LAZY:
                return TextInputField.TextChangeEventMode.LAZY;
            case TIMEOUT:
                return TextInputField.TextChangeEventMode.TIMEOUT;
            default:
                throw new UnsupportedOperationException("Unsupported Vaadin TextChangeEventMode");
        }
    }

    public static TextInputField.TextChangeEventMode toTextChangeEventMode(TextChangeEventMode mode) {
        checkNotNullArgument(mode);

        switch (mode) {
            case EAGER:
                return TextInputField.TextChangeEventMode.EAGER;
            case LAZY:
                return TextInputField.TextChangeEventMode.LAZY;
            case TIMEOUT:
                return TextInputField.TextChangeEventMode.TIMEOUT;
            default:
                throw new UnsupportedOperationException("Unsupported Vaadin TextChangeEventMode");
        }
    }

    @Nullable
    public static ValueChangeMode toVaadinValueChangeEventMode(@Nullable TextInputField.TextChangeEventMode mode) {
        if (mode == null) {
            return null;
        }

        ValueChangeMode vMode;
        switch (mode) {
            case BLUR:
                vMode = ValueChangeMode.BLUR;
                break;
            case EAGER:
                vMode = ValueChangeMode.EAGER;
                break;
            case LAZY:
                vMode = ValueChangeMode.LAZY;
                break;
            case TIMEOUT:
                vMode = ValueChangeMode.TIMEOUT;
                break;
            default:
                throw new UnsupportedOperationException("Unsupported TextChangeEventMode");
        }

        return vMode;
    }

    @Nullable
    public static TextChangeEventMode toVaadinTextChangeEventMode(@Nullable TextInputField.TextChangeEventMode mode) {
        if (mode == null) {
            return null;
        }

        TextChangeEventMode vMode;
        switch (mode) {
            case EAGER:
                vMode = TextChangeEventMode.EAGER;
                break;
            case LAZY:
                vMode = TextChangeEventMode.LAZY;
                break;
            case TIMEOUT:
                vMode = TextChangeEventMode.TIMEOUT;
                break;
            default:
                throw new UnsupportedOperationException("Unsupported TextChangeEventMode");
        }

        return vMode;
    }

    public static MouseEventDetails toMouseEventDetails(MouseEvents.ClickEvent event) {
        checkNotNullArgument(event);

        MouseEventDetails mouseEventDetails = new MouseEventDetails();
        mouseEventDetails.setButton(toMouseButton(event.getButton()));
        mouseEventDetails.setClientX(event.getClientX());
        mouseEventDetails.setClientY(event.getClientY());
        mouseEventDetails.setAltKey(event.isAltKey());
        mouseEventDetails.setCtrlKey(event.isCtrlKey());
        mouseEventDetails.setMetaKey(event.isMetaKey());
        mouseEventDetails.setShiftKey(event.isShiftKey());
        mouseEventDetails.setDoubleClick(event.isDoubleClick());
        mouseEventDetails.setRelativeX(event.getRelativeX());
        mouseEventDetails.setRelativeY(event.getRelativeY());

        return mouseEventDetails;
    }

    public static MouseEventDetails toMouseEventDetails(com.vaadin.shared.MouseEventDetails vMouseEventDetails) {
        checkNotNullArgument(vMouseEventDetails);

        MouseEventDetails mouseEventDetails = new MouseEventDetails();
        mouseEventDetails.setButton(toMouseButton(vMouseEventDetails.getButton()));
        mouseEventDetails.setClientX(vMouseEventDetails.getClientX());
        mouseEventDetails.setClientY(vMouseEventDetails.getClientY());
        mouseEventDetails.setAltKey(vMouseEventDetails.isAltKey());
        mouseEventDetails.setCtrlKey(vMouseEventDetails.isCtrlKey());
        mouseEventDetails.setMetaKey(vMouseEventDetails.isMetaKey());
        mouseEventDetails.setShiftKey(vMouseEventDetails.isShiftKey());
        mouseEventDetails.setDoubleClick(vMouseEventDetails.isDoubleClick());
        mouseEventDetails.setRelativeX(vMouseEventDetails.getRelativeX());
        mouseEventDetails.setRelativeY(vMouseEventDetails.getRelativeY());

        return mouseEventDetails;
    }

    @Nullable
    public static MouseEventDetails.MouseButton toMouseButton(@Nullable com.vaadin.shared.MouseEventDetails.MouseButton mouseButton) {
        if (mouseButton == null) {
            return null;
        }

        switch (mouseButton) {
            case LEFT:
                return MouseEventDetails.MouseButton.LEFT;
            case MIDDLE:
                return MouseEventDetails.MouseButton.MIDDLE;
            case RIGHT:
                return MouseEventDetails.MouseButton.RIGHT;
            default:
                throw new UnsupportedOperationException("Unsupported Vaadin MouseButton");
        }
    }

    public static DataGridStaticCellType toDataGridStaticCellType(GridStaticCellType cellType) {
        checkNotNullArgument(cellType);

        switch (cellType) {
            case HTML:
                return DataGridStaticCellType.HTML;
            case TEXT:
                return DataGridStaticCellType.TEXT;
            case WIDGET:
                return DataGridStaticCellType.COMPONENT;
            default:
                throw new UnsupportedOperationException("Unsupported GridStaticCellType");
        }
    }

    public static ResizeDirection toVaadinResizeDirection(ResizableTextArea.ResizeDirection direction) {
        switch (direction) {
            case BOTH:
                return ResizeDirection.BOTH;
            case VERTICAL:
                return ResizeDirection.VERTICAL;
            case HORIZONTAL:
                return ResizeDirection.HORIZONTAL;
            case NONE:
                return ResizeDirection.NONE;
            default:
                throw new IllegalArgumentException("Unknown direction: " + direction);
        }
    }

    public static ResizableTextArea.ResizeDirection toResizeDirection(ResizeDirection direction) {
        switch (direction) {
            case BOTH:
                return ResizableTextArea.ResizeDirection.BOTH;
            case VERTICAL:
                return ResizableTextArea.ResizeDirection.VERTICAL;
            case HORIZONTAL:
                return ResizableTextArea.ResizeDirection.HORIZONTAL;
            case NONE:
                return ResizableTextArea.ResizeDirection.NONE;
            default:
                throw new IllegalArgumentException("Unknown direction: " + direction);
        }
    }

    public static Sizeable.Unit toVaadinUnit(SizeUnit sizeUnit) {
        checkNotNullArgument(sizeUnit);

        switch (sizeUnit) {
            case PIXELS:
                return Sizeable.Unit.PIXELS;
            case PERCENTAGE:
                return Sizeable.Unit.PERCENTAGE;
            default:
                throw new UnsupportedOperationException("Unsupported Size Unit");
        }
    }

    public static SizeUnit toSizeUnit(Sizeable.Unit units) {
        checkNotNullArgument(units);

        switch (units) {
            case PIXELS:
                return SizeUnit.PIXELS;
            case PERCENTAGE:
                return SizeUnit.PERCENTAGE;
            default:
                throw new UnsupportedOperationException("Unsupported Size Unit");
        }
    }

    public static PopupButton.PopupOpenDirection toPopupOpenDirection(com.vaadin.ui.Alignment alignment) {
        checkNotNullArgument(alignment);

        if (alignment == com.vaadin.ui.Alignment.BOTTOM_LEFT)
            return PopupButton.PopupOpenDirection.BOTTOM_LEFT;

        if (alignment == com.vaadin.ui.Alignment.BOTTOM_RIGHT)
            return PopupButton.PopupOpenDirection.BOTTOM_RIGHT;

        if (alignment == com.vaadin.ui.Alignment.BOTTOM_CENTER)
            return PopupButton.PopupOpenDirection.BOTTOM_CENTER;

        throw new UnsupportedOperationException("Unsupported alignment");
    }

    public static com.vaadin.ui.Alignment toVaadinAlignment(PopupButton.PopupOpenDirection direction) {
        checkNotNullArgument(direction);

        switch (direction) {
            case BOTTOM_CENTER:
                return com.vaadin.ui.Alignment.BOTTOM_CENTER;
            case BOTTOM_RIGHT:
                return com.vaadin.ui.Alignment.BOTTOM_RIGHT;
            case BOTTOM_LEFT:
                return com.vaadin.ui.Alignment.BOTTOM_LEFT;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Nullable
    public static com.vaadin.v7.ui.Table.Align convertColumnAlignment(@Nullable io.jmix.ui.component.Table.ColumnAlignment alignment) {
        if (alignment == null) {
            return null;
        }

        switch (alignment) {
            case LEFT:
                return com.vaadin.v7.ui.Table.Align.LEFT;
            case CENTER:
                return com.vaadin.v7.ui.Table.Align.CENTER;
            case RIGHT:
                return com.vaadin.v7.ui.Table.Align.RIGHT;
            default:
                throw new UnsupportedOperationException();
        }
    }

    public static AggregationContainer.Type convertAggregationType(AggregationInfo.Type function) {
        switch (function) {
            case COUNT:
                return AggregationContainer.Type.COUNT;
            case AVG:
                return AggregationContainer.Type.AVG;
            case MAX:
                return AggregationContainer.Type.MAX;
            case MIN:
                return AggregationContainer.Type.MIN;
            case SUM:
                return AggregationContainer.Type.SUM;
            case CUSTOM:
                return AggregationContainer.Type.CUSTOM;
            default:
                throw new IllegalArgumentException("Unknown function: " + function);
        }
    }

    public static DateResolution convertDateResolution(DatePicker.Resolution resolution) {
        switch (resolution) {
            case YEAR:
                return DateResolution.YEAR;
            case MONTH:
                return DateResolution.MONTH;
            case DAY:
            default:
                return DateResolution.DAY;
        }
    }

    public static DateResolution convertDateTimeResolution(DateField.Resolution resolution) {
        switch (resolution) {
            case YEAR:
                return DateResolution.YEAR;
            case MONTH:
                return DateResolution.MONTH;
            case DAY:
            case HOUR:
            case MIN:
            case SEC:
            default:
                return DateResolution.DAY;
        }
    }

    public static TimeResolution toVaadinTimeResolution(TimeField.Resolution resolution) {
        switch (resolution) {
            case SEC:
                return TimeResolution.SECOND;
            case HOUR:
                return TimeResolution.HOUR;
            case MIN:
            default:
                return TimeResolution.MINUTE;
        }
    }

    public static TimeResolution toVaadinTimeResolution(DateField.Resolution resolution) {
        switch (resolution) {
            case HOUR:
                return TimeResolution.HOUR;
            case MIN:
                return TimeResolution.MINUTE;
            case SEC:
                return TimeResolution.SECOND;
            default:
                throw new IllegalArgumentException("Can't be converted to TimeResolution: " + resolution);
        }
    }

    public static TimeField.Resolution fromVaadinTimeResolution(TimeResolution timeResolution) {
        switch (timeResolution) {
            case HOUR:
                return TimeField.Resolution.HOUR;
            case MINUTE:
                return TimeField.Resolution.MIN;
            case SECOND:
                return TimeField.Resolution.SEC;
            default:
                throw new IllegalArgumentException("Can't be converted to TimeField.Resolution: " + timeResolution);
        }
    }

    public static DataGrid.ColumnResizeMode convertToDataGridColumnResizeMode(ColumnResizeMode mode) {
        checkNotNullArgument(mode);

        switch (mode) {
            case ANIMATED:
                return DataGrid.ColumnResizeMode.ANIMATED;
            case SIMPLE:
                return DataGrid.ColumnResizeMode.SIMPLE;
            default:
                throw new IllegalArgumentException("Can't be converted to ColumnResizeMode: " + mode);
        }
    }

    public static ColumnResizeMode convertToGridColumnResizeMode(DataGrid.ColumnResizeMode mode) {
        checkNotNullArgument(mode);

        switch (mode) {
            case ANIMATED:
                return ColumnResizeMode.ANIMATED;
            case SIMPLE:
                return ColumnResizeMode.SIMPLE;
            default:
                throw new IllegalArgumentException("Can't be converted to ColumnResizeMode: " + mode);
        }
    }

    public static SortDirection convertToGridSortDirection(DataGrid.SortDirection sortDirection) {
        checkNotNullArgument(sortDirection);

        switch (sortDirection) {
            case ASCENDING:
                return SortDirection.ASCENDING;
            case DESCENDING:
                return SortDirection.DESCENDING;
            default:
                throw new IllegalArgumentException("Can't be converted to SortDirection: " + sortDirection);
        }
    }

    public static DataGrid.SortDirection convertToDataGridSortDirection(SortDirection sortDirection) {
        checkNotNullArgument(sortDirection);

        switch (sortDirection) {
            case ASCENDING:
                return DataGrid.SortDirection.ASCENDING;
            case DESCENDING:
                return DataGrid.SortDirection.DESCENDING;
            default:
                throw new IllegalArgumentException("Can't be converted to SortDirection: " + sortDirection);
        }
    }

    public static ScrollDestination convertToGridScrollDestination(DataGrid.ScrollDestination destination) {
        checkNotNullArgument(destination);

        switch (destination) {
            case ANY:
                return ScrollDestination.ANY;
            case START:
                return ScrollDestination.START;
            case MIDDLE:
                return ScrollDestination.MIDDLE;
            case END:
                return ScrollDestination.END;
            default:
                throw new IllegalArgumentException("Can't be converted to ScrollDestination: " + destination);
        }
    }

    public static HasOrientation.Orientation convertToOrientation(Orientation orientation) {
        checkNotNullArgument(orientation);

        switch (orientation) {
            case VERTICAL:
                return HasOrientation.Orientation.VERTICAL;
            case HORIZONTAL:
                return HasOrientation.Orientation.HORIZONTAL;
            default:
                throw new IllegalArgumentException("Can't be converted to HasOrientation.Orientation: " + orientation);
        }
    }

    public static Orientation convertToVaadinOrientation(HasOrientation.Orientation orientation) {
        checkNotNullArgument(orientation);

        switch (orientation) {
            case VERTICAL:
                return Orientation.VERTICAL;
            case HORIZONTAL:
                return Orientation.HORIZONTAL;
            default:
                throw new IllegalArgumentException("Can't be converted to Orientation: " + orientation);
        }
    }

    public static HasOrientation.Orientation fromVaadinSliderOrientation(SliderOrientation orientation) {
        checkNotNullArgument(orientation);

        switch (orientation) {
            case VERTICAL:
                return HasOrientation.Orientation.VERTICAL;
            case HORIZONTAL:
                return HasOrientation.Orientation.HORIZONTAL;
            default:
                throw new IllegalArgumentException("Can't be converted to HasOrientation.Orientation: " + orientation);
        }
    }

    public static SliderOrientation toVaadinSliderOrientation(HasOrientation.Orientation orientation) {
        checkNotNullArgument(orientation);

        switch (orientation) {
            case VERTICAL:
                return SliderOrientation.VERTICAL;
            case HORIZONTAL:
                return SliderOrientation.HORIZONTAL;
            default:
                throw new IllegalArgumentException("Can't be converted to SliderOrientation: " + orientation);
        }
    }

    public static CaptionAlignment toVaadinFieldGroupCaptionAlignment(Form.CaptionAlignment alignment) {
        checkNotNullArgument(alignment);

        switch (alignment) {
            case LEFT:
                return CaptionAlignment.LEFT;
            case RIGHT:
                return CaptionAlignment.RIGHT;
            default:
                throw new IllegalArgumentException("Can't be converted to CaptionAlignment " + alignment);
        }
    }

    public static Form.CaptionAlignment fromVaadinFieldGroupCaptionAlignment(CaptionAlignment alignment) {
        checkNotNullArgument(alignment);

        switch (alignment) {
            case LEFT:
                return Form.CaptionAlignment.LEFT;
            case RIGHT:
                return Form.CaptionAlignment.RIGHT;
            default:
                throw new IllegalArgumentException("Can't be converted to CaptionAlignment " + alignment);
        }
    }

    @Nullable
    public static JavaScriptComponent.DependencyType toDependencyType(@Nullable Dependency.Type type) {
        if (type == null) {
            return null;
        }

        switch (type) {
            case JAVASCRIPT:
                return JavaScriptComponent.DependencyType.JAVASCRIPT;
            case STYLESHEET:
                return JavaScriptComponent.DependencyType.STYLESHEET;
            default:
                throw new IllegalArgumentException("Can't be converted to DependencyType: " + type);
        }
    }

    @Nullable
    public static Dependency.Type toVaadinDependencyType(@Nullable JavaScriptComponent.DependencyType type) {
        if (type == null) {
            return null;
        }

        switch (type) {
            case JAVASCRIPT:
                return Dependency.Type.JAVASCRIPT;
            case STYLESHEET:
                return Dependency.Type.STYLESHEET;
            default:
                throw new IllegalArgumentException("Can't be converted to Dependency.Type: " + type);
        }
    }

    public static String fromVaadinSize(String size) {
        return Component.AUTO_SIZE.equalsIgnoreCase(size)
                ? AUTO_SIZE
                : size;
    }

    @Nullable
    public static String toVaadinSize(@Nullable String size) {
        return AUTO_SIZE.equalsIgnoreCase(size)
                ? Component.AUTO_SIZE
                : size;
    }

    @Nullable
    public static PopupPosition toVaadinPopupPosition(@Nullable PopupView.PopupPosition popupPosition) {
        if (popupPosition == null) {
            return null;
        }

        for (PopupPosition position : PopupPosition.values()) {
            if (position.name().equals(popupPosition.name())) {
                return position;
            }
        }
        return null;
    }

    @Nullable
    public static PopupView.PopupPosition fromVaadinPopupPosition(@Nullable PopupPosition popupPosition) {
        if (popupPosition == null) {
            return null;
        }

        for (PopupView.PopupPosition position : PopupView.PopupPosition.values()) {
            if (position.name().equals(popupPosition.name())) {
                return position;
            }
        }
        return null;
    }

    public static TimeMode toVaadinTimeMode(TimeField.TimeMode timeMode) {
        for (TimeMode mode : TimeMode.values()) {
            if (mode.name().equals(timeMode.name())) {
                return mode;
            }
        }

        throw new IllegalArgumentException("Can't be converted to TimeMode: " + timeMode.name());
    }

    public static TimeField.TimeMode fromVaadinTimeMode(TimeMode timeMode) {
        for (TimeField.TimeMode mode : TimeField.TimeMode.values()) {
            if (mode.name().equals(timeMode.name())) {
                return mode;
            }
        }

        throw new IllegalArgumentException("Can't be converted to TimeField.TimeMode: " + timeMode.name());
    }

    public static ContainerType toVaadinContainerType(ResponsiveGridLayout.ContainerType containerType) {
        checkNotNullArgument(containerType);

        switch (containerType) {
            case FIXED:
                return ContainerType.FIXED;
            case FLUID:
                return ContainerType.FLUID;
            default:
                throw new IllegalArgumentException("Can't be converted to ContainerType: " + containerType);
        }
    }

    public static ResponsiveGridLayout.ContainerType fromVaadinContainerType(ContainerType containerType) {
        checkNotNullArgument(containerType);

        switch (containerType) {
            case FIXED:
                return ResponsiveGridLayout.ContainerType.FIXED;
            case FLUID:
                return ResponsiveGridLayout.ContainerType.FLUID;
            default:
                throw new IllegalArgumentException("Can't be converted to ContainerType: " + containerType);
        }
    }

    public static Breakpoint toVaadinBreakpoint(ResponsiveGridLayout.Breakpoint breakpoint) {
        checkNotNullArgument(breakpoint);

        switch (breakpoint) {
            case XS:
                return Breakpoint.XS;
            case SM:
                return Breakpoint.SM;
            case MD:
                return Breakpoint.MD;
            case LG:
                return Breakpoint.LG;
            case XL:
                return Breakpoint.XL;
            default:
                throw new IllegalArgumentException("Can't be converted to Breakpoint: " + breakpoint);
        }
    }

    public static ResponsiveGridLayout.Breakpoint fromVaadinBreakpoint(Breakpoint breakpoint) {
        checkNotNullArgument(breakpoint);

        switch (breakpoint) {
            case XS:
                return ResponsiveGridLayout.Breakpoint.XS;
            case SM:
                return ResponsiveGridLayout.Breakpoint.SM;
            case MD:
                return ResponsiveGridLayout.Breakpoint.MD;
            case LG:
                return ResponsiveGridLayout.Breakpoint.LG;
            case XL:
                return ResponsiveGridLayout.Breakpoint.XL;
            default:
                throw new IllegalArgumentException("Can't be converted to Breakpoint: " + breakpoint);
        }
    }

    public static AlignItems toVaadinAlignItems(ResponsiveGridLayout.AlignItems alignItems) {
        checkNotNullArgument(alignItems);

        switch (alignItems) {
            case START:
                return AlignItems.START;
            case CENTER:
                return AlignItems.CENTER;
            case END:
                return AlignItems.END;
            case BASELINE:
                return AlignItems.BASELINE;
            case STRETCH:
                return AlignItems.STRETCH;
            default:
                throw new IllegalArgumentException("Can't be converted to AlignItems: " + alignItems);
        }
    }

    public static ResponsiveGridLayout.AlignItems fromVaadinAlignItems(AlignItems alignItems) {
        checkNotNullArgument(alignItems);

        switch (alignItems) {
            case START:
                return ResponsiveGridLayout.AlignItems.START;
            case CENTER:
                return ResponsiveGridLayout.AlignItems.CENTER;
            case END:
                return ResponsiveGridLayout.AlignItems.END;
            case BASELINE:
                return ResponsiveGridLayout.AlignItems.BASELINE;
            case STRETCH:
                return ResponsiveGridLayout.AlignItems.STRETCH;
            default:
                throw new IllegalArgumentException("Can't be converted to AlignItems: " + alignItems);
        }
    }

    public static JustifyContent toVaadinJustifyContent(ResponsiveGridLayout.JustifyContent justifyContent) {
        checkNotNullArgument(justifyContent);

        switch (justifyContent) {
            case START:
                return JustifyContent.START;
            case CENTER:
                return JustifyContent.CENTER;
            case END:
                return JustifyContent.END;
            case AROUND:
                return JustifyContent.AROUND;
            case BETWEEN:
                return JustifyContent.BETWEEN;
            default:
               throw new IllegalArgumentException("Can't be converted to JustifyContent: " + justifyContent);
        }
    }

    public static ResponsiveGridLayout.JustifyContent fromVaadinJustifyContent(JustifyContent justifyContent) {
        checkNotNullArgument(justifyContent);

        switch (justifyContent) {
            case START:
                return ResponsiveGridLayout.JustifyContent.START;
            case CENTER:
                return ResponsiveGridLayout.JustifyContent.CENTER;
            case END:
                return ResponsiveGridLayout.JustifyContent.END;
            case AROUND:
                return ResponsiveGridLayout.JustifyContent.AROUND;
            case BETWEEN:
                return ResponsiveGridLayout.JustifyContent.BETWEEN;
            default:
                throw new IllegalArgumentException("Can't be converted to JustifyContent: " + justifyContent);
        }
    }

    public static RowColumnsValue toVaadinRowColumnsValue(ResponsiveGridLayout.RowColumnsValue columnsValue) {
        checkNotNullArgument(columnsValue);

        return RowColumnsValue.columns(columnsValue.getColumns());
    }

    public static ResponsiveGridLayout.RowColumnsValue fromVaadinRowColumnsValue(RowColumnsValue columnsValue) {
        checkNotNullArgument(columnsValue);

        return ResponsiveGridLayout.RowColumnsValue.columns(columnsValue.getColumns());
    }

    public static AlignSelf toVaadinResponsiveColumnAlignSelf(ResponsiveGridLayout.AlignSelf alignSelf) {
        checkNotNullArgument(alignSelf);

        switch (alignSelf) {
            case AUTO:
                return AlignSelf.AUTO;
            case START:
                return AlignSelf.START;
            case CENTER:
                return AlignSelf.CENTER;
            case END:
                return AlignSelf.END;
            case BASELINE:
                return AlignSelf.BASELINE;
            case STRETCH:
                return AlignSelf.STRETCH;
            default:
                throw new IllegalArgumentException("Can't be converted to AlignSelf: " + alignSelf);
        }
    }

    public static ResponsiveGridLayout.AlignSelf fromVaadinResponsiveColumnAlignSelf(AlignSelf alignSelf) {
        checkNotNullArgument(alignSelf);

        switch (alignSelf) {
            case AUTO:
                return ResponsiveGridLayout.AlignSelf.AUTO;
            case START:
                return ResponsiveGridLayout.AlignSelf.START;
            case CENTER:
                return ResponsiveGridLayout.AlignSelf.CENTER;
            case END:
                return ResponsiveGridLayout.AlignSelf.END;
            case BASELINE:
                return ResponsiveGridLayout.AlignSelf.BASELINE;
            case STRETCH:
                return ResponsiveGridLayout.AlignSelf.STRETCH;
            default:
                throw new IllegalArgumentException("Can't be converted to AlignSelf: " + alignSelf);
        }
    }

    public static ColumnsValue toVaadinColumnsValue(ResponsiveGridLayout.ColumnsValue columnsValue) {
        checkNotNullArgument(columnsValue);

        if (ResponsiveGridLayout.ColumnsValue.DEFAULT.equals(columnsValue)) {
            return ColumnsValue.DEFAULT;
        }

        if (ResponsiveGridLayout.ColumnsValue.AUTO.equals(columnsValue)) {
            return ColumnsValue.AUTO;
        }

        return ColumnsValue.columns(columnsValue.getColumns());
    }

    public static ResponsiveGridLayout.ColumnsValue fromVaadinColumnsValue(ColumnsValue columnsValue) {
        checkNotNullArgument(columnsValue);

        if (ColumnsValue.DEFAULT.equals(columnsValue)) {
            return ResponsiveGridLayout.ColumnsValue.DEFAULT;
        }

        if (ColumnsValue.AUTO.equals(columnsValue)) {
            return ResponsiveGridLayout.ColumnsValue.AUTO;
        }

        return ResponsiveGridLayout.ColumnsValue.columns(columnsValue.getColumns());
    }

    public static OrderValue toVaadinOrderValue(ResponsiveGridLayout.OrderValue orderValue) {
        checkNotNullArgument(orderValue);

        if (ResponsiveGridLayout.OrderValue.FIRST.equals(orderValue)) {
            return OrderValue.FIRST;
        }

        if (ResponsiveGridLayout.OrderValue.LAST.equals(orderValue)) {
            return OrderValue.LAST;
        }

        return orderValue.getOrder() != null
                ? new OrderValue(orderValue.getOrder())
                : new OrderValue(orderValue.getValue());
    }

    public static ResponsiveGridLayout.OrderValue fromVaadinOrderValue(OrderValue orderValue) {
        checkNotNullArgument(orderValue);

        if (OrderValue.FIRST.equals(orderValue)) {
            return ResponsiveGridLayout.OrderValue.FIRST;
        }

        if (OrderValue.LAST.equals(orderValue)) {
            return ResponsiveGridLayout.OrderValue.LAST;
        }

        return orderValue.getOrder() != null
                ? new ResponsiveGridLayout.OrderValue(orderValue.getOrder())
                : new ResponsiveGridLayout.OrderValue(orderValue.getValue());
    }

    public static OffsetValue toVaadinOffsetValue(ResponsiveGridLayout.OffsetValue offsetValue) {
        checkNotNullArgument(offsetValue);

        return OffsetValue.columns(offsetValue.getColumns());
    }

    public static ResponsiveGridLayout.OffsetValue fromVaadinOffsetValue(OffsetValue offsetValue) {
        checkNotNullArgument(offsetValue);

        return ResponsiveGridLayout.OffsetValue.columns(offsetValue.getColumns());
    }

    public static WindowMode fromVaadinWindowMode(com.vaadin.shared.ui.window.WindowMode windowMode) {
        checkNotNullArgument(windowMode);

        switch (windowMode) {
            case NORMAL:
                return WindowMode.NORMAL;
            case MAXIMIZED:
                return WindowMode.MAXIMIZED;
            default:
                throw new IllegalArgumentException("Can't be converted to DialogWindow.WindowMode: " + windowMode);
        }
    }

    public static com.vaadin.shared.ui.window.WindowMode toVaadinWindowMode(WindowMode windowMode) {
        checkNotNullArgument(windowMode);

        switch (windowMode) {
            case NORMAL:
                return com.vaadin.shared.ui.window.WindowMode.NORMAL;
            case MAXIMIZED:
                return com.vaadin.shared.ui.window.WindowMode.MAXIMIZED;
            default:
                throw new IllegalArgumentException("Can't be converted to WindowMode: " + windowMode);
        }
    }

    public static LogicalCondition.Type toLogicalConditionType(LogicalFilterComponent.Operation operation) {
        switch (operation) {
            case AND:
                return LogicalCondition.Type.AND;
            case OR:
                return LogicalCondition.Type.OR;
            default:
                throw new IllegalArgumentException("Unknown operation " + operation);
        }
    }

    public static LogicalFilterComponent.Operation fromLogicalConditionType(LogicalCondition.Type type) {
        switch (type) {
            case AND:
                return LogicalFilterComponent.Operation.AND;
            case OR:
                return LogicalFilterComponent.Operation.OR;
            default:
                throw new IllegalArgumentException("Unknown type: " + type);
        }
    }
}
