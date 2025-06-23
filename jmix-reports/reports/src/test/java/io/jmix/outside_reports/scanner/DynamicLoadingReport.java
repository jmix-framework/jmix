package io.jmix.outside_reports.scanner;

import io.jmix.reports.annotation.BandDef;
import io.jmix.reports.annotation.ReportDef;
import io.jmix.reports.annotation.TemplateDef;
import io.jmix.reports.entity.ReportOutputType;

@ReportDef(
        name = "Dynamic loading report",
        code = DynamicLoadingReport.CODE
)
@BandDef(
        name = "Root",
        root = true
)
@TemplateDef(
        code = "default",
        outputType = ReportOutputType.CSV,
        isDefault = true,
        filePath = "io/jmix/outside_reports/CorrectReport.csv"
)
public class DynamicLoadingReport {
    public static final String CODE = "DYNAMIC_LOAD_REPORT";
}