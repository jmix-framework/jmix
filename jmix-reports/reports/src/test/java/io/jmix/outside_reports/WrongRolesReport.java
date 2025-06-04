package io.jmix.outside_reports;

import io.jmix.reports.annotation.AvailableForRoles;
import io.jmix.reports.annotation.BandDef;
import io.jmix.reports.annotation.ReportDef;
import io.jmix.reports.annotation.TemplateDef;
import io.jmix.reports.entity.Orientation;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.test_support.role.TestRowLevelRole3;

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
        roleCodes = TestRowLevelRole3.CODE // wrong type of role
)
public class WrongRolesReport {

}