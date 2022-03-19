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

package io.jmix.ui.widget;

import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Grid;
import com.vaadin.ui.components.grid.GridSelectionModel;
import io.jmix.ui.widget.grid.JmixEditorField;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

public interface JmixEnhancedGrid<T> {

    void setGridSelectionModel(GridSelectionModel<T> model);

    Map<String, String> getColumnIds();

    void setColumnIds(Map<String, String> ids);

    void addColumnId(String column, String value);

    void removeColumnId(String column);

    void repaint();

    JmixGridEditorFieldFactory<T> getJmixEditorFieldFactory();

    void setJmixEditorFieldFactory(JmixGridEditorFieldFactory<T> editorFieldFactory);

    JmixEditorField<?> getColumnEditorField(T bean, Grid.Column<T, ?> column);

    void setBeforeRefreshHandler(Consumer<T> beforeRefreshHandler);

    void setShowEmptyState(boolean show);

    @Nullable
    String getEmptyStateMessage();
    void setEmptyStateMessage(@Nullable String message);

    @Nullable
    String getEmptyStateLinkMessage();
    void setEmptyStateLinkMessage(@Nullable String linkMessage);

    void setEmptyStateLinkClickHandler(Runnable handler);

    /**
     * CAUTION! Safari hides footer while changing predefined styles at runtime. Given method updates footer visibility
     * without changing its value.
     */
    void updateFooterVisibility();

    String getSelectAllLabel();

    void setSelectAllLabel(String selectAllLabel);

    String getDeselectAllLabel();

    void setDeselectAllLabel(String deselectAllLabel);

    boolean isAggregatable();

    void setAggregatable(boolean aggregatable);

    AggregationPosition getAggregationPosition();

    void setAggregationPosition(AggregationPosition position);

    void addAggregationPropertyId(String propertyId);

    void removeAggregationPropertyId(String propertyId);

    Collection<String> getAggregationPropertyIds();

    ContentMode getRowDescriptionContentMode();

    @Nullable
    Float getMinHeight();

    @Nullable
    Sizeable.Unit getMinHeightSizeUnit();

    void setMinHeight(@Nullable String minHeight);

    @Nullable
    Float getMinWidth();

    @Nullable
    Sizeable.Unit getMinWidthSizeUnit();

    void setMinWidth(@Nullable String minWidth);

    /**
     * Defines the position of aggregation row.
     */
    enum AggregationPosition {
        TOP,
        BOTTOM
    }
}
