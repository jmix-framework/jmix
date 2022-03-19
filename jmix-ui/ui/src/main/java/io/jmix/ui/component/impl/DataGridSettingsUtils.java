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

package io.jmix.ui.component.impl;

import io.jmix.core.annotation.Internal;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.settings.component.binder.AbstractDataGridSettingsBinder;
import io.jmix.ui.settings.component.binder.DataGridSettingsBinder;
import io.jmix.ui.settings.facet.ScreenSettingsFacet;

import java.util.List;

/**
 * Class provides access to protected functionality from {@link DataGrid} to support
 * restoring UI settings when the screen contains {@link ScreenSettingsFacet}.
 *
 * @see AbstractDataGridSettingsBinder
 */
@Internal
public final class DataGridSettingsUtils {

    private DataGridSettingsUtils() {
    }

    /**
     * INTERNAL API. Is used for DataGrid settings.
     *
     * @param dataGrid dataGrid
     * @param columns  columns
     * @see DataGridSettingsBinder
     */
    @Internal
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void restoreColumnsOrder(DataGrid dataGrid, List<DataGrid.Column> columns) {
        ((AbstractDataGrid) dataGrid).restoreColumnsOrderAndUpdate(columns);
    }
}
