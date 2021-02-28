package io.jmix.reportsui.role;

import io.jmix.reports.role.ReportsRunRole;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityui.role.annotation.ScreenPolicy;

/**
 * Role that grants minimal permissions to run reports in UI.
 */
@ResourceRole(name = "Reports: run reports in UI", code = ReportsRunUiRole.CODE)
public interface ReportsRunUiRole extends ReportsRunRole {

    String CODE = "report-run-ui";

    @ScreenPolicy(screenIds = {
            "report_inputParameters",
            "report_Report.run",
            "report_showReportTable",
            "report_showPivotTable",
            "report_showChart",
            "commonLookup"
    })
    @Override
    void access();
}
