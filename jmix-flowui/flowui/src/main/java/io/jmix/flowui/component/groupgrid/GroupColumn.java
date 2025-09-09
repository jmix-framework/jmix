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

import org.springframework.lang.Nullable;

/**
 * Interface to be implemented by group columns.
 *
 * @param <E> item type
 */
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
