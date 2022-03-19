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

package io.jmix.reportsui.screen.report.history;

import io.jmix.core.LoadContext;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.reports.ReportSecurityManager;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.ui.WindowParam;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.LookupComponent;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@UiController("report_ReportExecution.dialog")
@UiDescriptor("report-execution-dialog.xml")
@LookupComponent("reportsTable")
public class ReportExecutionDialog extends StandardLookup<Report> {

    public static final String META_CLASS_PARAMETER = "metaClass";
    public static final String SCREEN_PARAMETER = "screen";

    @Autowired
    protected ReportSecurityManager reportSecurityManager;

    @Autowired
    protected CollectionContainer<Report> reportsDc;
    @Autowired
    protected Table<Report> reportsTable;

    @Autowired
    protected Button applyFilterBtn;
    @Autowired
    protected TextField<String> filterName;
    @Autowired
    protected TextField<String> filterCode;
    @Autowired
    protected EntityComboBox<ReportGroup> filterGroup;
    @Autowired
    protected DateField<Date> filterUpdatedDate;
    @Autowired
    protected CurrentUserSubstitution currentUserSubstitution;

    @Autowired
    protected MetadataTools metadataTools;

    @WindowParam(name = META_CLASS_PARAMETER)
    protected MetaClass metaClassParameter;
    @WindowParam(name = SCREEN_PARAMETER)
    protected String screenParameter;

    @Install(to = "reportsDl", target = Target.DATA_LOADER)
    protected List<Report> reportsDlLoadDelegate(LoadContext<Report> loadContext) {
        return reportSecurityManager.getAvailableReports(screenParameter,
                currentUserSubstitution.getEffectiveUser(),
                metaClassParameter);
    }

    @Subscribe("clearFilterBtn")
    protected void onClearFilterBtnClick(Button.ClickEvent event) {
        filterName.setValue(null);
        filterCode.setValue(null);
        filterUpdatedDate.setValue(null);
        filterGroup.setValue(null);
        filterReports();
    }

    @Subscribe("applyFilterBtn")
    protected void onApplyFilterBtnClick(Button.ClickEvent event) {
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

        Table.SortInfo sortInfo = reportsTable.getSortInfo();
        if (sortInfo != null) {
            Table.SortDirection direction = sortInfo.getAscending() ? Table.SortDirection.ASCENDING : Table.SortDirection.DESCENDING;
            reportsTable.sort(sortInfo.getPropertyId().toString(), direction);
        }
    }

    protected boolean filterReport(Report report) {
        String filterNameValue = StringUtils.lowerCase(filterName.getValue());
        String filterCodeValue = StringUtils.lowerCase(filterCode.getValue());
        ReportGroup groupFilterValue = filterGroup.getValue();
        Date dateFilterValue = filterUpdatedDate.getValue();

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

    @Install(to = "reportsTable.name", subject = "valueProvider")
    protected String reportsTableNameValueProvider(Report report) {
        return metadataTools.getInstanceName(report);
    }
}
