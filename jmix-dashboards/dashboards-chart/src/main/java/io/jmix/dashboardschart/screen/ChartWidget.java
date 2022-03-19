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

import com.haulmont.yarg.reporting.ReportOutputDocument;
import io.jmix.charts.component.CustomChart;
import io.jmix.charts.model.chart.impl.AbstractChart;
import io.jmix.core.DataManager;
import io.jmix.dashboards.model.Widget;
import io.jmix.dashboardsui.annotation.DashboardWidget;
import io.jmix.dashboardsui.annotation.WidgetParam;
import io.jmix.dashboardsui.event.DashboardEvent;
import io.jmix.dashboardsui.repository.WidgetRepository;
import io.jmix.dashboardsui.widget.RefreshableWidget;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.runner.ReportRunner;
import io.jmix.ui.WindowParam;
import io.jmix.ui.component.Label;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@DashboardWidget(name = ChartWidget.CAPTION, editFragmentId = "dshbrd_ChartWidgetEdit")
@UiController("dshbrd_ChartWidget")
@UiDescriptor("chart-widget.xml")
public class ChartWidget extends ScreenFragment implements RefreshableWidget {
    public static final String CAPTION = "Chart";
    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected ReportRunner reportRunner;

    @Autowired
    protected WidgetRepository widgetRepository;

    @WindowParam
    protected Widget widget;

    @WidgetParam
    @WindowParam
    protected UUID reportId;

    @WidgetParam
    @WindowParam
    protected Boolean refreshAutomatically = false;

    @WidgetParam
    @WindowParam
    protected UUID templateId;

    @Autowired
    protected Label<String> errorLabel;

    @Autowired
    protected CustomChart reportJsonChart;
    private Report report;
    private ReportTemplate reportTemplate;

    @Subscribe
    public void onInit(InitEvent initEvent) {
        report = dataManager.load(Report.class)
                .id(reportId)
                .fetchPlan("report.edit")
                .optional()
                .orElse(null);

        reportTemplate = null;
        if (report != null) {
            List<ReportTemplate> chartTemplates = report.getTemplates().stream()
                    .filter(rt -> ReportOutputType.CHART == rt.getReportOutputType())
                    .collect(Collectors.toList());
            if (templateId != null) {
                reportTemplate = chartTemplates.stream()
                        .filter(t -> templateId.equals(t.getId()))
                        .findFirst()
                        .orElse(ReportOutputType.CHART == report.getDefaultTemplate().getReportOutputType() ?
                                report.getDefaultTemplate() : null);
            }

        }
        updateChart();
    }

    private void updateChart() {
        if (report == null || reportTemplate == null) {
            errorLabel.setVisible(true);
            reportJsonChart.setVisible(false);
            return;
        }

        Map<String, Object> widgetParams = widgetRepository.getWidgetParams(widget);
        ReportOutputDocument document = reportRunner.byReportEntity(report)
                .withParams(widgetParams)
                .withTemplate(reportTemplate)
                .run();

        if (document.getContent() != null) {
            reportJsonChart.setVisible(true);
            reportJsonChart.setSizeFull();
            reportJsonChart.setConfiguration(new BasicChart());
            reportJsonChart.setNativeJson(new String(document.getContent(), StandardCharsets.UTF_8));
        } else {
            errorLabel.setVisible(true);
            reportJsonChart.setVisible(false);
        }
    }

    @Override
    public void refresh(DashboardEvent dashboardEvent) {
        if (refreshAutomatically) {
            updateChart();
        }
    }

    public CustomChart getReportChart() {
        return reportJsonChart;
    }

    /**
     * Used for default initialization in
     * ChartImpl.JmixAmchartsSceneExt#setupDefaults(AbstractChart)
     */
    protected static class BasicChart extends AbstractChart<BasicChart> {
    }

}