package io.jmix.reportsui.role;

import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityui.role.annotation.ScreenPolicy;

import static io.jmix.security.model.EntityAttributePolicyAction.VIEW;

/**
 * Role that grants minimal permissions to run reports.
 */
@ResourceRole(name = "Reports: run reports", code = ReportsRunRole.CODE, scope = SecurityScope.UI)
public interface ReportsRunRole {

    String CODE = "report-run";

    @ScreenPolicy(screenIds = {
            "report_inputParameters",
            "report_Report.run",
            "report_showReportTable",
            "report_showPivotTable",
            "report_showChart",
            "commonLookup"
    })
    @EntityPolicy(entityClass = Report.class, actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityClass = ReportGroup.class, actions = {EntityPolicyAction.READ})
    @EntityPolicy(entityClass = ReportTemplate.class, actions = {EntityPolicyAction.READ})
    @EntityAttributePolicy(entityClass = Report.class, attributes = {"locName", "description", "code", "updateTs", "group"}, action = VIEW)
    @EntityAttributePolicy(entityClass = ReportGroup.class, attributes = {"title", "localeNames"}, action = VIEW)
    @EntityAttributePolicy(entityClass = ReportTemplate.class, attributes = {"code", "name", "customDefinition", "custom", "alterable"}, action = VIEW)
    void reportsRun();
}
