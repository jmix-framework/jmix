/*
 * Copyright 2024 Haulmont.
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

package io.jmix.pivottableflowui.component;

import io.jmix.core.annotation.Internal;
import io.jmix.pivottableflowui.export.model.PivotData;
import io.jmix.pivottableflowui.kit.component.model.PivotTableOptions;

import java.util.function.Consumer;

@Internal
public class PivotTableUtils {

    public static void requestPivotTableData(PivotTable<?> pivotTable, Consumer<PivotData> consumer) {
        pivotTable.requestPivotData(consumer);
    }

    public static void setPivotTableOptions(PivotTable<?> pivotTable, PivotTableOptions pivotTableOptions) {
        pivotTable.setPivotTableOptions(pivotTableOptions);
    }
}
