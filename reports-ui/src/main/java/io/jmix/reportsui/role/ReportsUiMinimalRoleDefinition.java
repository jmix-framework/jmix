package io.jmix.reportsui.role;

import io.jmix.reports.role.ReportsMinimalRoleDefinition;
import io.jmix.security.role.annotation.Role;
import io.jmix.securityui.role.annotation.ScreenPolicy;

/**
 * System role that grants minimal permissions for run reports required for all users of generic UI client.
 */
@Role(name = ReportsUiMinimalRoleDefinition.ROLE_NAME, code = ReportsUiMinimalRoleDefinition.ROLE_NAME)
public interface ReportsUiMinimalRoleDefinition extends ReportsMinimalRoleDefinition {

    String ROLE_NAME = "ui-reports-minimal";

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
