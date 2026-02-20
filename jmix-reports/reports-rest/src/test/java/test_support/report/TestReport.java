package test_support.report;

import io.jmix.reports.annotation.*;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.yarg.loaders.ReportDataLoader;

import java.util.List;
import java.util.Map;

@ReportDef(
        code = "dt-test-1",
        name = "Test Report",
        restAccessible = true
)
@TemplateDef(
        isDefault = true,
        code = "DEFAULT",
        filePath = "/test_support/dt-test-1.csv",
        outputType = ReportOutputType.CSV,
        outputNamePattern = "dt-test-1.csv"
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
public class TestReport {

    @DataSetDelegate(name = "data")
        public ReportDataLoader dataLoader() {
           return (reportQuery, parentBand, params) ->
                List.of(Map.of("field1", "value1", "field2", "value2"));
    }
}