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

package io.jmix.reportsflowui.helper;

import io.jmix.reports.entity.ReportOutputType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component("report_OutputTypeHelper")
public class OutputTypeHelper {

    /**
     * @return list of currently supported output types that user should see in UI
     */
    public List<ReportOutputType> getSupportedOutputTypes() {
        ArrayList<ReportOutputType> outputTypes = new ArrayList<>(Arrays.asList(ReportOutputType.values()));

        // Unsupported types for now
        outputTypes.remove(ReportOutputType.CHART);
        outputTypes.remove(ReportOutputType.PIVOT_TABLE);

        return outputTypes;
    }
}
