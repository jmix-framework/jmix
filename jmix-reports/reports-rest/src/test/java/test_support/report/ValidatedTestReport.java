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
import io.jmix.reports.delegate.ParametersCrossValidator;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.exception.ReportParametersValidationException;
import io.jmix.reports.yarg.loaders.ReportDataLoader;

import java.util.List;
import java.util.Map;

// This report carries both a single-parameter validator and a cross-parameter validator so the REST
// run path can be verified to invoke report input validation the same way the UI does.
@ReportDef(
        code = "dt-validated-1",
        name = "Validated Test Report",
        restAccessible = true
)
@InputParameterDef(
        alias = ValidatedTestReport.PARAM_1,
        name = "Param 1",
        type = ParameterType.TEXT
)
@InputParameterDef(
        alias = ValidatedTestReport.PARAM_2,
        name = "Param 2",
        type = ParameterType.TEXT
)
@TemplateDef(
        isDefault = true,
        code = "DEFAULT",
        filePath = "/test_support/dt-test-1.csv",
        outputType = ReportOutputType.CSV,
        outputNamePattern = "dt-validated-1.csv"
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
public class ValidatedTestReport {

    public static final String PARAM_1 = "param1";
    public static final String PARAM_2 = "param2";

    public static final String INVALID_PARAM_VALUE = "invalid-param";
    public static final String INVALID_CROSS_VALUE = "invalid-cross";

    public static final String PARAM_VALIDATION_MESSAGE = "param1 has an invalid value";
    public static final String CROSS_VALIDATION_MESSAGE = "param2 fails cross validation";

    @DataSetDelegate(name = "data")
    public ReportDataLoader dataLoader() {
        return (reportQuery, parentBand, params) ->
                List.of(Map.of("field1", "value1", "field2", "value2"));
    }

    @InputParameterDelegate(alias = PARAM_1)
    public ParameterValidator<String> param1Validator() {
        return value -> {
            if (INVALID_PARAM_VALUE.equals(value)) {
                throw new ReportParametersValidationException(PARAM_VALIDATION_MESSAGE);
            }
        };
    }

    @ReportDelegate
    public ParametersCrossValidator crossValidator() {
        return parameterValues -> {
            if (INVALID_CROSS_VALUE.equals(parameterValues.get(PARAM_2))) {
                throw new ReportParametersValidationException(CROSS_VALIDATION_MESSAGE);
            }
        };
    }
}
