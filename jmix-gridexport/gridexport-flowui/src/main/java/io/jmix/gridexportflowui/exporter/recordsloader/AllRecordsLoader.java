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

package io.jmix.gridexportflowui.exporter.recordsloader;

import io.jmix.flowui.data.DataUnit;
import io.jmix.gridexportflowui.exporter.EntityExporter;

public interface AllRecordsLoader {
    String getPaginationType();

    void exportAll(DataUnit dataUnit, EntityExporter entityExporter);
}
