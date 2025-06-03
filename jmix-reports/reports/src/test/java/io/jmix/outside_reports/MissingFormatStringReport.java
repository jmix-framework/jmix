package io.jmix.outside_reports;

import io.jmix.reports.annotation.BandDef;
import io.jmix.reports.annotation.ReportDef;
import io.jmix.reports.annotation.TemplateDef;
import io.jmix.reports.annotation.ValueFormatDef;
import io.jmix.reports.entity.Orientation;
import io.jmix.reports.entity.ReportOutputType;

@ReportDef(
        name = "Some name",
        code = "some-code"
)
@BandDef(
        name = "Root",
        root = true,
        orientation = Orientation.HORIZONTAL
)
@BandDef(
        name = "users",
        parent = "Root",
        orientation = Orientation.HORIZONTAL
)
@TemplateDef(
        code = "default",
        outputType = ReportOutputType.HTML,
        isDefault = true,
        filePath = "io/jmix/outside_reports/SomeTemplate.html"
)
@ValueFormatDef(
        band = "users",
        field = "registrationDate"
        // no format string, no formatting delegate
)
public class MissingFormatStringReport {

}