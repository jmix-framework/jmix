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

package io.jmix.aitoolsflowui.gridexport.view;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.data.provider.Query;
import io.jmix.aitoolsflowui.AiToolsGridExportProperties;
import io.jmix.aitoolsflowui.gridexport.AiGridDataContext;
import io.jmix.aitoolsflowui.gridexport.AiGridDataSupport;
import io.jmix.aitoolsflowui.gridexport.AiGridDataView;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.service.AiChatMessageService;
import io.jmix.aitoolsflowui.service.AiConversationService;
import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardOutcome;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.gridexportflowui.exporter.ExportMode;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * INTERNAL.
 * <p>
 * The "Ask AI about the data" dialog: the user chooses which rows of the grid to send and the conversation to send
 * them to, and the AI chat opens with the rows waiting for the user's question.
 * <p>
 * An application replaces it by declaring its own view with the same id that implements {@link AiGridDataView}, not
 * by extending this class.
 */
@Internal
@ViewController(id = "aitls_AiGridDataDialog")
@ViewDescriptor("ai-grid-data-dialog.xml")
@DialogMode(width = "36em")
public class AiGridDataDialog extends StandardView implements AiGridDataView {

    @Autowired
    protected AiGridDataSupport gridDataSupport;
    @Autowired
    protected AiToolsGridExportProperties gridExportProperties;
    @Autowired
    protected AiConversationService conversationService;
    @Autowired
    protected AiChatMessageService messageService;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected Notifications notifications;

    @ViewComponent
    protected JmixRadioButtonGroup<ExportMode> rowsField;
    @ViewComponent
    protected JmixComboBox<AiConversation> conversationField;
    @ViewComponent
    protected MessageBundle messageBundle;

    @Nullable
    protected AiGridDataContext gridDataContext;
    protected List<AiConversation> conversations = List.of();

    @Override
    public void setGridDataContext(AiGridDataContext context) {
        this.gridDataContext = context;
    }

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        initRowsField(getGridDataContext());
        initConversationField();
    }

    @Subscribe(id = "openChatBtn", subject = "clickListener")
    public void onOpenChatBtnClick(final ClickEvent<JmixButton> event) {
        AiGridDataContext context = getGridDataContext();
        ExportMode mode = rowsField.getValue();
        int rowCount = gridDataSupport.countRows(context, mode);
        int maxRows = gridExportProperties.getMaxRows();
        if (rowCount > maxRows) {
            notifications.create(messageBundle.formatMessage("aiGridDataDialog.tooManyRows", rowCount, maxRows))
                    .withType(Notifications.Type.WARNING)
                    .show();
            return;
        }

        AiConversation conversation = conversationField.getValue();
        close(StandardOutcome.CLOSE)
                .then(() -> gridDataSupport.openChat(context, mode, conversation, List.of()));
    }

    @Subscribe(id = "cancelBtn", subject = "clickListener")
    public void onCancelBtnClick(final ClickEvent<JmixButton> event) {
        close(StandardOutcome.CLOSE);
    }

    protected AiGridDataContext getGridDataContext() {
        if (gridDataContext == null) {
            throw new IllegalStateException(getClass().getSimpleName() + " is opened without the grid to ask about");
        }
        return gridDataContext;
    }

    /**
     * Offers the selected rows, when there are any, and the current page; without a selection the current page is
     * the only choice and is shown read-only.
     *
     * @param context the grid
     */
    protected void initRowsField(AiGridDataContext context) {
        int selectedCount = gridDataSupport.countRows(context, ExportMode.SELECTED_ROWS);
        int pageCount = gridDataSupport.countRows(context, ExportMode.CURRENT_PAGE);

        List<ExportMode> modes = selectedCount > 0
                ? List.of(ExportMode.SELECTED_ROWS, ExportMode.CURRENT_PAGE)
                : List.of(ExportMode.CURRENT_PAGE);
        rowsField.setItems(modes);
        rowsField.setItemLabelGenerator(mode -> mode == ExportMode.SELECTED_ROWS
                ? messageBundle.formatMessage("aiGridDataDialog.rows.selected", selectedCount)
                : messageBundle.formatMessage("aiGridDataDialog.rows.currentPage", pageCount));
        rowsField.setValue(modes.get(0));
        rowsField.setReadOnly(modes.size() == 1);
    }

    /**
     * Lets the user search the conversations by title, the most recently active first. An empty value means a new
     * conversation.
     */
    protected void initConversationField() {
        conversations = orderByActivity(conversationService.loadConversations());
        conversationField.setItemLabelGenerator(metadataTools::getInstanceName);
        conversationField.setItemsFetchCallback(this::fetchConversations);
    }

    protected Stream<AiConversation> fetchConversations(Query<AiConversation, String> query) {
        String filter = query.getFilter().orElse("").trim().toLowerCase(Locale.ROOT);
        return conversations.stream()
                .filter(conversation -> filter.isEmpty()
                        || metadataTools.getInstanceName(conversation).toLowerCase(Locale.ROOT).contains(filter))
                .skip(query.getOffset())
                .limit(query.getLimit());
    }

    /**
     * Orders the conversations as the chat hub does: by the date of the latest message, or by the creation date of
     * a conversation without messages.
     *
     * @param loaded conversations to order
     * @return the conversations, the most recently active first
     */
    protected List<AiConversation> orderByActivity(List<AiConversation> loaded) {
        Map<AiConversation, AiChatMessage> latestMessages = messageService.loadLatestMessages(loaded, null);
        List<AiConversation> ordered = new ArrayList<>(loaded);
        ordered.sort(Comparator.comparing((AiConversation conversation) -> {
            AiChatMessage latestMessage = latestMessages.get(conversation);
            OffsetDateTime messageDate = latestMessage != null ? latestMessage.getCreatedDate() : null;
            return messageDate != null ? messageDate : conversation.getCreatedDate();
        }, Comparator.nullsLast(Comparator.reverseOrder())));
        return ordered;
    }
}
