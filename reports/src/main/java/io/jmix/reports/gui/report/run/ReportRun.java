/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.reports.gui.report.run;

import com.sun.deploy.config.ClientConfig;
import io.jmix.core.metamodel.model.MetaClass;
import com.haulmont.cuba.core.global.UserSessionSource;
import io.jmix.ui.WindowParam;
import com.haulmont.cuba.gui.components.*;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.action.ItemTrackingAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import io.jmix.reports.app.service.ReportService;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.gui.ReportGuiManager;
import io.jmix.ui.component.GridLayout;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;
import java.util.stream.Collectors;

public class ReportRun extends AbstractLookup {

    protected static final String RUN_ACTION_ID = "runReport";
    public static final String META_CLASS_PARAMETER = "metaClass";
    public static final String REPORTS_PARAMETER = "reports";
    public static final String SCREEN_PARAMETER = "screen";

    @Autowired
    protected Table<Report> reportsTable;

    @Autowired
    protected ReportGuiManager reportGuiManager;

    @Autowired
    protected CollectionDatasource<Report, UUID> reportDs;

    @Autowired
    protected UserSessionSource userSessionSource;

    @Autowired
    protected TextField<String> nameFilter;

    @Autowired
    protected TextField<String> codeFilter;

    @Autowired
    protected LookupField<ReportGroup> groupFilter;

    @Autowired
    protected DateField<Date> updatedDateFilter;

    @Autowired
    protected GridLayout gridFilter;

    @Autowired
    protected ClientConfig clientConfig;

    @WindowParam(name = REPORTS_PARAMETER)
    protected List<Report> reportsParameter;

    @WindowParam(name = META_CLASS_PARAMETER)
    protected MetaClass metaClassParameter;
    @WindowParam(name = SCREEN_PARAMETER)
    protected String screenParameter;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        List<Report> reports = reportsParameter;
        if (reports == null) {
            reports = reportGuiManager.getAvailableReports(screenParameter, userSessionSource.getUserSession().getUser(),
                    metaClassParameter);
        }

        if (reportsParameter != null) {
            gridFilter.setVisible(false);
        }

        for (Report report : reports) {
            reportDs.includeItem(report);
        }

        Action runAction = new ItemTrackingAction(RUN_ACTION_ID)
                .withCaption(getMessage("runReport"))
                .withHandler(e -> {
                    Report report = reportsTable.getSingleSelected();
                    if (report != null) {
                        report = getDsContext().getDataSupplier().reload(report, ReportService.MAIN_VIEW_NAME);
                        reportGuiManager.runReport(report, ReportRun.this);
                    }
                });

        reportsTable.addAction(runAction);
        reportsTable.setItemClickAction(runAction);

        addAction(new BaseAction("applyFilter")
                //TODO filter apply shortcut
//                .withShortcut(clientConfig.getFilterApplyShortcut())
                .withHandler(e -> {
                    filterReports();
                }));
    }

    public void filterReports() {
        String nameFilterValue = StringUtils.lowerCase(nameFilter.getValue());
        String codeFilterValue = StringUtils.lowerCase(codeFilter.getValue());
        ReportGroup groupFilterValue = groupFilter.getValue();
        Date dateFilterValue = updatedDateFilter.getValue();

        List<Report> reports =
                reportGuiManager.getAvailableReports(screenParameter, userSessionSource.getUserSession().getUser(),
                        metaClassParameter)
                        .stream()
                        .filter(report -> {
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

                            if (groupFilterValue != null && !Objects.equals(report.getGroup(), groupFilterValue)) {
                                return false;
                            }

                            if (dateFilterValue != null
                                    && report.getUpdateTs() != null
                                    && !report.getUpdateTs().after(dateFilterValue)) {
                                return false;
                            }

                            return true;
                        })
                        .collect(Collectors.toList());

        reportDs.clear();
        for (Report report : reports) {
            reportDs.includeItem(report);
        }

        Table.SortInfo sortInfo = reportsTable.getSortInfo();
        if (sortInfo != null) {
            Table.SortDirection direction = sortInfo.getAscending() ? Table.SortDirection.ASCENDING : Table.SortDirection.DESCENDING;
            reportsTable.sort(sortInfo.getPropertyId().toString(), direction);
        }
    }

    public void clearFilter() {
        nameFilter.setValue(null);
        codeFilter.setValue(null);
        updatedDateFilter.setValue(null);
        groupFilter.setValue(null);
        filterReports();
    }
}