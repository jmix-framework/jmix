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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// A DATE parameter with a string default value and a validator typed on LocalDate (the canonical parameter class,
// what the engine and the UI use). It verifies the REST run path validates the default value in that same type, not
// in the REST wire type (java.sql.Date) - otherwise a LocalDate-typed validator would fail with a ClassCastException.
@ReportDef(
        code = "dt-validated-date",
        name = "Date Default Validated Test Report",
        restAccessible = true
)
@InputParameterDef(
        alias = DateDefaultValidatedTestReport.PARAM,
        name = "Date param",
        type = ParameterType.DATE,
        defaultValue = DateDefaultValidatedTestReport.INVALID_DEFAULT_VALUE
)
@TemplateDef(
        isDefault = true,
        code = "DEFAULT",
        filePath = "/test_support/dt-test-1.csv",
        outputType = ReportOutputType.CSV,
        outputNamePattern = "dt-validated-date.csv"
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
public class DateDefaultValidatedTestReport {

    public static final String PARAM = "dateParam";

    public static final String INVALID_DEFAULT_VALUE = "2099-12-31";

    public static final String VALIDATION_MESSAGE = "date must not be in the far future";

    @DataSetDelegate(name = "data")
    public ReportDataLoader dataLoader() {
        return (reportQuery, parentBand, params) ->
                List.of(Map.of("field1", "value1", "field2", "value2"));
    }

    @InputParameterDelegate(alias = PARAM)
    public ParameterValidator<LocalDate> dateValidator() {
        return value -> {
            if (value.getYear() > 2050) {
                throw new ReportParametersValidationException(VALIDATION_MESSAGE);
            }
        };
    }
}
