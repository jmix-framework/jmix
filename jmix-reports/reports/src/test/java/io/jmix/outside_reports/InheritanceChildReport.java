package io.jmix.outside_reports;

import io.jmix.reports.annotation.ReportDef;
import io.jmix.reports.annotation.ValueFormatDef;
import io.jmix.reports.annotation.ValueFormatDelegate;
import io.jmix.reports.yarg.structure.CustomValueFormatter;

@ReportDef(
        name = "Inheritance report",
        code = "inheritance-report",
        uuid = "01973162-6761-7113-9d6b-b4ef0bea42f0",
        group = CorrectReportGroup.class
)
@ValueFormatDef(
        band = "title",
        field = "caption"
)
public class InheritanceChildReport extends InheritanceParentReport implements InheritanceAspectReport {

    @ValueFormatDelegate(band = "title", field = "caption")
    public CustomValueFormatter<String> titleCaptionFormatter() {
        return value -> {
            return value.toUpperCase();
        };
    }
}