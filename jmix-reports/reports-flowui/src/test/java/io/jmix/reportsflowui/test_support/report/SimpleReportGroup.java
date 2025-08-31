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

package io.jmix.reportsflowui.test_support.report;

import io.jmix.reports.annotation.ReportGroupDef;

@ReportGroupDef(
        title = "msg://SimpleReportGroup.title",
        code = SimpleReportGroup.CODE,
        uuid = "15c81a52-09fc-4de7-e08a-b8a9a9155f15",
        beanName = "sample_demoReportGroup"
)
public class SimpleReportGroup {
    public static final String CODE = "GROUP-1";
}
