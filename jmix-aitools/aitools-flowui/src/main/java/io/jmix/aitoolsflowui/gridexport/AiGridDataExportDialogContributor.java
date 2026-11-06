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

package io.jmix.aitoolsflowui.gridexport;

import com.vaadin.flow.component.Component;
import io.jmix.aitoolsflowui.gridexport.view.AiGridDataDialog;
import io.jmix.aitoolsflowui.service.AiChatService;
import io.jmix.aitoolsflowui.view.chat.AiChatView;
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.core.annotation.Experimental;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.accesscontext.UiShowViewContext;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import io.jmix.flowui.view.View;
import io.jmix.gridexportflowui.action.ExportDialogActionContributor;
import io.jmix.gridexportflowui.action.ExportDialogContext;
import io.jmix.gridexportflowui.exporter.AbstractDataGridExporter;
import io.jmix.gridexportflowui.exporter.ExportMode;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Adds "Ask AI" to the grid export dialog. It opens the {@link AiGridDataDialog "Ask AI about the data"} dialog, or
 * the view that replaces it by id, for the rows read the way the export reads them.
 * <p>
 * Not a component by itself, as it names the optional grid export add-on's types; it is registered by the
 * auto-configuration that is active only when that add-on is present.
 */
@Experimental
@NullMarked
public class AiGridDataExportDialogContributor implements ExportDialogActionContributor {

    public static final String ACTION_ID = "aitls_askAiAboutData";

    private static final Logger log = LoggerFactory.getLogger(AiGridDataExportDialogContributor.class);
    private static final String MESSAGE_GROUP = "io.jmix.aitoolsflowui.gridexport";

    @Autowired
    protected AiGridDataSupport gridDataSupport;
    @Autowired
    protected AiChatService chatService;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected DialogWindows dialogWindows;
    @Autowired
    protected Messages messages;

    /**
     * Returns "Ask AI" unless it cannot be used for this export: the exporter cannot read rows, the chat is
     * unavailable, the user may not open the chat or the dialog, or the export reads no column that may be sent to
     * the AI. The action is disabled while the grid is empty.
     *
     * @param exportContext the export the dialog is opened for
     * @return the action, or an empty list
     */
    @Override
    public List<Action> getActions(ExportDialogContext exportContext) {
        if (!(exportContext.getExporter() instanceof AbstractDataGridExporter<?> exporter)) {
            return List.of();
        }

        AiGridDataContext context = new AiGridDataContext(exportContext.getTarget(), exporter,
                exportContext.getColumnFilter());
        if (!isAvailable(context)) {
            return List.of();
        }
        return List.of(createAction(context));
    }

    protected boolean isAvailable(AiGridDataContext context) {
        return chatService.isAvailable()
                && isViewPermitted(AiChatView.class)
                && isViewPermitted(AiGridDataDialog.class)
                && gridDataSupport.hasAllowedColumns(context);
    }

    protected boolean isViewPermitted(Class<? extends View<?>> viewClass) {
        UiShowViewContext showViewContext = new UiShowViewContext(ViewDescriptorUtils.getInferredViewId(viewClass));
        accessManager.applyRegisteredConstraints(showViewContext);
        return showViewContext.isPermitted();
    }

    protected Action createAction(AiGridDataContext context) {
        Action action = new SecuredBaseAction<>(ACTION_ID)
                .withText(messages.getMessage(MESSAGE_GROUP, "aiGridDataExportDialogContributor.askAi"))
                .withHandler(event -> openDialog(context));
        action.setEnabled(gridDataSupport.countRows(context, ExportMode.CURRENT_PAGE) > 0);
        return action;
    }

    protected void openDialog(AiGridDataContext context) {
        View<?> origin = UiComponentUtils.getView((Component) context.getTarget());
        // By id, not by class: an application may replace the dialog with a view of its own class.
        String dialogId = ViewDescriptorUtils.getInferredViewId(AiGridDataDialog.class);
        dialogWindows.view(origin, dialogId)
                .withViewConfigurer(view -> {
                    if (view instanceof AiGridDataView gridDataView) {
                        gridDataView.setGridDataContext(context);
                    } else {
                        log.warn("View {} does not implement {}, so it gets no grid to ask the AI about",
                                dialogId, AiGridDataView.class.getSimpleName());
                    }
                })
                .open();
    }
}
