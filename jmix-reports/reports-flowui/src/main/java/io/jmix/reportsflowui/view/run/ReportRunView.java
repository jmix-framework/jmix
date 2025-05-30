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

package io.jmix.reportsflowui.view.run;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import io.jmix.reports.ReportRepository;
import io.jmix.reports.ReportSecurityManager;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.entity.ReportSource;
import io.jmix.reports.exception.MissingDefaultTemplateException;
import io.jmix.reportsflowui.ReportsClientProperties;
import io.jmix.reportsflowui.runner.FluentUiReportRunner;
import io.jmix.reportsflowui.runner.ParametersDialogShowMode;
import io.jmix.reportsflowui.runner.UiReportRunner;
import org.springframework.lang.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Route(value = "reports/run", layout = DefaultMainViewParent.class)
@ViewController("report_ReportRunView")
@ViewDescriptor("report-run-view.xml")
@LookupComponent("reportDataGrid")
@DialogMode(width = "80em", resizable = true)
public class ReportRunView extends StandardListView<Report> {

    @ViewComponent
    protected DataGrid<Report> reportDataGrid;
    @ViewComponent
    protected CollectionContainer<Report> reportsDc;
    @ViewComponent
    protected TypedTextField<String> nameFilter;
    @ViewComponent
    protected TypedTextField<String> codeFilter;
    @ViewComponent
    protected EntityComboBox<ReportGroup> groupFilter;
    @ViewComponent
    protected TypedDatePicker<Date> updatedDateFilter;
    @ViewComponent
    protected FormLayout filterPanel;

    @Autowired
    protected ReportSecurityManager reportSecurityManager;
    @Autowired
    protected CurrentUserSubstitution currentUserSubstitution;
    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected UiReportRunner uiReportRunner;
    @Autowired
    protected ReportsClientProperties reportsClientProperties;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Messages messages;

    protected List<Report> reports;
    protected MetaClass metaClassParameter;
    protected String screenParameter;
    @Autowired
    private ReportRepository reportRepository;

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }

    public void setMetaClass(MetaClass metaClassParameter) {
        this.metaClassParameter = metaClassParameter;
    }

    public void setScreen(String screenParameter) {
        this.screenParameter = screenParameter;
    }

    @Install(to = "reportsDl", target = Target.DATA_LOADER)
    private List<Report> reportsDlLoadDelegate(LoadContext<Report> loadContext) {
        //Pass current grid sort to support settings facet
        // todo filter, sort etc.
        List<Report> items = reportRepository.getAllReports().stream().toList();
        return items;
        // return reportSecurityManager.getAvailableReports(screenParameter, currentUserSubstitution.getEffectiveUser(),
        //         metaClassParameter, getReportGridSort());
    }

    @Supply(to = "reportDataGrid.name", subject = "renderer")
    private Renderer<Report> nameCellRenderer() {
        return new TextRenderer<>(report ->
                metadataTools.getInstanceName(report));
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        if (this.reports != null) {
            filterPanel.setVisible(false);
        }
    }

    @Subscribe("reportDataGrid.runReport")
    protected void onReportDataGridRunReport(ActionPerformedEvent event) {
        Report report = reportDataGrid.getSingleSelectedItem();
        if (report == null) {
            return;
        }

        // todo better logic
        if (report.getSource() == ReportSource.DATABASE) {
            report = dataManager.load(Id.of(report))
                    .fetchPlan("report.edit")
                    .one();
        }
        FluentUiReportRunner fluentRunner = uiReportRunner.byReportEntity(report)
                .withParametersDialogShowMode(ParametersDialogShowMode.IF_REQUIRED);
        try {
            if (reportsClientProperties.getUseBackgroundReportProcessing()) {
                fluentRunner.inBackground(ReportRunView.this);
            }
            fluentRunner.runAndShow();
        } catch (MissingDefaultTemplateException e) {
            notifications.create(
                            messages.getMessage("runningReportError.title"),
                            messages.getMessage("missingDefaultTemplateError.description"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        }
    }

    @Subscribe("searchBtn")
    protected void onFilterReports(ClickEvent<JmixButton> event) {
        filterReports();
    }

    @Subscribe("clearBtn")
    protected void onClearFilter(ClickEvent<JmixButton> event) {
        nameFilter.clear();
        codeFilter.clear();
        updatedDateFilter.clear();
        groupFilter.clear();

        filterReports();
    }

    protected void filterReports() {
        String nameFilterValue = StringUtils.lowerCase(nameFilter.getTypedValue());
        String codeFilterValue = StringUtils.lowerCase(codeFilter.getTypedValue());
        ReportGroup groupFilterValue = groupFilter.getValue();
        Date dateFilterValue = updatedDateFilter.getTypedValue();

        // todo proper logic
        //List<Report> reports = reportSecurityManager.getAvailableReports(screenParameter,
        //                currentUserSubstitution.getEffectiveUser(), metaClassParameter, getReportGridSort())
        List<Report> reports = reportRepository.getAllReports()
                .stream()
                .filter(report ->
                        isCandidateReport(nameFilterValue, codeFilterValue, groupFilterValue, dateFilterValue, report)
                )
                .toList();

        reportsDc.setItems(reports);
    }

    @Nullable
    protected Sort getReportGridSort() {
        if (reportDataGrid.getSortOrder().isEmpty()) {
            return null;
        }

        GridSortOrder<Report> reportGridSortOrder = reportDataGrid.getSortOrder().get(0);
        return Sort.by(SortDirection.ASCENDING == reportGridSortOrder.getDirection()
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                reportGridSortOrder.getSorted().getKey());
    }

    protected boolean isCandidateReport(@Nullable String nameFilterValue, @Nullable String codeFilterValue,
                                        @Nullable ReportGroup groupFilterValue, @Nullable Date dateFilterValue,
                                        Report report) {
        if (nameFilterValue != null
                && !report.getName().toLowerCase().contains(nameFilterValue)) {
            return false;
        }

        if (codeFilterValue != null) {
            if (report.getCode() == null
                    || (report.getCode() != null
                    && !report.getCode().toLowerCase().contains(codeFilterValue))) {
                return false;
            }
        }

        if (groupFilterValue != null
                && !Objects.equals(report.getGroup(), groupFilterValue)) {
            return false;
        }

        if (dateFilterValue != null
                && report.getUpdateTs() != null
                && !report.getUpdateTs().after(dateFilterValue)) {
            return false;
        }

        return true;
    }
}