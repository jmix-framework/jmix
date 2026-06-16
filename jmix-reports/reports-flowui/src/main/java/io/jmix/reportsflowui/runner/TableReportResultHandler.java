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

import com.vaadin.flow.component.UI;
import io.jmix.core.JmixOrder;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.OpenedDialogWindows;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.OpenMode;
import io.jmix.flowui.view.View;
import io.jmix.reports.entity.JmixReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import io.jmix.reportsflowui.ReportsClientProperties;
import io.jmix.reportsflowui.view.run.ReportTableView;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * {@link ReportResultHandler} that opens report output with the {@code table} output type
 * in a table viewer (either dialog or via navigation, depending on configuration).
 */
@Internal
@NullMarked
@Order(JmixOrder.HIGHEST_PRECEDENCE)
@Component("report_TableReportResultHandler")
public class TableReportResultHandler implements ReportResultHandler {

    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected ViewNavigators viewNavigators;
    @Autowired
    protected ReportsClientProperties reportsClientProperties;
    @Autowired
    protected ObjectProvider<OpenedDialogWindows> openedDialogWindowsProvider;

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
            closeOpenedDialogs(context.getOwner());
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

    /**
     * Closes any dialog views currently opened in the owner's UI before navigating to the table view,
     * so that the navigation target is not hidden behind leftover modal layers (e.g. the input parameters dialog).
     */
    protected void closeOpenedDialogs(View<?> owner) {
        UI ui = owner.getUI().orElseGet(UI::getCurrent);
        if (ui == null) {
            return;
        }
        // Snapshot — closing each view removes it from the underlying list.
        List.copyOf(openedDialogWindowsProvider.getObject().getDialogs(ui))
                .forEach(View::closeWithDefaultAction);
    }
}
