/*
 * Copyright 2026 Haulmont.
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

package io.jmix.reportsflowui.runner;

import io.jmix.core.JmixOrder;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.OpenMode;
import io.jmix.reports.entity.JmixReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import io.jmix.reportsflowui.ReportsClientProperties;
import io.jmix.reportsflowui.view.run.ReportTableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

/**
 * {@link ReportResultHandler} that opens report output with the {@code table} output type
 * in a table viewer (either dialog or via navigation, depending on configuration).
 */
@Internal
@Order(JmixOrder.HIGHEST_PRECEDENCE)
@Component("report_TableReportResultHandler")
public class TableReportResultHandler implements ReportResultHandler {

    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected ReportsClientProperties reportsClientProperties;

    /**
     * Handles the report output document by opening it in the table viewer.
     * <p>
     * Returns {@code true} if the document was handled (output type is {@code table}),
     * {@code false} to pass handling to the next handler in the chain.
     */
    @Override
    public boolean handle(ReportOutputDocument document, UiReportRunContext context) {
        if (!JmixReportOutputType.table.getId().equals(document.getReportOutputType().getId())) {
            return false;
        }

        ReportTemplate reportTemplate = context.getReportTemplate();
        Map<String, Object> params = context.getParams() != null ? context.getParams() : Collections.emptyMap();
        String templateCode = reportTemplate != null ? reportTemplate.getCode() : null;

        Consumer<ReportTableView> configurer = view -> {
            view.setReportOutputDocument(document);
            view.setTemplateCode(templateCode);
            view.setReportParameters(params);
        };

        if (reportsClientProperties.getTableOutputOpenMode() == OpenMode.NAVIGATION) {
            viewNavigators.view(context.getOwner(), ReportTableView.class)
                    .withAfterNavigationHandler(event -> {
                        ReportTableView view = event.getView();
                        configurer.accept(view);
                        view.loadReportContent();
                    })
                    .navigate();
        } else {
            DialogWindow<ReportTableView> dialogWindow = dialogWindows.view(context.getOwner(), ReportTableView.class)
                    .build();
            configurer.accept(dialogWindow.getView());
            dialogWindow.open();
        }
        return true;
    }
}
