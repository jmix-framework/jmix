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

package test_support.report;

import io.jmix.reports.annotation.*;
import io.jmix.reports.delegate.ParameterValidator;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.exception.ReportParametersValidationException;
import io.jmix.reports.yarg.loaders.ReportDataLoader;
import io.jmix.reports.yarg.structure.DefaultValueProvider;

import java.util.List;
import java.util.Map;

// The single parameter has a default value that its own validator rejects. It lets the REST run path be verified to
// validate the effective (default-substituted) value of a parameter the caller did not pass, the same way the UI does.
@ReportDef(
        code = "dt-validated-default",
        name = "Default Value Validated Test Report",
        restAccessible = true
)
@InputParameterDef(
        alias = DefaultValueValidatedTestReport.PARAM,
        name = "Param",
        type = ParameterType.TEXT
)
@TemplateDef(
        isDefault = true,
        code = "DEFAULT",
        filePath = "/test_support/dt-test-1.csv",
        outputType = ReportOutputType.CSV,
        outputNamePattern = "dt-validated-default.csv"
)
@BandDef(
        name = "Root",
        root = true
)
@BandDef(
        name = "Data",
        parent = "Root",
        dataSets = @DataSetDef(
                name = "data",
                type = DataSetType.DELEGATE
        )
)
public class DefaultValueValidatedTestReport {

    public static final String PARAM = "param";

    public static final String INVALID_DEFAULT_VALUE = "invalid-default";

    public static final String VALIDATION_MESSAGE = "param default value is invalid";

    @DataSetDelegate(name = "data")
    public ReportDataLoader dataLoader() {
        return (reportQuery, parentBand, params) ->
                List.of(Map.of("field1", "value1", "field2", "value2"));
    }

    @InputParameterDelegate(alias = PARAM)
    public DefaultValueProvider<String> paramDefaultValue() {
        return parameter -> INVALID_DEFAULT_VALUE;
    }

    @InputParameterDelegate(alias = PARAM)
    public ParameterValidator<String> paramValidator() {
        return value -> {
            if (INVALID_DEFAULT_VALUE.equals(value)) {
                throw new ReportParametersValidationException(VALIDATION_MESSAGE);
            }
        };
    }
}
