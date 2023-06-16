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

package io.jmix.reportsflowui.view.history;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import io.jmix.core.LoadContext;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import io.jmix.reports.ReportSecurityManager;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroup;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ViewController("report_ReportExecutionDialog.view")
@ViewDescriptor("report-execution-dialog-view.xml")
@LookupComponent("reportsDataGrid")
public class ReportExecutionDialog extends StandardListView<Report> {

    @ViewComponent
    protected DataGrid<Report> reportsDataGrid;
    @ViewComponent
    protected TypedTextField<String> filterName;
    @ViewComponent
    protected TypedTextField<String> filterCode;
    @ViewComponent
    protected EntityComboBox<ReportGroup> filterGroup;
    @ViewComponent
    protected TypedDatePicker<Date> filterUpdatedDate;
    @ViewComponent
    protected CollectionContainer<Report> reportsDc;

    @Autowired
    protected ReportSecurityManager reportSecurityManager;
    @Autowired
    protected CurrentUserSubstitution currentUserSubstitution;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected MessageBundle messageBundle;

    protected MetaClass metaClassParameter;
    protected String screenParameter;

    @Subscribe
    public void onInit(InitEvent event) {
        reportsDataGrid.addColumn(report -> metadataTools.getInstanceName(report))
                .setHeader(messageBundle.getMessage("history.name.header"))
                .setKey("name")
                .setSortable(true)
                .setResizable(true);

        List<Grid.Column<Report>> columns = List.of(
                reportsDataGrid.getColumnByKey("group"),
                reportsDataGrid.getColumnByKey("name"),
                reportsDataGrid.getColumnByKey("description"),
                reportsDataGrid.getColumnByKey("code"),
                reportsDataGrid.getColumnByKey("updateTs")
        );
        reportsDataGrid.setColumnOrder(columns);
    }

    @Install(to = "reportsDl", target = Target.DATA_LOADER)
    protected List<Report> reportsDlLoadDelegate(LoadContext<Report> loadContext) {
        return reportSecurityManager.getAvailableReports(screenParameter,
                currentUserSubstitution.getEffectiveUser(),
                metaClassParameter);
    }

    @Subscribe("clearFilterBtn")
    protected void onClearFilterBtnClick(ClickEvent<Button> event) {
        filterName.clear();
        filterCode.clear();
        filterUpdatedDate.clear();
        filterGroup.clear();
        filterReports();
    }

    @Subscribe("applyFilterBtn")
    protected void onApplyFilterBtnClick(ClickEvent<Button> event) {
        filterReports();
    }

    protected void filterReports() {
        List<Report> reports = reportSecurityManager.getAvailableReports(screenParameter,
                        currentUserSubstitution.getEffectiveUser(),
                        metaClassParameter)
                .stream()
                .filter(this::filterReport)
                .collect(Collectors.toList());

        reportsDc.setItems(reports);
    }

    protected boolean filterReport(Report report) {
        String filterNameValue = StringUtils.lowerCase(filterName.getValue());
        String filterCodeValue = StringUtils.lowerCase(filterCode.getValue());
        ReportGroup groupFilterValue = filterGroup.getValue();
        Date dateFilterValue = filterUpdatedDate.getTypedValue();

        if (filterNameValue != null
                && !report.getName().toLowerCase().contains(filterNameValue)) {
            return false;
        }

        if (filterCodeValue != null) {
            if (report.getCode() == null
                    || (report.getCode() != null
                    && !report.getCode().toLowerCase().contains(filterCodeValue))) {
                return false;
            }
        }

        if (groupFilterValue != null && !Objects.equals(report.getGroup(), groupFilterValue)) {
            return false;
        }

        if (dateFilterValue != null
                && report.getUpdateTs() != null
                && !report.getUpdateTs().after(dateFilterValue)) {
            return false;
        }

        return true;
    }

    public void setMetaClassParameter(MetaClass metaClassParameter) {
        this.metaClassParameter = metaClassParameter;
    }

    public void setScreenParameter(String screenParameter) {
        this.screenParameter = screenParameter;
    }
}
