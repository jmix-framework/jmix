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

package io.jmix.reportsui.role;

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
import io.jmix.securityui.model.ResourceRoleModel;
import io.jmix.securityui.role.annotation.MenuPolicy;
import io.jmix.securityui.role.annotation.ScreenPolicy;

/**
 * Role that grants full access to working with reports.
 */
@ResourceRole(name = "Reports: full access", code = ReportsFullAccessRole.CODE, scope = SecurityScope.UI)
public interface ReportsFullAccessRole {

	String CODE = "reports-full-access";

	@ScreenPolicy(screenIds = {
			"report_InputParameters.dialog",
			"report_Report.run",
			"report_ShowReportTable.screen",
			"report_ShowPivotTable.screen",
			"report_ShowChart.screen",
			"report_ReportGroup.browse",
			"report_Report.browse",
			"report_Report.edit",
			"report_ChartEdit.fragment",
			"report_InputParameters.fragment",
			"report_PivotTableAggregation.edit",
			"report_PivotTableEdit.fragment",
			"report_PivotTableProperty.edit",
			"report_BandDefinitionEditor.fragment",
			"report_ReportImport.dialog",
			"report_Region.edit",
			"report_ReportWizardCreator",
			"report_ReportEntityTree.lookup",
			"report_ReportExecution.browse",
			"report_ReportExecution.dialog",
			"report_ReportGroup.edit",
			"report_ReportInputParameter.edit",
			"report_ReportTemplate.edit",
			"report_ReportValueFormat.edit",
			"report_TableEdit.fragment",
			"report_ScriptEditor.dialog"
	})
	@MenuPolicy(menuIds = {
			"reports",
			"report_Report.browse",
			"report_ReportGroup.browse",
			"report_Report.run",
			"report_ShowChart.screen",
			"report_ShowReportTable.screen",
			"report_ShowPivotTable.screen"
	})
	@EntityPolicy(entityClass = ResourceRoleModel.class, actions = EntityPolicyAction.READ)
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
	@EntityAttributePolicy(entityClass = ResourceRoleModel.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
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
}