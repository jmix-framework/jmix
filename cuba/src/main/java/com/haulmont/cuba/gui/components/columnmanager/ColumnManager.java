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

package com.haulmont.cuba.gui.components.columnmanager;

import io.jmix.ui.component.AggregationInfo;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.TreeTable;

import javax.annotation.Nullable;

/**
 * Class provides methods for {@link Table.Column} manipulation.
 *
 * @see Table
 * @see GroupTable
 * @see TreeTable
 */
@Deprecated
public interface ColumnManager {

    /**
     * Assign caption for column in runtime.
     *
     * @param columnId column id
     * @param caption  column caption
     * @deprecated Use {@link Table.Column#setCaption(String)} instead
     */
    @Deprecated
    void setColumnCaption(String columnId, @Nullable String caption);

    /**
     * Assign caption for column in runtime.
     *
     * @param column  column instance
     * @param caption column caption
     * @deprecated Use {@link Table.Column#setCaption(String)} instead
     */
    @Deprecated
    void setColumnCaption(Table.Column column, @Nullable String caption);

    /**
     * Assign description for column in runtime.
     *
     * @param columnId    column id
     * @param description column description
     * @deprecated Use {@link Table.Column#setDescription(String)} instead
     */
    @Deprecated
    void setColumnDescription(String columnId, @Nullable String description);

    /**
     * Assign description for column in runtime.
     *
     * @param column      column instance
     * @param description column description
     * @deprecated Use {@link Table.Column#setDescription(String)} instead
     */
    @Deprecated
    void setColumnDescription(Table.Column column, @Nullable String description);

    /**
     * Sets text alignment for a column in runtime.
     *
     * @param columnId  column id
     * @param alignment text alignment
     * @deprecated Use {@link Table.Column#setAlignment(Table.ColumnAlignment)} instead
     */
    @Deprecated
    void setColumnAlignment(String columnId, Table.ColumnAlignment alignment);

    /**
     * Sets text alignment for a column in runtime.
     *
     * @param column    column instance
     * @param alignment text alignment
     * @deprecated Use {@link Table.Column#setAlignment(Table.ColumnAlignment)} instead
     */
    @Deprecated
    void setColumnAlignment(Table.Column column, Table.ColumnAlignment alignment);

    /**
     * Show/hide column in runtime. Hidden column will be available in column control.
     *
     * @param columnId  column id
     * @param collapsed collapsed option
     * @deprecated Use {@link Table.Column#setCollapsed(boolean)} instead
     */
    @Deprecated
    void setColumnCollapsed(String columnId, boolean collapsed);

    /**
     * Show/hide column in runtime. Hidden column will be available in column control.
     *
     * @param column    column instance
     * @param collapsed collapsed option
     * @deprecated Use {@link Table.Column#setCollapsed(boolean)} instead
     */
    @Deprecated
    void setColumnCollapsed(Table.Column column, boolean collapsed);

    /**
     * Set column width in runtime.
     *
     * @param columnId column id
     * @param width    column width
     * @deprecated Use {@link Table.Column#setWidth(Integer)} instead
     */
    @Deprecated
    void setColumnWidth(String columnId, int width);

    /**
     * Set column width in runtime.
     *
     * @param column column instance
     * @param width  column width
     * @deprecated Use {@link Table.Column#setWidth(Integer)} instead
     */
    @Deprecated
    void setColumnWidth(Table.Column column, int width);

    /**
     * Adds aggregation type to the column.
     *
     * @param columnId column id
     * @param type     aggregation type
     * @deprecated Use {@link Table.Column#setAggregation(AggregationInfo)} instead
     */
    @Deprecated
    void addAggregationProperty(String columnId, AggregationInfo.Type type);

    /**
     * Adds aggregation type to the column.
     *
     * @param columnId column id
     * @param type     aggregation type
     * @deprecated Use {@link Table.Column#setAggregation(AggregationInfo)} instead
     */
    @Deprecated
    void addAggregationProperty(Table.Column columnId, AggregationInfo.Type type);

    /**
     * Removes the column from aggregation cells list.
     *
     * @param columnId column id
     * @deprecated Use {@link Table.Column#setAggregation(AggregationInfo)} instead
     */
    @Deprecated
    void removeAggregationProperty(String columnId);

    /**
     * Enables sortable for the column.
     *
     * @param columnId column id
     * @param sortable sortable option
     * @deprecated Use {@link Table.Column#setSortable(boolean)} instead
     */
    @Deprecated
    void setColumnSortable(String columnId, boolean sortable);

    /**
     * Enables sortable for the column.
     *
     * @param column   column instance
     * @param sortable sortable option
     * @deprecated Use {@link Table.Column#setSortable(boolean)} instead
     */
    @Deprecated
    void setColumnSortable(Table.Column column, boolean sortable);

    /**
     * Sets whether caption of column with the given {@code columnId} should be interpreted as HTML or not.
     *
     * @param columnId      column id
     * @param captionAsHtml interpret caption as HTML or not
     * @deprecated Use {@link Table.Column#setCaptionAsHtml(boolean)} instead
     */
    @Deprecated
    void setColumnCaptionAsHtml(String columnId, boolean captionAsHtml);

    /**
     * Sets whether caption of the given {@code column} should be interpreted as HTML or not.
     *
     * @param column        column
     * @param captionAsHtml interpret caption as HTML or not
     * @deprecated Use {@link Table.Column#setCaptionAsHtml(boolean)} instead
     */
    @Deprecated
    void setColumnCaptionAsHtml(Table.Column column, boolean captionAsHtml);

    /**
     * Sets expand ratio for a column.
     *
     * @param column a column to set expand ratio
     * @param ratio  ratio
     */
    @Deprecated
    void setColumnExpandRatio(Table.Column column, float ratio);

    /**
     * @param column a column to get ratio
     * @return ratio for the column
     */
    @Deprecated
    float getColumnExpandRatio(Table.Column column);

    /**
     * Registers a new cell click listener for given column.
     *
     * @param columnId id of column
     */
    @Deprecated
    void addCellClickListener(String columnId);

    /**
     * Removes a previously registered cell click listener for given column.
     *
     * @param columnId id of column
     */
    @Deprecated
    void removeCellClickListener(String columnId);
}
