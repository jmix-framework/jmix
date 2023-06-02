/*
 * Copyright 2021 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.reportsflowui.role;

import io.jmix.reports.entity.*;
import io.jmix.reports.entity.charts.ChartSeries;
import io.jmix.reports.entity.charts.PieChartDescription;
import io.jmix.reports.entity.charts.SerialChartDescription;
import io.jmix.reports.entity.pivottable.PivotTableAggregation;
import io.jmix.reports.entity.pivottable.PivotTableDescription;
import io.jmix.reports.entity.pivottable.PivotTableProperty;
import io.jmix.reports.entity.table.TemplateTableBand;
import io.jmix.reports.entity.table.TemplateTableColumn;
import io.jmix.reports.entity.table.TemplateTableDescription;
import io.jmix.reports.entity.wizard.EntityTreeNode;
import io.jmix.reports.entity.wizard.RegionProperty;
import io.jmix.reports.entity.wizard.ReportData;
import io.jmix.reports.entity.wizard.ReportRegion;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

/**
 * Role that grants full access to working with reports.
 */
@ResourceRole(name = "Reports: full access", code = ReportsFullAccessRole.CODE, scope = SecurityScope.UI)
public interface ReportsFullAccessRole {

	@ViewPolicy(viewIds = {
			"report_InputParametersDialog.view",
			"report_ReportRun.view",
			"report_ReportTable.view",
			"report_ReportGroup.list",
			"report_Report.list",
			"report_Report.detail",
			"report_ReportInputParameter.detail",
			"report_ReportImportDialog.view",
			"report_Region.detail",
			"report_ReportWizardCreator.view",
			"report_EntityTreeList.list",
			"report_ReportExecution.list",
			"report_ReportExecutionDialog.view",
			"report_ReportGroup.detail",
			"report_InputParametersDialog.view",
			"report_ReportTemplate.detail",
			"report_ReportValueFormat.detail",
			"report_ScriptEditor.view"
	})

	@MenuPolicy(menuIds = {
			"report_Report.list",
			"report_ReportGroup.list",
			"report_ReportRun.view",
			"report_ReportTable.view"
	})

	@EntityPolicy(entityClass = Report.class, actions = EntityPolicyAction.ALL)
	@EntityPolicy(entityClass = ReportGroup.class, actions = EntityPolicyAction.ALL)
	@EntityPolicy(entityClass = ReportTemplate.class, actions = EntityPolicyAction.ALL)
	@EntityPolicy(entityClass = ReportExecution.class, actions = EntityPolicyAction.ALL)
	@EntityPolicy(entityClass = ChartSeries.class, actions = EntityPolicyAction.ALL)
	@EntityPolicy(entityClass = PieChartDescription.class, actions = EntityPolicyAction.ALL)
	@EntityPolicy(entityClass = SerialChartDescription.class, actions = EntityPolicyAction.ALL)
	@EntityPolicy(entityClass = PivotTableAggregation.class, actions = EntityPolicyAction.ALL)
	@EntityPolicy(entityClass = PivotTableDescription.class, actions = EntityPolicyAction.ALL)
	@EntityPolicy(entityClass = PivotTableProperty.class, actions = EntityPolicyAction.ALL)
	@EntityPolicy(entityClass = TemplateTableBand.class, actions = EntityPolicyAction.ALL)
	@EntityPolicy(entityClass = TemplateTableColumn.class, actions = EntityPolicyAction.ALL)
	@EntityPolicy(entityClass = TemplateTableDescription.class, actions = EntityPolicyAction.ALL)
	@EntityPolicy(entityClass = EntityTreeNode.class, actions = EntityPolicyAction.ALL)
	@EntityPolicy(entityClass = RegionProperty.class, actions = EntityPolicyAction.ALL)
	@EntityPolicy(entityClass = ReportData.class, actions = EntityPolicyAction.ALL)
	@EntityPolicy(entityClass = ReportRegion.class, actions = EntityPolicyAction.ALL)
	@EntityPolicy(entityClass = BandDefinition.class, actions = EntityPolicyAction.ALL)
	@EntityPolicy(entityClass = DataSet.class, actions = EntityPolicyAction.ALL)
	@EntityPolicy(entityClass = ReportInputParameter.class, actions = EntityPolicyAction.ALL)
	@EntityPolicy(entityClass = ReportScreen.class, actions = EntityPolicyAction.ALL)
	@EntityPolicy(entityClass = ReportValueFormat.class, actions = EntityPolicyAction.ALL)
	@EntityAttributePolicy(entityClass = Report.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
	@EntityAttributePolicy(entityClass = ReportGroup.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
	@EntityAttributePolicy(entityClass = ReportTemplate.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
	@EntityAttributePolicy(entityClass = ReportExecution.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
	@EntityAttributePolicy(entityClass = ChartSeries.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
	@EntityAttributePolicy(entityClass = PieChartDescription.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
	@EntityAttributePolicy(entityClass = SerialChartDescription.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
	@EntityAttributePolicy(entityClass = PivotTableAggregation.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
	@EntityAttributePolicy(entityClass = PivotTableDescription.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
	@EntityAttributePolicy(entityClass = PivotTableProperty.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
	@EntityAttributePolicy(entityClass = TemplateTableBand.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
	@EntityAttributePolicy(entityClass = TemplateTableColumn.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
	@EntityAttributePolicy(entityClass = TemplateTableDescription.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
	@EntityAttributePolicy(entityClass = EntityTreeNode.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
	@EntityAttributePolicy(entityClass = RegionProperty.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
	@EntityAttributePolicy(entityClass = ReportData.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
	@EntityAttributePolicy(entityClass = ReportRegion.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
	@EntityAttributePolicy(entityClass = BandDefinition.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
	@EntityAttributePolicy(entityClass = DataSet.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
	@EntityAttributePolicy(entityClass = ReportInputParameter.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
	@EntityAttributePolicy(entityClass = ReportScreen.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
	@EntityAttributePolicy(entityClass = ReportValueFormat.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
	void reportsFullAccess();

	String CODE = "reports-full-access";
}