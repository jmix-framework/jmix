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

import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.reports.annotation.*;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.yarg.structure.DefaultValueProvider;
import io.jmix.reportsflowui.test_support.entity.Publisher;

@ReportDef(
        name = "Sample report with default value provider",
        code = SampleDefaultValueReport.CODE,
        description = """
                Uses the following features:
                - default value provider for input parameter
                """
)
@InputParameterDef(
        alias = SampleDefaultValueReport.PARAM_PUBLISHER,
        name = "Publisher",
        type = ParameterType.ENTITY,
        required = true,
        entity = @EntityParameterDef(
                entityClass = Publisher.class
        )
)
@BandDef(
        name = "Root",
        root = true
)
@TemplateDef(
        code = "default",
        outputType = ReportOutputType.HTML,
        isDefault = true,
        filePath = "io/jmix/outside_reportsflowui/SomeTemplate.html"
)
public class SampleDefaultValueReport {
    public static final String CODE = "SAMPLE_DEFAULT_VALUE";
    public static final String PARAM_PUBLISHER = "publisher";

    private final UnconstrainedDataManager unconstrainedDataManager;

    public SampleDefaultValueReport(UnconstrainedDataManager unconstrainedDataManager) {
        this.unconstrainedDataManager = unconstrainedDataManager;
    }

    @InputParameterDelegate(alias = SampleDefaultValueReport.PARAM_PUBLISHER)
    public DefaultValueProvider<Publisher> publisherNameDefaultValue() {
        return (parameter) -> {
            return unconstrainedDataManager.load(Publisher.class)
                    .condition(PropertyCondition.equal("name", "Ubisoft"))
                    .one();
        };
    }
}
