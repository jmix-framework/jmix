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


import io.jmix.core.FileRef;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportExecution;
import io.jmix.ui.WindowParam;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Table;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.screen.*;
import io.jmix.ui.model.CollectionLoader;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@UiController("report_ReportExecution.browse")
@UiDescriptor("report-execution-browse.xml")
public class ReportExecutionBrowser extends StandardLookup<ReportExecution> {

    public static final String REPORTS_PARAMETER = "reports";

    @Autowired
    protected CollectionLoader<ReportExecution> executionsDl;
    @Autowired
    protected Table<ReportExecution> executionsTable;
    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected Downloader downloader;
    @Autowired
    protected SecondsToTextFormatter durationFormatter;

    @WindowParam(name = REPORTS_PARAMETER)
    protected List<Report> filterByReports;

    @Subscribe
    protected void onInit(InitEvent event) {
        executionsTable.addAction(new DownloadDocumentAction());
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        initDataLoader();

        if (CollectionUtils.isNotEmpty(filterByReports)) {
            String caption = messageBundle.formatMessage("report.executionHistory.byReport", getReportsNames());
            getWindow().setCaption(caption);
        }
    }

    protected void initDataLoader() {
        StringBuilder query = new StringBuilder("select e from report_ReportExecution e");

        if (!CollectionUtils.isEmpty(filterByReports)) {
            query.append(" where e.report.id in :reportIds");
            executionsDl.setParameter("reportIds", filterByReports);
        }
        query.append(" order by e.startTime desc");

        executionsDl.setQuery(query.toString());
        executionsDl.load();
    }

    @Install(to = "executionsTable.executionTimeSec", subject = "formatter")
    protected String formatExecutionTimeSec(Long value) {
        return durationFormatter.apply(value);
    }

    protected String getReportsNames() {
        if (CollectionUtils.isEmpty(filterByReports)) {
            return "";
        }

        return filterByReports.stream()
                .map(Report::getName)
                .collect(Collectors.joining(", "));
    }

    @Install(to = "executionsTable.outputDocument", subject = "valueProvider")
    protected String executionsTableOutputDocumentValueProvider(ReportExecution reportExecution) {
        return reportExecution.getOutputDocument() != null ? reportExecution.getOutputDocument().getFileName() : "";
    }

    public class DownloadDocumentAction extends BaseAction {
        public DownloadDocumentAction() {
            super("download");
        }

        @Override
        protected boolean isApplicable() {
            if (executionsTable.getSelected().size() != 1) {
                return false;
            }
            ReportExecution execution = executionsTable.getSingleSelected();
            return execution != null && execution.getOutputDocument() != null;
        }

        @Override
        public String getCaption() {
            return messageBundle.getMessage("report.executionHistory.download");
        }

        @Override
        public String getIcon() {
            return JmixIcon.DOWNLOAD.source();
        }

        @Override
        public void actionPerform(Component component) {
            ReportExecution execution = executionsTable.getSingleSelected();
            if (execution != null && execution.getOutputDocument() != null) {
                FileRef fileRef = execution.getOutputDocument();
                downloader.download(fileRef);
            }
        }
    }
}
