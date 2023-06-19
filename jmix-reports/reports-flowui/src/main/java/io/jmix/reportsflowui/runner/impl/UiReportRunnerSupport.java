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

import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.reports.entity.JmixReportOutputType;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import io.jmix.reportsflowui.runner.UiReportRunContext;
import io.jmix.reportsflowui.view.run.ReportTableView;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component("report_UiReportRunnerSupport")
public class UiReportRunnerSupport {

    protected final DialogWindows dialogWindows;
    protected final Downloader downloader;

    public UiReportRunnerSupport(DialogWindows dialogWindows, Downloader downloader) {
        this.dialogWindows = dialogWindows;
        this.downloader = downloader;
    }

    protected void showResult(ReportOutputDocument document, UiReportRunContext context) {
        String templateCode = getTemplateCode(context);
        Map<String, Object> params = context.getParams();
        ReportOutputType outputType = context.getOutputType();

        if (document.getReportOutputType().getId().equals(JmixReportOutputType.table.getId())) {
            DialogWindow<ReportTableView> showReportTableViewDialogWindow = dialogWindows.view(context.getOwner(),
                            ReportTableView.class)
                    .build();

            ReportTableView reportTableView = showReportTableViewDialogWindow.getView();
            reportTableView.setTableData(document.getContent());
            reportTableView.setReport((Report) document.getReport());
            reportTableView.setTemplateCode(templateCode);
            reportTableView.setReportParameters(params);
            showReportTableViewDialogWindow.open();
        } else {
            byte[] byteArr = document.getContent();
            io.jmix.reports.yarg.structure.ReportOutputType finalOutputType =
                    (outputType != null) ? outputType.getOutputType() : document.getReportOutputType();

            DownloadFormat exportFormat = DownloadFormat.getByExtension(finalOutputType.getId());
            String outputFileName = context.getOutputNamePattern();
            String documentName = isNotBlank(outputFileName) ? outputFileName : document.getDocumentName();

            downloader.download(byteArr, documentName, exportFormat);
        }
    }

    @Nullable
    protected String getTemplateCode(UiReportRunContext context) {
        ReportTemplate reportTemplate = context.getReportTemplate();
        return reportTemplate != null ? reportTemplate.getCode() : null;
    }
}
