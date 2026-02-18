/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.component.groupgrid;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import io.jmix.core.annotation.Experimental;
import org.springframework.lang.Nullable;

/**
 * Interface to be implemented by group columns. The group column is a special column used to display a hierarchy of
 * grouped items, manage the grouping and expand/collapse group rows.
 *
 * @param <E> item type
 */
@Experimental
public interface GroupColumn<E> {

    /**
     * @return {@code true} if the column is automatically hidden when there are no grouping columns
     */
    boolean isAutoHidden();

    /**
     * Sets whether the column is automatically hidden when there are no grouping columns.
     * <p>
     * The default value is {@code false}.
     *
     * @param autoHidden autoHidden option
     */
    void setAutoHidden(boolean autoHidden);

    /**
     * @return {@code true} if the column displays the number of items in each group
     */
    boolean isDisplayItemsCount();

    /**
     * Sets whether the column displays the number of items in each group.
     * <p>
     * The default value is {@code true}.
     *
     * @param displayItemsCount displayItemsCount option
     */
    void setDisplayItemsCount(boolean displayItemsCount);

    /**
     * @return {@code true} if the column displays a button that opens a popup that manages columns grouping
     */
    boolean isDisplayColumnsGrouperOnIconClick();

    /**
     * Sets whether the column displays a button that opens a popup that manages columns grouping.
     * <p>
     * The default value is {@code true}.
     *
     * @param displayColumnsGrouper displayColumnsGrouper option
     */
    void setDisplayColumnsGrouperOnIconClick(boolean displayColumnsGrouper);

    /**
     * @return the icon for the Columns Grouper button or {@code null} if the default icon is used
     * @deprecated use {@link #getGroupIconComponent()} instead
     */
    @Deprecated(since = "2.8", forRemoval = true)
    @Nullable
    Icon getGroupIcon();

    /**
     * Sets the icon for the Columns Grouper button.
     *
     * @param icon the icon to set, or {@code null} to set the default icon
     * @deprecated use {@link #setGroupIconComponent(Component)} instead
     */
    @Deprecated(since = "2.8", forRemoval = true)
    void setGroupIcon(@Nullable Icon icon);

    /**
     * @return the icon for the Columns Grouper button or {@code null} if the default icon is used
     */
    @Nullable
    Component getGroupIconComponent();

    /**
     * Sets the icon for the Columns Grouper button.
     *
     * @param icon the icon to set, or {@code null} to set the default icon
     */
    void setGroupIconComponent(@Nullable Component icon);

    /**
     * @return {@code true} if the icon for the Columns Grouper button is visible
     */
    boolean isGroupIconVisible();

    /**
     * Sets whether the icon for the Columns Grouper button is visible.
     * <p>
     * The default value is {@code true}.
     *
     * @param visible visible option
     */
    void setGroupIconVisible(boolean visible);

    /**
     * @return a formatter for group cell values or {@code null} if no formatter is set.
     */
    @Nullable
    GroupCellValueFormatter<E> getGroupCellValueFormatter();

    /**
     * Sets a formatter for group cell values.
     *
     * @param groupCellValueFormatter a formatter to set, or {@code null} to remove the formatter
     */
    void setGroupCellValueFormatter(@Nullable GroupCellValueFormatter<E> groupCellValueFormatter);
}
