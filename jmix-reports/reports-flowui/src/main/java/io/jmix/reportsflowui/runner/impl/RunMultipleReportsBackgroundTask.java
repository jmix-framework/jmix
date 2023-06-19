/*
 * Copyright 2022 Haulmont.
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

package io.jmix.reportsflowui.runner.impl;

import io.jmix.flowui.backgroundtask.BackgroundTask;
import io.jmix.flowui.backgroundtask.TaskLifeCycle;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.view.View;
import io.jmix.reports.entity.Report;
import io.jmix.reports.runner.ReportRunContext;
import io.jmix.reports.runner.ReportRunner;
import io.jmix.reports.util.ReportZipUtils;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import io.jmix.reportsflowui.runner.UiReportRunContext;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class RunMultipleReportsBackgroundTask extends BackgroundTask<Integer, List<ReportOutputDocument>> {

    protected ReportRunner reportRunner;
    protected ReportZipUtils reportZipUtils;
    protected Downloader downloader;

    protected final UiReportRunContext context;
    protected final Report targetReport;
    protected final String multiParamAlias;
    protected final Collection<?> multiParamValue;

    public RunMultipleReportsBackgroundTask(UiReportRunContext context, long timeout, View<?> view, Report targetReport,
                                            String multiParamAlias, Collection<?> multiParamValue) {
        super(timeout, TimeUnit.MILLISECONDS, view);

        this.multiParamAlias = multiParamAlias;
        this.multiParamValue = multiParamValue;
        this.targetReport = targetReport;
        this.context = context;
    }

    @Autowired
    public void setReportZipUtils(ReportZipUtils reportZipUtils) {
        this.reportZipUtils = reportZipUtils;
    }

    @Autowired
    public void setDownloader(Downloader downloader) {
        this.downloader = downloader;
    }

    @Autowired
    public void setReportRunner(ReportRunner reportRunner) {
        this.reportRunner = reportRunner;
    }

    @Override
    public List<ReportOutputDocument> run(TaskLifeCycle<Integer> taskLifeCycle) {
        context.setReport(targetReport);
        return multiRunSync(context, multiParamAlias, multiParamValue);
    }

    @Override
    public void done(List<ReportOutputDocument> result) {
        downloadZipArchive(result);
    }

    protected void downloadZipArchive(List<ReportOutputDocument> outputDocuments) {
        byte[] zipArchiveContent = reportZipUtils.createZipArchive(outputDocuments);
        downloader.download(zipArchiveContent, "Reports.zip", DownloadFormat.ZIP);
    }

    protected List<ReportOutputDocument> multiRunSync(UiReportRunContext uiReportRunContext, String multiParamName,
                                                      Collection<?> multiParamValues) {
        List<ReportOutputDocument> outputDocuments = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(multiParamValues)) {
            multiParamValues.forEach(paramValue -> {
                Map<String, Object> map = new HashMap<>();
                map.put(multiParamName, paramValue);
                if (CollectionUtils.isNotEmpty(multiParamValues)) {
                    map.putAll(uiReportRunContext.getParams());
                }

                ReportRunContext reportRunContext = new ReportRunContext(uiReportRunContext.getReport())
                        .setReportTemplate(uiReportRunContext.getReportTemplate())
                        .setParams(map)
                        .setOutputType(uiReportRunContext.getOutputType())
                        .setOutputNamePattern(uiReportRunContext.getOutputNamePattern());

                ReportOutputDocument reportOutputDocument = reportRunner.run(reportRunContext);
                outputDocuments.add(reportOutputDocument);
            });
        }
        return outputDocuments;
    }
}
