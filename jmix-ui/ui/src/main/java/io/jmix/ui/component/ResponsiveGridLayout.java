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

import com.google.common.base.Preconditions;
import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.ContainerType;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A layout where the components are laid out on a grid, based on the Bootstrap's 12 columns grid system.
 * <p>
 * Each component must be located in the corresponding {@link Column}.
 */
@StudioComponent(
        caption = "ResponsiveGridLayout",
        category = "Containers",
        xmlElement = "responsiveGridLayout",
        icon = "io/jmix/ui/icon/container/responsiveGridLayout.svg",
        canvasBehaviour = CanvasBehaviour.CONTAINER,
        containerType = ContainerType.RESPONSIVE_GRID,
        unsupportedProperties = {"responsive", "width"},
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/containers/responsive-grid-layout.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "height", type = PropertyType.SIZE, defaultValue = "-1px", initialValue = "100px")
        }
)
public interface ResponsiveGridLayout extends Component, Component.BelongToFrame, HasComponents,
        LayoutClickNotifier, HasHtmlSanitizer {

    String NAME = "responsiveGridLayout";

    /**
     * Creates a new {@link Row} and adds it to this responsive grid layout.
     *
     * @return the created row
     * @throws IllegalStateException if this responsive grid layout has been shown on a screen
     */
    Row addRow();

    /**
     * Creates a new {@link Row} and adds it to the given position in responsive grid layout.
     *
     * @param index the position of the new row. The rows that are
     *              currently in and after the position are shifted forwards
     * @return the created row
     * @throws IllegalStateException if this responsive grid layout has been shown on a screen
     */
    Row addRow(int index);

    /**
     * Removes the given row from this responsive grid layout.
     *
     * @param row the row to be removed
     * @throws IllegalStateException if this responsive grid layout has been shown on a screen
     */
    void removeRow(Row row);

    /**
     * Removes all rows from this responsive grid layout.
     *
     * @throws IllegalStateException if this responsive grid layout has been shown on a screen
     */
    void removeAllRows();

    /**
     * @return a list of contained rows
     */
    @StudioElement
    List<Row> getRows();

    /**
     * @return the container type, not {@code null}
     */
    ContainerType getContainerType();

    /**
     * Sets the container type.
     * <p>
     * The built-in container types are:
     * <ul>
     *     <li>{@link ContainerType#FLUID} - a full width container,
     *     spanning the entire width of the viewport. <strong>Used by default</strong>.</li>
     *     <li>{@link ContainerType#FIXED} - a fixed-width container,
     *     meaning its {@code max-width} changes at each breakpoint.</li>
     * </ul>
     *
     * @param containerType the container type to switch to, not {@code null}
     * @throws IllegalStateException if this responsive grid layout has been shown on a screen
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "FLUID", options = {"FLUID", "FIXED"})
    void setContainerType(ContainerType containerType);

    /**
     * Container type representing possible basic container styles.
     *
     * @see #setContainerType(ContainerType)
     */
    enum ContainerType {

        /**
         * A fixed-width container, meaning its {@code max-width} changes at each breakpoint.
         * <p>
         * Corresponds to the {@code .container} style.
         */
        FIXED,

        /**
         * A full width container, spanning the entire width of the viewport.
         * <p>
         * Corresponds to the {@code .container-fluid} style.
         */
        FLUID
    }

    /**
     * Breakpoint representing minimum viewport widths used in media query ranges.
     * When used, they apply to that one breakpoint and all those above it
     * (e.g. {@link Breakpoint#SM} applies to small, medium, large, and extra large devices,
     * but not the first {@link Breakpoint#XS} breakpoint).
     */
    enum Breakpoint {

        /**
         * Extra small devices (portrait phones, less than 576px).
         * No media query for `xs` since this is the default in Bootstrap.
         */
        XS,

        /**
         * Small devices (landscape phones, 576px and up).
         * <p>
         * Media query example:
         * <p>
         * <pre>
         * {@code @media (min-width: 576px) { ... }}
         * </pre>
         */
        SM,

        /**
         * Medium devices (tablets, 768px and up).
         * <p>
         * Media query example:
         * <p>
         * <pre>
         * {@code @media (min-width: 768px) { ... }}
         * </pre>
         */
        MD,

        /**
         * Large devices (desktops, 992px and up).
         * <p>
         * Media query example:
         * <p>
         * <pre>
         * {@code @media (min-width: 992px) { ... }}
         * </pre>
         */
        LG,

        /**
         * Extra large devices (large desktops, 1200px and up).
         * <p>
         * Media query example:
         * <p>
         * <pre>
         * {@code @media (min-width: 1200px) { ... }}
         * </pre>
         */
        XL
    }

    /**
     * Interface defining a grid row. It's a wrapper over
     * grid {@link Column}s with the ability to provide content alignment.
     * <p>
     * Corresponds to the {@code <div class="row">} element.
     */
    @StudioElement(
            xmlElement = "row",
            caption = "Row",
            icon = "io/jmix/ui/icon/element/row.svg",
            documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/containers/responsive-grid-layout.html#row-element"
    )
    @StudioProperties(
            properties = {
                    @StudioProperty(name = "alignItemsLg", type = PropertyType.ENUMERATION,
                            options = {"START", "CENTER", "END", "BASELINE", "STRETCH"}),
                    @StudioProperty(name = "alignItemsMd", type = PropertyType.ENUMERATION,
                            options = {"START", "CENTER", "END", "BASELINE", "STRETCH"}),
                    @StudioProperty(name = "alignItemsSm", type = PropertyType.ENUMERATION,
                            options = {"START", "CENTER", "END", "BASELINE", "STRETCH"}),
                    @StudioProperty(name = "alignItemsXl", type = PropertyType.ENUMERATION,
                            options = {"START", "CENTER", "END", "BASELINE", "STRETCH"}),
                    @StudioProperty(name = "colsLg", type = PropertyType.INTEGER),
                    @StudioProperty(name = "colsMd", type = PropertyType.INTEGER),
                    @StudioProperty(name = "colsSm", type = PropertyType.INTEGER),
                    @StudioProperty(name = "colsXl", type = PropertyType.INTEGER),
                    @StudioProperty(name = "justifyContentLg", type = PropertyType.ENUMERATION,
                            options = {"AROUND", "BETWEEN", "CENTER", "END", "START"}),
                    @StudioProperty(name = "justifyContentMd", type = PropertyType.ENUMERATION,
                            options = {"AROUND", "BETWEEN", "CENTER", "END", "START"}),
                    @StudioProperty(name = "justifyContentSm", type = PropertyType.ENUMERATION,
                            options = {"AROUND", "BETWEEN", "CENTER", "END", "START"}),
                    @StudioProperty(name = "justifyContentXl", type = PropertyType.ENUMERATION,
                            options = {"AROUND", "BETWEEN", "CENTER", "END", "START"})
            }
    )
    interface Row extends GridElement {

        /**
         * Creates a new {@link Column} and adds it to this row.
         *
         * @return the created column
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        Column addColumn();

        /**
         * Creates a new {@link Column} and adds it to the given position in this row.
         *
         * @param index the position of the new column. The columns that are
         *              currently in and after the position are shifted forwards
         * @return the created column
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        Column addColumn(int index);

        /**
         * Removes the given column from this row.
         *
         * @param column the column to be removed
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        void removeColumn(Column column);

        /**
         * Removes all columns from this row.
         *
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        void removeAllColumns();

        /**
         * @return a list of contained columns
         */
        @StudioElement
        List<Column> getColumns();

        /**
         * @return whether gutters are enabled
         */
        boolean isGuttersEnabled();

        /**
         * Sets whether gutters are enabled. Gutters are enabled by default.
         * <p>
         * If set to {@code false}, the {@code .no-gutters} style will be added to this row.
         * It removes the negative margins from the row and the horizontal padding from all
         * immediate children columns.
         *
         * @param guttersEnabled a boolean value specifying if the row should have gutters or not
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        @StudioProperty(defaultValue = "true")
        void setGuttersEnabled(boolean guttersEnabled);

        /**
         * Returns the height of this row. A negative number implies unspecified size.
         *
         * @return height of the row in units specified by {{@link #getHeightSizeUnit()}} property.
         */
        float getHeight();

        /**
         * Returns the height property units.
         *
         * @return units used in height property.
         */
        SizeUnit getHeightSizeUnit();

        /**
         * Sets the height of the component using String presentation.
         * <p>
         * String presentation is similar to what is used in Cascading Style Sheets.
         * Size can be length or percentage of available size.
         * <p>
         * The empty string ("") or null will unset the height and set the units to pixels.
         *
         * @param height the height to be set
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        @StudioProperty(name = "height", type = PropertyType.SIZE, defaultValue = "-1px")
        void setHeight(String height);

        /**
         * Sets row height to {@link Component#AUTO_SIZE}.
         *
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        default void setHeightAuto() {
            setHeight(AUTO_SIZE);
        }

        /**
         * Sets row height to {@link Component#FULL_SIZE}.
         *
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        default void setHeightFull() {
            setHeight(FULL_SIZE);
        }

        /**
         * Returns the map that contains information regarding what
         * number of columns in the row will be at a specific breakpoint.
         * <p>
         * Corresponds to the {@code .row-cols-{breakpoint}-*} responsive styles.
         *
         * @return the map that contains information regarding what
         * number of columns in the row will be at a specific breakpoint
         */
        Map<Breakpoint, RowColumnsValue> getRowColumns();

        /**
         * Sets a new mapping of the number of columns in the row that will
         * be applied for all breakpoints, i.e. starting from {@link Breakpoint#XS}.
         * Clears previous values and creates a map with a single value.
         *
         * @param columnsValue the instance of {@link RowColumnsValue} to set for all breakpoint
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         * @see Row#setRowColumns(Map)
         */
        @StudioProperty(name = "cols", type = PropertyType.INTEGER)
        default void setRowColumns(RowColumnsValue columnsValue) {
            setRowColumns(Breakpoint.XS, columnsValue);
        }

        /**
         * Sets a new mapping of the number of columns in the row with the specified breakpoint.
         * Clears previous values and creates a map with a single value.
         *
         * @param breakpoint   the breakpoint to start from
         * @param columnsValue the instance of {@link RowColumnsValue} to set
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         * @see Row#setRowColumns(Map)
         */
        default void setRowColumns(Breakpoint breakpoint, RowColumnsValue columnsValue) {
            setRowColumns(Collections.singletonMap(breakpoint, columnsValue));
        }

        /**
         * Sets a new mapping of the number of columns in the row with corresponding breakpoints.
         * Overrides previous values.
         * <p>
         * Corresponds to the {@code .row-cols-{breakpoint}-*} responsive styles.
         *
         * @param columns the map that contains information regarding what
         *                number of columns in the row will be at a specific breakpoint
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        void setRowColumns(Map<Breakpoint, RowColumnsValue> columns);

        /**
         * Adds information regarding what number of columns in the row will be
         * at the given breakpoint. If a mapping for the breakpoint already exist,
         * the old value is replaced by the specified {@link RowColumnsValue} instance.
         *
         * @param breakpoint   the breakpoint with which the specified {@link RowColumnsValue}
         *                     instance is to be associated
         * @param columnsValue the {@link RowColumnsValue} instance to be associated
         *                     with the specified breakpoint
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         * @see Row#setRowColumns(Map)
         */
        void addRowColumns(Breakpoint breakpoint, RowColumnsValue columnsValue);

        /**
         * Removes the mapping of the number of columns in the row
         * for the given breakpoint if it is present.
         *
         * @param breakpoint the breakpoint for which to remove mapping
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        void removeRowColumns(Breakpoint breakpoint);

        /**
         * Returns the map that contains information regarding how columns are laid
         * out along the cross axis on the current row at a specific breakpoint.
         * <p>
         * Corresponds to the {@code .align-items-{breakpoint}-*} responsive styles.
         *
         * @return the map that contains information regarding how columns are laid
         * out along the cross axis on the current row at a specific breakpoint
         */
        Map<Breakpoint, AlignItems> getAlignItems();

        /**
         * Sets a new mapping of how columns are laid out along the cross axis
         * on the current row at all breakpoints, i.e. starting from {@link Breakpoint#XS}.
         * Clears previous values and creates a map with a single value.
         *
         * @param alignItems the {@link AlignItems} value to set for all breakpoints
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         * @see Row#setAlignItems(Map)
         */
        @StudioProperty(type = PropertyType.ENUMERATION, options = {"START", "CENTER", "END", "BASELINE", "STRETCH"})
        default void setAlignItems(AlignItems alignItems) {
            setAlignItems(Breakpoint.XS, alignItems);
        }

        /**
         * Sets a new mapping of how columns are laid out along the cross axis on
         * the current row with the specified breakpoint. Clears previous values
         * and creates a map with a single value.
         *
         * @param breakpoint the breakpoint to start from
         * @param alignItems the {@link AlignItems} value to set
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         * @see Row#setAlignItems(Map)
         */
        default void setAlignItems(Breakpoint breakpoint, AlignItems alignItems) {
            setAlignItems(Collections.singletonMap(breakpoint, alignItems));
        }

        /**
         * Sets a new mapping of how columns are laid out along the cross axis on
         * the current row with corresponding breakpoints. Overrides previous values.
         * <p>
         * Corresponds to the {@code .align-items-{breakpoint}-*} responsive styles.
         *
         * @param alignItems the map that contains information regarding how columns
         *                   are laid out along the cross axis on the current row at
         *                   a specific breakpoint
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        void setAlignItems(Map<Breakpoint, AlignItems> alignItems);

        /**
         * Adds information regarding how columns are laid out along the cross
         * axis on the current row at the given breakpoint. If a mapping for the breakpoint
         * already exist, the old value is replaced by the specified {@link AlignItems} value.
         *
         * @param breakpoint the breakpoint with which the specified {@link AlignItems}
         *                   value is to be associated
         * @param alignItems the {@link AlignItems} value to be associated
         *                   with the specified breakpoint
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         * @see Row#setAlignItems(Map)
         */
        void addAlignItems(Breakpoint breakpoint, AlignItems alignItems);

        /**
         * Removes the mapping of how columns are laid out along the cross
         * axis on the current row for the given breakpoint if it is present.
         *
         * @param breakpoint the breakpoint for which to remove mapping
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        void removeAlignItems(Breakpoint breakpoint);

        /**
         * Returns the map that contains information regarding how columns are laid
         * out along the main axis on the current row at a specific breakpoint.
         * <p>
         * Corresponds to the {@code .justify-content-{breakpoint}-*} responsive styles.
         *
         * @return the map that contains information regarding how columns are laid
         * out along the main axis on the current row at a specific breakpoint
         */
        Map<Breakpoint, JustifyContent> getJustifyContent();

        /**
         * Sets a new mapping of how columns are laid out along the main axis
         * on the current row at all breakpoints, i.e. starting from {@link Breakpoint#XS}.
         * Clears previous values and creates a map with a single value.
         *
         * @param justifyContent the {@link JustifyContent} value to set for all breakpoints
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         * @see Row#setJustifyContent(Map)
         */
        @StudioProperty(type = PropertyType.ENUMERATION, options = {"AROUND", "BETWEEN", "CENTER", "END", "START"})
        default void setJustifyContent(JustifyContent justifyContent) {
            setJustifyContent(Breakpoint.XS, justifyContent);
        }

        /**
         * Sets a new mapping of how columns are laid out along the main axis on
         * the current row with the specified breakpoint. Clears previous values
         * and creates a map with a single value.
         *
         * @param breakpoint     the breakpoint to start from
         * @param justifyContent the {@link JustifyContent} value to set
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         * @see Row#setJustifyContent(Map)
         */
        default void setJustifyContent(Breakpoint breakpoint, JustifyContent justifyContent) {
            setJustifyContent(Collections.singletonMap(breakpoint, justifyContent));
        }

        /**
         * Sets a new mapping of how columns are laid out along the main axis on
         * the current row with corresponding breakpoints. Overrides previous values.
         * <p>
         * Corresponds to the {@code .justify-content-{breakpoint}-*} responsive styles.
         *
         * @param justifyContent the map that contains information regarding how columns
         *                       are laid out along the main axis on the current row at
         *                       a specific breakpoint
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        void setJustifyContent(Map<Breakpoint, JustifyContent> justifyContent);

        /**
         * Adds information regarding how columns are laid out along the main axis on
         * the current row at the given breakpoint. If a mapping for the breakpoint already
         * exist, the old value is replaced by the specified {@link JustifyContent} value.
         *
         * @param breakpoint     the breakpoint with which the specified {@link AlignItems}
         *                       value is to be associated
         * @param justifyContent the {@link JustifyContent} value to be associated
         *                       with the specified breakpoint
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         * @see Row#setJustifyContent(Map)
         */
        void addJustifyContent(Breakpoint breakpoint, JustifyContent justifyContent);

        /**
         * Removes the mapping of how columns are laid out along the main
         * axis on the current row for the given breakpoint if it is present.
         *
         * @param breakpoint the breakpoint for which to remove mapping
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        void removeJustifyContent(Breakpoint breakpoint);
    }

    /**
     * Represents the default behavior for how flex items are laid out along the cross axis on the current row.
     * <p>
     * Corresponds to the {@code align-items} CSS attribute.
     *
     * @see Row#setAlignItems(Map)
     */
    enum AlignItems {

        /**
         * Items are placed at the start of the cross axis.
         * <p>
         * Corresponds to the {@code .align-items-start} style.
         */
        START,

        /**
         * Items are centered in the cross-axis.
         * <p>
         * Corresponds to the {@code .align-items-center} style.
         */
        CENTER,

        /**
         * Items are placed at the end of the cross axis.
         * <p>
         * Corresponds to the {@code .align-items-end} style.
         */
        END,

        /**
         * Items are aligned such as their baselines align.
         * <p>
         * Corresponds to the {@code .align-items-baseline} style.
         */
        BASELINE,

        /**
         * Stretch to fill the container.
         * <p>
         * Corresponds to the {@code .align-items-stretch} style.
         */
        STRETCH
    }

    /**
     * Represents the default behavior for how flex items are laid out along the main axis on the current row.
     * <p>
     * Corresponds to the {@code justify-content} CSS attribute.
     *
     * @see Row#setJustifyContent(Map)
     */
    enum JustifyContent {

        /**
         * Items are packed toward the start of the flex-direction.
         * <p>
         * Corresponds to the {@code .justify-content-start} style.
         */
        START,

        /**
         * Items are centered along the line.
         * <p>
         * Corresponds to the {@code .justify-content-center} style.
         */
        CENTER,

        /**
         * Items are packed toward the end of the flex-direction.
         * <p>
         * Corresponds to the {@code .justify-content-end} style.
         */
        END,

        /**
         * Items are evenly distributed in the line with equal space around them.
         * <p>
         * Note that visually the spaces aren't equal, since all the items have equal space on both sides.
         * The first item will have one unit of space against the container edge, but two units of space
         * between the next item because that next item has its own spacing that applies.
         * <p>
         * Corresponds to the {@code .justify-content-around} style.
         */
        AROUND,

        /**
         * Items are evenly distributed in the line. First item is on the start line, last item on the end line.
         * <p>
         * Corresponds to the {@code .justify-content-between} style.
         */
        BETWEEN
    }

    /**
     * Class containing information about the number of columns in the row.
     * <p>
     * Corresponds to the {@code .row-cols-*} styles.
     *
     * @see Row#setRowColumns(Map)
     */
    class RowColumnsValue {

        protected int cols;

        public RowColumnsValue(int cols) {
            Preconditions.checkArgument(cols > 0,
                    "Columns value must be greater than 0");
            this.cols = cols;
        }

        /**
         * Creates an instance of {@link RowColumnsValue} with the given number of columns.
         *
         * @param cols the number of columns to set
         * @return an instance of {@link RowColumnsValue} with the given number of columns
         */
        public static RowColumnsValue columns(int cols) {
            return new RowColumnsValue(cols);
        }

        /**
         * @return the number of columns in the row
         */
        public int getColumns() {
            return cols;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof RowColumnsValue)) {
                return false;
            }

            RowColumnsValue that = (RowColumnsValue) o;
            return cols == that.cols;
        }

        @Override
        public int hashCode() {
            return Objects.hash(cols);
        }
    }

    /**
     * Interface defining a grid column. It's a wrapper over grid's content.
     * <p>
     * Corresponds to the {@code <div class="col">} element.
     */
    @StudioElement(
            xmlElement = "col",
            caption = "Column",
            icon = "io/jmix/ui/icon/element/col.svg",
            documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/containers/responsive-grid-layout.html#col-element"
    )
    @StudioProperties(
            properties = {
                    @StudioProperty(name = "alignSelfLg", type = PropertyType.ENUMERATION,
                            options = {"AUTO", "BASELINE", "CENTER", "END", "START", "STRETCH"}),
                    @StudioProperty(name = "alignSelfMd", type = PropertyType.ENUMERATION,
                            options = {"AUTO", "BASELINE", "CENTER", "END", "START", "STRETCH"}),
                    @StudioProperty(name = "alignSelfSm", type = PropertyType.ENUMERATION,
                            options = {"AUTO", "BASELINE", "CENTER", "END", "START", "STRETCH"}),
                    @StudioProperty(name = "alignSelfXl", type = PropertyType.ENUMERATION,
                            options = {"AUTO", "BASELINE", "CENTER", "END", "START", "STRETCH"}),
                    @StudioProperty(name = "xs", type = PropertyType.STRING, options = {"AUTO", "DEFAULT"}),
                    @StudioProperty(name = "sm", type = PropertyType.STRING, options = {"AUTO", "DEFAULT"}),
                    @StudioProperty(name = "md", type = PropertyType.STRING, options = {"AUTO", "DEFAULT"}),
                    @StudioProperty(name = "lg", type = PropertyType.STRING, options = {"AUTO", "DEFAULT"}),
                    @StudioProperty(name = "xl", type = PropertyType.STRING, options = {"AUTO", "DEFAULT"}),
                    @StudioProperty(name = "offsetLg", type = PropertyType.INTEGER),
                    @StudioProperty(name = "offsetMd", type = PropertyType.INTEGER),
                    @StudioProperty(name = "offsetSm", type = PropertyType.INTEGER),
                    @StudioProperty(name = "offsetXl", type = PropertyType.INTEGER),
                    @StudioProperty(name = "orderLg", type = PropertyType.STRING, options = {"FIRST", "LAST"}),
                    @StudioProperty(name = "orderMd", type = PropertyType.STRING, options = {"FIRST", "LAST"}),
                    @StudioProperty(name = "orderSm", type = PropertyType.STRING, options = {"FIRST", "LAST"}),
                    @StudioProperty(name = "orderXl", type = PropertyType.STRING, options = {"FIRST", "LAST"})
            }
    )
    interface Column extends GridElement {

        /**
         * @return a component to be rendered within this column
         */
        Component getComponent();

        /**
         * Sets a component to be rendered within this column.
         *
         * @param component a components to set
         */
        void setComponent(Component component);

        /**
         * Returns the map that contains information regarding the logical columns
         * number that this column occupies at a specific breakpoint.
         * <p>
         * Corresponds to the {@code .col-{breakpoint}-*} responsive styles.
         *
         * @return the map that contains information regarding the logical columns
         * number that this column occupies at a specific breakpoint
         */
        Map<Breakpoint, ColumnsValue> getColumns();

        /**
         * Sets a new mapping of the logical columns number that this column occupies
         * at all breakpoints, i.e. starting from {@link Breakpoint#XS}. Clears
         * previous values and creates a map with a single value.
         *
         * @param columnsValue the {@link ColumnsValue} instance to set for all breakpoints
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         * @see Column#setColumns(Map)
         */
        default void setColumns(ColumnsValue columnsValue) {
            setColumns(Breakpoint.XS, columnsValue);
        }

        /**
         * Sets a new mapping of the logical columns number that this column occupies
         * with the specified breakpoint. Clears previous values and creates a map
         * with a single value.
         *
         * @param breakpoint   the breakpoint to start from
         * @param columnsValue the {@link ColumnsValue} instance to set
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         * @see Column#setColumns(Map)
         */
        default void setColumns(Breakpoint breakpoint, ColumnsValue columnsValue) {
            setColumns(Collections.singletonMap(breakpoint, columnsValue));
        }

        /**
         * Sets a new mapping of the logical columns number that this column occupies
         * with corresponding breakpoints. Overrides previous values.
         * <p>
         * Corresponds to the {@code .col-{breakpoint}-*} responsive styles.
         *
         * @param columns the map that contains information regarding the logical
         *                columns number that this column occupies at a specific breakpoint
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        void setColumns(Map<Breakpoint, ColumnsValue> columns);

        /**
         * Adds information regarding the logical columns number that this column occupies
         * at the given breakpoint. If a mapping for the breakpoint already exist, the old
         * value is replaced by the specified {@link ColumnsValue} instance.
         *
         * @param breakpoint   the breakpoint with which the specified {@link ColumnsValue}
         *                     instance is to be associated
         * @param columnsValue the {@link ColumnsValue} instance to be associated
         *                     with the specified breakpoint
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         * @see Column#setColumns(Map)
         */
        void addColumns(Breakpoint breakpoint, ColumnsValue columnsValue);

        /**
         * Removes the mapping of the logical columns number that this column occupies
         * for the given breakpoint if it is present.
         *
         * @param breakpoint the breakpoint for which to remove mapping
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        void removeColumns(Breakpoint breakpoint);

        /**
         * Returns the map that contains information regarding how this column is laid
         * out along the cross axis at a specific breakpoint.
         * <p>
         * Corresponds to the {@code .align-self-{breakpoint}-*} responsive styles.
         *
         * @return the map that contains information regarding how this column is laid
         * out along the cross axis at a specific breakpoint
         */
        Map<Breakpoint, AlignSelf> getAlignSelf();

        /**
         * Sets a new mapping of how this column is laid out along the cross axis
         * at all breakpoints, i.e. starting from {@link Breakpoint#XS}.
         * Clears previous values and creates a map with a single value.
         *
         * @param alignSelf the {@link AlignSelf} value to set for all breakpoints
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         * @see Column#setAlignSelf(Map)
         */
        @StudioProperty(type = PropertyType.ENUMERATION,
                options = {"AUTO", "BASELINE", "CENTER", "END", "START", "STRETCH"})
        default void setAlignSelf(AlignSelf alignSelf) {
            setAlignSelf(Breakpoint.XS, alignSelf);
        }

        /**
         * Sets a new mapping of how this column is laid out along the cross axis
         * with the specified breakpoint. Clears previous values and creates a map
         * with a single value.
         *
         * @param breakpoint the breakpoint to start from
         * @param alignSelf  the {@link AlignSelf} value to set
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         * @see Column#setAlignSelf(Map)
         */
        default void setAlignSelf(Breakpoint breakpoint, AlignSelf alignSelf) {
            setAlignSelf(Collections.singletonMap(breakpoint, alignSelf));
        }

        /**
         * Sets a new mapping of how this column is laid out along the cross axis
         * with corresponding breakpoints. Overrides previous values.
         * <p>
         * Corresponds to the {@code .align-self-{breakpoint}-*} responsive styles.
         *
         * @param aligns the map that contains information regarding how this column
         *               is laid out along the cross axis at a specific breakpoint
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        void setAlignSelf(Map<Breakpoint, AlignSelf> aligns);

        /**
         * Adds information regarding how this column is laid out along the cross
         * axis at the given breakpoint. If a mapping for the breakpoint already exist,
         * the old value is replaced by the specified {@link AlignSelf} value.
         *
         * @param breakpoint the breakpoint with which the specified {@link AlignSelf}
         *                   value is to be associated
         * @param alignSelf  the {@link AlignSelf} value to be associated
         *                   with the specified breakpoint
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         * @see Column#setAlignSelf(Map)
         */
        void addAlignSelf(Breakpoint breakpoint, AlignSelf alignSelf);

        /**
         * Removes the mapping of how this column is laid out along the cross
         * axis for the given breakpoint if it is present.
         *
         * @param breakpoint the breakpoint for which to remove mapping
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        void removeAlignSelf(Breakpoint breakpoint);

        /**
         * Returns the map that contains information regarding the visual order
         * of this column at a specific breakpoint.
         * <p>
         * Corresponds to the {@code .order-{breakpoint}-*} responsive styles.
         *
         * @return the map that contains information regarding the visual order of
         * this column at a specific breakpoint
         */
        Map<Breakpoint, OrderValue> getOrder();

        /**
         * Sets a new mapping of the visual order of this column
         * at all breakpoints, i.e. starting from {@link Breakpoint#XS}.
         * Clears previous values and creates a map with a single value.
         *
         * @param orderValue the {@link OrderValue} instance to set for all breakpoints
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         * @see Column#setOrder(Map)
         */
        @StudioProperty(name = "order", type = PropertyType.STRING, options = {"FIRST", "LAST"})
        default void setOrder(OrderValue orderValue) {
            setOrder(Breakpoint.XS, orderValue);
        }

        /**
         * Sets a new mapping of the visual order of this column with the
         * specified breakpoint. Clears previous values and creates a map
         * with a single value.
         *
         * @param breakpoint the breakpoint to start from
         * @param orderValue the {@link OrderValue} instance to set
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         * @see Column#setOrder(Map)
         */
        default void setOrder(Breakpoint breakpoint, OrderValue orderValue) {
            setOrder(Collections.singletonMap(breakpoint, orderValue));
        }

        /**
         * Sets a new mapping of the visual order of this column with corresponding
         * breakpoints. Overrides previous values.
         * <p>
         * Corresponds to the {@code .order-{breakpoint}-*} responsive styles.
         *
         * @param orders the map that contains information regarding the visual order
         *               of this column at a specific breakpoint
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        void setOrder(Map<Breakpoint, OrderValue> orders);

        /**
         * Adds information regarding the visual order of this column at the given breakpoint.
         * If a mapping for the breakpoint already exist, the old value is replaced by the
         * specified {@link OrderValue} instance.
         *
         * @param breakpoint the breakpoint with which the specified {@link OrderValue}
         *                   instance is to be associated
         * @param orderValue the {@link OrderValue} instance to be associated
         *                   with the specified breakpoint
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         * @see Column#setOrder(Map)
         */
        void addOrder(Breakpoint breakpoint, OrderValue orderValue);

        /**
         * Removes the mapping of the visual order of this column for the given
         * breakpoint if it is present.
         *
         * @param breakpoint the breakpoint for which to remove mapping
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        void removeOrder(Breakpoint breakpoint);

        /**
         * Returns the map that contains information regarding offset of this column
         * at a specific breakpoint.
         * <p>
         * Corresponds to the {@code .offset-{breakpoint}-*} responsive styles.
         *
         * @return the map that contains information regarding offset of
         * this column at a specific breakpoint
         */
        Map<Breakpoint, OffsetValue> getOffset();

        /**
         * Sets a new mapping of offset of this column at all breakpoints,
         * i.e. starting from {@link Breakpoint#XS}. Clears previous values
         * and creates a map with a single value.
         *
         * @param offsetValue the {@link OffsetValue} instance to set for all breakpoints
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         * @see Column#setOffset(Map)
         */
        @StudioProperty(name = "offset", type = PropertyType.INTEGER)
        default void setOffset(OffsetValue offsetValue) {
            setOffset(Breakpoint.XS, offsetValue);
        }

        /**
         * Sets a new mapping of offset of this column with the
         * specified breakpoint. Clears previous values and creates a map
         * with a single value.
         *
         * @param breakpoint  the breakpoint to start from
         * @param offsetValue the {@link OffsetValue} instance to set
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         * @see Column#setOffset(Map)
         */
        default void setOffset(Breakpoint breakpoint, OffsetValue offsetValue) {
            setOffset(Collections.singletonMap(breakpoint, offsetValue));
        }

        /**
         * Sets a new mapping of offset of this column with corresponding
         * breakpoints. Overrides previous values.
         * <p>
         * Corresponds to the {@code .offset-{breakpoint}-*} responsive styles.
         *
         * @param offsets the map that contains information regarding offset
         *                of this column at a specific breakpoint
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        void setOffset(Map<Breakpoint, OffsetValue> offsets);

        /**
         * Adds information regarding offset of this column at the given breakpoint.
         * If a mapping for the breakpoint already exist, the old value is replaced by the
         * specified {@link OffsetValue} instance.
         *
         * @param breakpoint  the breakpoint with which the specified {@link OffsetValue}
         *                    instance is to be associated
         * @param offsetValue the {@link OffsetValue} instance to be associated
         *                    with the specified breakpoint
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         * @see Column#setOffset(Map)
         */
        void addOffset(Breakpoint breakpoint, OffsetValue offsetValue);

        /**
         * Removes the mapping of offset of this column for the given
         * breakpoint if it is present.
         *
         * @param breakpoint the breakpoint for which to remove mapping
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        void removeOffset(Breakpoint breakpoint);
    }

    /**
     * Represents the default behavior for how individual flex item is laid out along the cross axis.
     * <p>
     * Corresponds to the {@code align-self} CSS attribute.
     *
     * @see Column#setAlignSelf(Map)
     */
    enum AlignSelf {

        /**
         * Item inherits its parent row's {@code align-items} property.
         * <p>
         * Corresponds to the {@code .align-self-auto} style.
         */
        AUTO,

        /**
         * Item is placed at the start of the cross axis.
         * <p>
         * Corresponds to the {@code .align-self-start} style.
         */
        START,

        /**
         * Item is centered in the cross-axis.
         * <p>
         * Corresponds to the {@code .align-self-center} style.
         */
        CENTER,

        /**
         * Item is placed at the end of the cross axis.
         * <p>
         * Corresponds to the {@code .align-self-end} style.
         */
        END,

        /**
         * Item is aligned such as their baselines align.
         * <p>
         * Corresponds to the {@code .align-self-baseline} style.
         */
        BASELINE,

        /**
         * Stretch to fill the container.
         * <p>
         * Corresponds to the {@code .align-self-stretch} style.
         */
        STRETCH
    }

    /**
     * Class containing information about logical columns number that a column occupies.
     * Also contains pre-instantiated constants for the special values.
     * <p>
     * Corresponds to the {@code .col-*} styles.
     *
     * @see Column#setColumns(Map)
     */
    class ColumnsValue {

        /**
         * The instance of {@link ColumnsValue} that represents the case when
         * no explicit columns value is defined. This means that the equal-width
         * columns will be created.
         * <p>
         * Corresponds to the {@code .col} style.
         */
        public static ColumnsValue DEFAULT = new ColumnsValue(null);

        /**
         * The instance of {@link ColumnsValue} that represents a special value 'auto'.
         * This means that columns with this value will have width based on the natural
         * width of their content.
         * <p>
         * Corresponds to the {@code .col-auto} style.
         */
        public static ColumnsValue AUTO = new ColumnsValue(true);

        protected Integer cols;
        protected boolean auto;

        public ColumnsValue(@Nullable Integer cols) {
            this(cols, false);
        }

        public ColumnsValue(boolean auto) {
            this(null, auto);
        }

        protected ColumnsValue(@Nullable Integer cols, boolean auto) {
            Preconditions.checkArgument(cols == null || cols > 0,
                    "Columns value must be either 'null' for auto-layout or be greater than 0");

            this.cols = cols;
            this.auto = auto;
        }

        public static ColumnsValue columns(@Nullable Integer cols) {
            return new ColumnsValue(cols);
        }

        /**
         * @return the number of logical column that represents a column width
         * or {@code null} if not defined
         */
        @Nullable
        public Integer getColumns() {
            return cols;
        }

        /**
         * @return whether column width must be based on the natural width of its content
         */
        public boolean isAuto() {
            return auto;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof ColumnsValue)) {
                return false;
            }

            ColumnsValue that = (ColumnsValue) o;
            return auto == that.auto &&
                    Objects.equals(cols, that.cols);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cols, auto);
        }
    }

    /**
     * Class containing information for controlling the visual order of a column.
     * Also contains pre-instantiated constants for the special values.
     * <p>
     * Corresponds to the {@code .order-*} styles.
     *
     * @see Column#setOrder(Map)
     */
    class OrderValue {

        /**
         * The instance of {@link OrderValue} that represents a special order value 'first'.
         * Behaves like the order is {@code -1}. This means that a column with this order
         * value will be placed at the very first position even if there are columns with order 0.
         * <p>
         * Corresponds to the {@code .order-first} style.
         */
        public static OrderValue FIRST = new OrderValue("first");

        /**
         * The instance of {@link OrderValue} that represents a special order value 'last'.
         * Behaves like the order is {@code 13}. This means that a column with this order
         * value will be placed at the very last position even if there are columns with order 12.
         * <p>
         * Corresponds to the {@code .order-last} style.
         */
        public static OrderValue LAST = new OrderValue("last");

        protected Integer order;
        protected String value;

        public OrderValue(int order) {
            this(order, null);
        }

        public OrderValue(String value) {
            this(null, value);
        }

        protected OrderValue(@Nullable Integer order, @Nullable String value) {
            Preconditions.checkArgument(order != null && value == null
                            || order == null && value != null,
                    "Either numeric value or constant value can be set at a time");
            Preconditions.checkArgument(order == null || order >= 0,
                    "Order value must be greater or equal to 0");

            this.order = order;
            this.value = value;
        }

        /**
         * Creates an instance of {@link OrderValue} with the given visual order value.
         *
         * @param cols the visual order value to set
         * @return an instance of {@link OffsetValue} with the given visual order value
         */
        public static OrderValue columns(int cols) {
            return new OrderValue(cols);
        }

        /**
         * @return the visual order value or {@code null} if not defined
         */
        @Nullable
        public Integer getOrder() {
            return order;
        }

        /**
         * @return the visual order value represented by a special word
         * or {@code null} if not defined
         */
        @Nullable
        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof OrderValue)) {
                return false;
            }

            OrderValue that = (OrderValue) o;
            return Objects.equals(order, that.order) &&
                    Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(order, value);
        }
    }

    /**
     * Class containing information about the number of columns by which
     * to move a column to the right.
     * <p>
     * Corresponds to the {@code .offset-*} styles.
     *
     * @see Column#setOffset(Map)
     */
    class OffsetValue {

        protected int cols;

        public OffsetValue(int cols) {
            Preconditions.checkArgument(cols >= 0,
                    "Columns value must be greater or equal to 0");
            this.cols = cols;
        }

        /**
         * Creates an instance of {@link OffsetValue} with the given number of columns.
         *
         * @param cols the number of columns to set
         * @return an instance of {@link OffsetValue} with the given number of columns
         */
        public static OffsetValue columns(int cols) {
            return new OffsetValue(cols);
        }

        /**
         * @return the number of columns by which to move a column to the right
         */
        public int getColumns() {
            return cols;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof OffsetValue)) {
                return false;
            }

            OffsetValue that = (OffsetValue) o;
            return cols == that.cols;
        }

        @Override
        public int hashCode() {
            return Objects.hash(cols);
        }
    }

    /**
     * Interface to provide default functionality for grid elements.
     */
    interface GridElement {

        /**
         * @return current id, {@code null} if not set
         */
        String getId();

        /**
         * Sets an nonunique id for grid element that is used as a debug identifier.
         *
         * @param id an alphanumeric id
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        @StudioProperty(name = "id", type = PropertyType.COMPONENT_ID)
        void setId(String id);

        /**
         * Gets all user-defined CSS style names of the grid element. If the grid element
         * has multiple style names defined, the returned string is a space-separated
         * list of style names.
         *
         * @return the style name or a space-separated list of user-defined style names
         */
        String getStyleName();

        /**
         * Sets one or more user-defined style names of the grid element, replacing any
         * previous user-defined styles. Multiple styles can be specified as a
         * space-separated list of style names.
         *
         * @param styleName one or more style names separated by space
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        @StudioProperty(name = "stylename", type = PropertyType.CSS_CLASSNAME_LIST)
        void setStyleName(String styleName);

        /**
         * Adds one or more style names to this grid element. Multiple styles can be
         * specified as a space-separated list of style names.
         *
         * @param styleName the style name or style names to be added
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        void addStyleName(String styleName);

        /**
         * Removes one or more style names to this grid element. Multiple styles can be
         * specified as a space-separated list of style names.
         *
         * @param styleName the style name or style names to be removed
         * @throws IllegalStateException if parent responsive grid layout has been shown on a screen
         */
        void removeStyleName(String styleName);
    }
}
