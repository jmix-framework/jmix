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

package grid_data;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import io.jmix.aitoolsflowui.fragment.chat.AiChatFragment;
import io.jmix.aitoolsflowui.gridexport.AiGridDataContext;
import io.jmix.aitoolsflowui.gridexport.AiGridDataExportDialogContributor;
import io.jmix.aitoolsflowui.gridexport.AiGridDataSupport;
import io.jmix.aitoolsflowui.gridexport.view.AiGridDataDialog;
import io.jmix.aitoolsflowui.model.AiChatDataBlock;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.aitoolsflowui.view.chat.AiChatView;
import io.jmix.core.AccessManager;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.flowui.accesscontext.UiShowViewContext;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.testassist.dialog.DialogInfo;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.gridexportflowui.action.ExportAction;
import io.jmix.gridexportflowui.action.ExportDialogContext;
import io.jmix.gridexportflowui.exporter.AbstractDataGridExporter;
import io.jmix.gridexportflowui.exporter.DataGridExporter;
import io.jmix.gridexportflowui.exporter.ExportMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import test_support.AiGridDataTestConfiguration;
import test_support.AiToolsFlowuiTestConfiguration;
import test_support.TestAiChatMessageService;
import test_support.TestAiChatService;
import test_support.TestAiConversationService;
import test_support.entity.GridOrder;
import test_support.view.GridDataTestView;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * "Ask AI" in the grid export dialog: when it is offered, the dialog that chooses the rows and the conversation, and
 * the chat it opens with the rows read the way the export reads them.
 */
@UiTest(viewBasePackages = {"io.jmix.aitoolsflowui.view", "io.jmix.aitoolsflowui.gridexport.view",
        "test_support.view"})
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class,
        AiGridDataTestConfiguration.class},
        properties = "jmix.aitools.ui.gridexport.max-rows=3")
class AskAiAboutDataTest {

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    Metadata metadata;
    @Autowired
    MessageTools messageTools;
    @Autowired
    Messages messages;
    @Autowired
    AiGridDataExportDialogContributor contributor;
    @Autowired
    AiGridDataSupport gridDataSupport;
    @MockitoSpyBean
    AccessManager accessManager;
    @Autowired
    TestAiChatService chatService;
    @Autowired
    TestAiConversationService conversationService;
    @Autowired
    TestAiChatMessageService messageService;

    DataGrid<GridOrder> grid;
    ExportAction exportAction;
    CollectionContainer<GridOrder> ordersDc;
    List<GridOrder> orders;

    @BeforeEach
    void setUp() {
        navigationSupport.navigate(GridDataTestView.class);
        GridDataTestView view = UiTestUtils.getCurrentView();
        grid = UiTestUtils.getComponent(view, "ordersDataGrid");
        exportAction = (ExportAction) Objects.requireNonNull(grid.getAction("excelExportAction"));
        ordersDc = ViewControllerUtils.getViewData(view).getContainer("ordersDc");
        setOrders(2);
    }

    @AfterEach
    void tearDown() {
        UiTestUtils.getOpenedDialogs().forEach(dialogInfo -> dialogInfo.getDialog().close());
        chatService.reset();
        conversationService.clear();
        messageService.clear();
    }

    @Test
    void exportDialog_offersAskAi_beforeCancel() {
        exportAction.execute();

        List<String> buttons = lastDialogButtonTexts();
        assertEquals(askAiText(), buttons.get(buttons.size() - 2));
    }

    @Test
    void askAi_chatUnavailable_notOffered() {
        chatService.setAvailable(false);

        assertTrue(contributor.getActions(exportContext()).isEmpty());
    }

    @Test
    void askAi_onlyExcludedColumnsExported_notOffered() {
        ExportDialogContext context = new ExportDialogContext(target(), exportAction,
                exportAction.getDataGridExporter(), column -> "internalNote".equals(column.getKey()));

        assertTrue(contributor.getActions(context).isEmpty());
    }

    @Test
    void askAi_exporterCannotReadRows_notOffered() {
        ExportDialogContext context = new ExportDialogContext(target(), exportAction,
                mock(DataGridExporter.class), column -> true);

        assertTrue(contributor.getActions(context).isEmpty());
    }

    @Test
    void askAi_onlyAllowedColumnHidden_offered_asTheExportReadsIt() {
        grid.getColumnByKey("number").setVisible(false);

        assertEquals(1, contributor.getActions(exportContext()).size());
    }

    @Test
    void askAi_chatViewNotPermitted_notOffered() {
        denyView("aitls_AiChatView");

        assertTrue(contributor.getActions(exportContext()).isEmpty());
    }

    @Test
    void askAi_dialogNotPermitted_notOffered() {
        denyView("aitls_AiGridDataDialog");

        assertTrue(contributor.getActions(exportContext()).isEmpty());
    }

    @Test
    void openChat_promptsGoWithRows_inMaximizableDialog() {
        AiGridDataContext context = new AiGridDataContext(target(),
                (AbstractDataGridExporter<?>) exportAction.getDataGridExporter(), column -> true);

        DialogWindow<AiChatView> window = gridDataSupport.openChat(context, ExportMode.CURRENT_PAGE, null,
                List.of("Compare"));

        assertTrue(window.isMaximizable());
        AiChatView chatView = UiTestUtils.getLastOpenedViewDialog();
        AiChatFragment fragment = UiTestUtils.getComponent(chatView, "chatFragment");
        assertEquals(List.of("Compare"), Objects.requireNonNull(fragment.getDataBlock()).getSuggestedPrompts());
    }

    @Test
    void askAi_emptyGrid_disabled() {
        ordersDc.setItems(List.of());

        List<Action> actions = contributor.getActions(exportContext());

        assertEquals(1, actions.size());
        assertFalse(actions.get(0).isEnabled());
    }

    @Test
    void dialog_noSelection_offersCurrentPageOnly_readOnly() {
        AiGridDataDialog dialog = openAskAiDialog();

        JmixRadioButtonGroup<ExportMode> rowsField = UiTestUtils.getComponent(dialog, "rowsField");
        assertEquals(List.of(ExportMode.CURRENT_PAGE), rowsField.getListDataView().getItems().toList());
        assertTrue(rowsField.isReadOnly());
        assertEquals(viewMessage("aiGridDataDialog.rows.currentPage", 2),
                rowsField.getItemLabelGenerator().apply(ExportMode.CURRENT_PAGE));
    }

    @Test
    void dialog_withSelection_offersSelectedFirst() {
        grid.select(orders.get(0));

        AiGridDataDialog dialog = openAskAiDialog();

        JmixRadioButtonGroup<ExportMode> rowsField = UiTestUtils.getComponent(dialog, "rowsField");
        assertEquals(List.of(ExportMode.SELECTED_ROWS, ExportMode.CURRENT_PAGE),
                rowsField.getListDataView().getItems().toList());
        assertEquals(ExportMode.SELECTED_ROWS, rowsField.getValue());
        assertFalse(rowsField.isReadOnly());
        assertEquals(viewMessage("aiGridDataDialog.rows.selected", 1),
                rowsField.getItemLabelGenerator().apply(ExportMode.SELECTED_ROWS));
    }

    @Test
    void dialog_overRowLimit_warns_andStaysOpen() {
        setOrders(4);
        AiGridDataDialog dialog = openAskAiDialog();

        clickOpenChat(dialog);

        assertEquals(viewMessage("aiGridDataDialog.tooManyRows", 4, 3),
                UiTestUtils.getLastOpenedNotification().getText());
        assertSame(dialog, UiTestUtils.getLastOpenedViewDialog());
    }

    @Test
    void dialog_openChat_closes_andChatGetsSelectedRowsWithoutExcludedColumns() {
        setOrders(3);
        grid.select(List.of(orders.get(0), orders.get(2)));
        AiGridDataDialog dialog = openAskAiDialog();

        clickOpenChat(dialog);

        assertFalse(UiTestUtils.getOpenedViewDialogs().contains(dialog));
        AiChatView chatView = UiTestUtils.getLastOpenedViewDialog();
        AiChatFragment fragment = UiTestUtils.getComponent(chatView, "chatFragment");
        assertTrue(fragment.isNewConversation());
        AiChatDataBlock block = Objects.requireNonNull(fragment.getDataBlock());
        String listTitle = messageTools.getEntityCaption(metadata.getClass(GridOrder.class));
        assertEquals(messages.formatMessage("io.jmix.aitoolsflowui.gridexport", "aiGridDataSupport.chipCaption",
                listTitle, 2), block.getCaption());
        assertTrue(block.getContent().contains("| N1 |"));
        assertTrue(block.getContent().contains("| N3 |"));
        assertFalse(block.getContent().contains("N2"));
        assertFalse(block.getContent().contains("note"));
        assertSame(metadata.getClass(GridOrder.class), block.getMetaClass());
        assertSame(UiComponentUtils.getView(grid), block.getView());
    }

    @Test
    void dialog_conversationChosen_chatContinuesIt() {
        AiConversation conversation = conversationService.addConversation("Orders review");
        AiGridDataDialog dialog = openAskAiDialog();
        JmixComboBox<AiConversation> conversationField = UiTestUtils.getComponent(dialog, "conversationField");
        conversationField.setValue(conversation);

        clickOpenChat(dialog);

        AiChatView chatView = UiTestUtils.getLastOpenedViewDialog();
        AiChatFragment fragment = UiTestUtils.getComponent(chatView, "chatFragment");
        assertFalse(fragment.isNewConversation());
        assertNotNull(fragment.getDataBlock());
        assertEquals("Orders review", chatView.getPageTitle());
    }

    @Test
    void dialog_conversationSearch_byTitle_mostRecentlyActiveFirst() {
        OffsetDateTime now = OffsetDateTime.now();
        AiConversation createdLater = conversationService.addConversation("Orders 2025", now.minusDays(3));
        AiConversation activeLater = conversationService.addConversation("Monthly orders", now.minusDays(5));
        messageService.addMessage(activeLater, now.minusHours(1));
        conversationService.addConversation("Invoices", now.minusDays(1));
        AiGridDataDialog dialog = openAskAiDialog();
        JmixComboBox<AiConversation> conversationField = UiTestUtils.getComponent(dialog, "conversationField");

        @SuppressWarnings("unchecked")
        DataProvider<AiConversation, String> dataProvider =
                (DataProvider<AiConversation, String>) conversationField.getDataProvider();
        List<AiConversation> found = dataProvider.fetch(new Query<>(0, 50, List.of(), null, "ORDERS")).toList();

        assertEquals(List.of(activeLater, createdLater), found);
    }

    void denyView(String viewId) {
        doAnswer(invocation -> {
            invocation.callRealMethod();
            if (invocation.getArgument(0) instanceof UiShowViewContext showViewContext
                    && viewId.equals(showViewContext.getViewId())) {
                showViewContext.setDenied();
            }
            return null;
        }).when(accessManager).applyRegisteredConstraints(any());
    }

    void setOrders(int count) {
        orders = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            GridOrder order = metadata.create(GridOrder.class);
            order.setNumber("N" + i);
            order.setInternalNote("note " + i);
            orders.add(order);
        }
        ordersDc.setItems(orders);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    ListDataComponent<Object> target() {
        return (ListDataComponent) grid;
    }

    ExportDialogContext exportContext() {
        return new ExportDialogContext(target(), exportAction, exportAction.getDataGridExporter(), column -> true);
    }

    AiGridDataDialog openAskAiDialog() {
        Action askAi = contributor.getActions(exportContext()).get(0);
        askAi.actionPerform(grid);
        return UiTestUtils.getLastOpenedViewDialog();
    }

    void clickOpenChat(AiGridDataDialog dialog) {
        UiTestUtils.<Button>getComponent(dialog, "openChatBtn").click();
    }

    List<String> lastDialogButtonTexts() {
        DialogInfo dialogInfo = UiTestUtils.getLastOpenedDialog();
        assertNotNull(dialogInfo);
        return dialogInfo.getButtons().stream()
                .map(Button::getText)
                .toList();
    }

    String askAiText() {
        return messages.getMessage("io.jmix.aitoolsflowui.gridexport", "aiGridDataExportDialogContributor.askAi");
    }

    String viewMessage(String key, Object... params) {
        return messages.formatMessage("io.jmix.aitoolsflowui.gridexport.view", key, params);
    }
}
