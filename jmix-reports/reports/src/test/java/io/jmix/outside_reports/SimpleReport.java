package io.jmix.outside_reports;

import io.jmix.reports.annotation.*;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.Orientation;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.yarg.loaders.ReportDataLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ReportDef(
        name = "Simple report",
        code = "simple-report",
        uuid = "01973162-6761-71a1-9d6b-b4ef0bea42f0",
        group = CorrectReportGroup.class,
        restAccessible = true
)
@InputParameterDef(
        alias = "afterDate",
        name = "msg://SimpleReport.afterDate",
        type = ParameterType.DATETIME,
        required = true
)
@BandDef(
        name = "Root",
        root = true,
        orientation = Orientation.HORIZONTAL
)
@BandDef(
        name = "title",
        parent = "Root",
        orientation = Orientation.HORIZONTAL,
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
        field = "date",
        format = "dd.MM.yyyy HH:mm:ss"
)
public class SimpleReport {

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

}