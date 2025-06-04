package io.jmix.outside_reports;

import io.jmix.reports.annotation.*;
import io.jmix.reports.entity.Orientation;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.test_support.role.TestResourceRole1;
import io.jmix.reports.test_support.role.TestResourceRole2;

@ReportDef(
        name = "Simple report",
        code = "simple-report"
)
@BandDef(
        name = "Root",
        root = true,
        orientation = Orientation.HORIZONTAL
)
@TemplateDef(
        code = "default",
        outputType = ReportOutputType.HTML,
        isDefault = true,
        filePath = "io/jmix/outside_reports/SomeTemplate.html"
)
@AvailableForRoles(
        roleCodes = TestResourceRole1.CODE,
        roleClasses = TestResourceRole2.class
)
public class ReportWithRoles {

}