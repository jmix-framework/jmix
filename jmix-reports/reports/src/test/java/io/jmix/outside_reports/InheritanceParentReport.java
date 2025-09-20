package io.jmix.outside_reports;

import io.jmix.reports.annotation.*;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.yarg.loaders.ReportDataLoader;
import io.jmix.reports.yarg.structure.CustomValueFormatter;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@BandDef(
        name = "Root",
        root = true
)
@BandDef(
        name = "title",
        parent = "Root",
        dataSets = @DataSetDef(
                name = "title",
                type = DataSetType.DELEGATE
        )
)
@TemplateDef(
        code = "default",
        outputType = ReportOutputType.CSV,
        isDefault = true,
        filePath = "io/jmix/outside_reports/CorrectReport.csv",
        outputNamePattern = "${title.caption}.csv"
)
@ValueFormatDef(
        band = "title",
        field = "date"
)
public abstract class InheritanceParentReport {

    @DataSetDelegate(name = "title")
    public ReportDataLoader titleDataLoader() {
        return (reportQuery, parentBand, parameters) -> {
            return List.of(
                    new HashMap<>(Map.of(
                            "caption",
                            "Hello reports",
                            "date",
                            parameters.get("afterDate")
                    ))
            );
        };
    }

    @ValueFormatDelegate(band = "title", field = "date")
    public CustomValueFormatter<Date> titleDateFormatter() {
        return value -> {
            return value.toString();
        };
    }
}