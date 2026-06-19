/*
 * Copyright 2026 Haulmont.
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

package io.jmix.outside_reports;

import io.jmix.reports.annotation.ReportDef;
import io.jmix.reports.annotation.ValueFormatDef;
import io.jmix.reports.annotation.ValueFormatDelegate;
import io.jmix.reports.yarg.structure.CustomValueFormatter;

@ReportDef(
        name = "Inheritance report",
        code = "inheritance-report",
        uuid = "01973162-6761-7113-9d6b-b4ef0bea42f0",
        group = CorrectReportGroup.class
)
@ValueFormatDef(
        band = "title",
        field = "caption"
)
public class InheritanceChildReport extends InheritanceParentReport implements InheritanceAspectReport {

    @ValueFormatDelegate(band = "title", field = "caption")
    public CustomValueFormatter<String> titleCaptionFormatter() {
        return value -> {
            return value.toUpperCase();
        };
    }
}