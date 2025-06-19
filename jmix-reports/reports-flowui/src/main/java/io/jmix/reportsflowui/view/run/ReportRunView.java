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

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.Route;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.Sort;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.reports.ReportFilter;
import io.jmix.reports.ReportLoadContext;
import io.jmix.reports.ReportRepository;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.exception.MissingDefaultTemplateException;
import io.jmix.reportsflowui.ReportsClientProperties;
import io.jmix.reportsflowui.helper.GridSortHelper;
import io.jmix.reportsflowui.runner.FluentUiReportRunner;
import io.jmix.reportsflowui.runner.ParametersDialogShowMode;
import io.jmix.reportsflowui.runner.UiReportRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Route(value = "reports/run", layout = DefaultMainViewParent.class)
@ViewController("report_ReportRunView")
@ViewDescriptor("report-run-view.xml")
@LookupComponent("reportDataGrid")
@DialogMode(width = "80em", resizable = true)
public class ReportRunView extends StandardListView<Report> {

    @ViewComponent
    protected DataGrid<Report> reportDataGrid;
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
    @ViewComponent
    protected CollectionLoader<Report> reportsDl;

    @Autowired
    protected ReportRepository reportRepository;
    @Autowired
    protected CurrentUserSubstitution currentUserSubstitution;
    @ViewComponent
    protected MessageBundle messageBundle;
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
    @Autowired
    protected GridSortHelper gridSortHelper;

    protected List<Report> reports;
    protected MetaClass metaClassParameter;
    protected String screenParameter;

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }

    public void setMetaClass(MetaClass metaClassParameter) {
        this.metaClassParameter = metaClassParameter;
    }

    public void setScreen(String screenParameter) {
        this.screenParameter = screenParameter;
    }

    @Subscribe
    public void onInit(final InitEvent event) {
        codeFilter.addTypedValueChangeListener(e -> onFilterFieldValueChange());
        nameFilter.addTypedValueChangeListener(e -> onFilterFieldValueChange());
        groupFilter.addValueChangeListener(e -> onFilterFieldValueChange());
        updatedDateFilter.addTypedValueChangeListener(e -> onFilterFieldValueChange());
    }

    @Install(to = "reportsDl", target = Target.DATA_LOADER)
    private List<Report> reportsDlLoadDelegate(LoadContext<Report> ignored) {
        if (reports != null) {
            return reports;
        }

        ReportFilter filter = new ReportFilter();
        // ui filters
        filter.setNameContains(nameFilter.getTypedValue());
        filter.setCodeContains(codeFilter.getTypedValue());
        filter.setGroup(groupFilter.getValue());
        filter.setUpdatedAfter(updatedDateFilter.getTypedValue());
        // access filters
        filter.setViewId(screenParameter);
        filter.setUser(currentUserSubstitution.getEffectiveUser());
        filter.setInputValueMetaClass(metaClassParameter);
        filter.setSystem(false);

        Sort sort = getReportGridSort();
        ReportLoadContext context = new ReportLoadContext(filter, sort);
        List<Report> items = reportRepository.loadList(context);
        return items;
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

    protected Sort getReportGridSort() {
        return gridSortHelper.convertSortOrders(
                reportDataGrid.getSortOrder(),
                Map.of("name", ReportLoadContext.LOCALIZED_NAME_SORT_KEY) // custom cell renderer
        );
    }

    protected void onFilterFieldValueChange() {
        reportsDl.load();
    }
}