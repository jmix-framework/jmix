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

package io.jmix.dashboardschart.screen;

import io.jmix.core.DataManager;
import io.jmix.dashboardsui.annotation.WidgetParam;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.ui.WindowParam;
import io.jmix.ui.component.CheckBox;
import io.jmix.ui.component.EntityComboBox;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@UiController("dshbrd_ChartWidgetEdit")
@UiDescriptor("chart-widget-edit.xml")
public class ChartWidgetEdit extends ScreenFragment {

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected EntityComboBox<Report> reportComboBox;

    @Autowired
    protected EntityComboBox<ReportTemplate> templateComboBox;

    @Autowired
    private CheckBox refreshAutomaticallyCheckbox;

    @WidgetParam
    @WindowParam
    protected UUID reportId;

    @WidgetParam
    @WindowParam
    protected UUID templateId;

    @WidgetParam
    @WindowParam
    protected Boolean refreshAutomatically = false;

    @Subscribe
    public void onInit(InitEvent initEvent) {
        if (reportId != null) {
            Report report = dataManager.load(Report.class)
                    .id(reportId)
                    .fetchPlan("report.edit")
                    .optional().orElse(null);

            if (report != null) {
                List<ReportTemplate> chartTemplates = getChartsTemplates(report);
                ReportTemplate reportTemplate = null;
                if (templateId != null) {
                    reportTemplate = chartTemplates.stream()
                            .filter(t -> templateId.equals(t.getId()))
                            .findFirst()
                            .orElse(ReportOutputType.CHART == report.getDefaultTemplate().getReportOutputType() ? report.getDefaultTemplate() : null);
                }
                reportComboBox.setValue(report);
                templateComboBox.setOptionsList(chartTemplates);
                templateComboBox.setValue(reportTemplate);
            }
        }

        refreshAutomaticallyCheckbox.setValue(refreshAutomatically);
    }

    protected List<ReportTemplate> getChartsTemplates(Report report) {
        return report.getTemplates().stream()
                .filter(rt -> ReportOutputType.CHART == rt.getReportOutputType())
                .collect(Collectors.toList());
    }

    @Subscribe("reportComboBox")
    public void onReportComboBoxValueChange(HasValue.ValueChangeEvent<Report> event) {
        Report report = event.getValue();
        if (report != null) {
            List<ReportTemplate> chartTemplates = getChartsTemplates(report);
            templateComboBox.setOptionsList(chartTemplates);
            reportId = report.getId();
            if (ReportOutputType.CHART == report.getDefaultTemplate().getReportOutputType()) {
                templateComboBox.setValue(report.getDefaultTemplate());
                templateId = report.getDefaultTemplate().getId();
            }
        }
    }

    @Subscribe("templateComboBox")
    public void onTemplateComboBoxValueChange(HasValue.ValueChangeEvent<ReportTemplate> event) {
        ReportTemplate reportTemplate = event.getValue();
        if (reportTemplate != null) {
            templateId = reportTemplate.getId();
        }
    }

    @Subscribe("refreshAutomaticallyCheckbox")
    public void onRefreshAutomaticallyCheckboxValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        refreshAutomatically = event.getValue();
    }
}