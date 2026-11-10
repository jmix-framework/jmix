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

package export_dialog;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import export_dialog.view.ExportDialogActionsTestView;
import io.jmix.core.DataManager;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.testassist.dialog.DialogInfo;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.gridexportflowui.GridExportProperties;
import io.jmix.gridexportflowui.action.ExportAction;
import io.jmix.gridexportflowui.action.ExportDialogContext;
import io.jmix.gridexportflowui.exporter.ExportMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.GridExportFlowuiTestConfiguration;
import test_support.action.TestDialogAction;
import test_support.action.TestDialogActionRecorder;
import test_support.action.TestDisabledDialogAction;
import test_support.action.TestFirstDialogAction;
import test_support.action.TestInvisibleDialogAction;
import test_support.action.TestPrimaryDialogAction;
import test_support.action.TestSecondDialogAction;
import test_support.entity.Product;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The export option dialog shows extra actions listed by their type ids, without the export knowing what they do.
 * Here {@code jmix.gridexport.dialog-actions} is not set.
 */
@UiTest(viewBasePackages = "export_dialog.view")
@SpringBootTest(classes = {GridExportFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class ExportDialogActionsTest {

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    DataManager dataManager;
    @Autowired
    GridExportProperties gridExportProperties;
    @Autowired
    TestDialogActionRecorder recorder;

    DataGrid<Product> dataGrid;
    ExportAction exportAction;

    @BeforeEach
    void setUp() {
        Product product = dataManager.create(Product.class);
        product.setName("Product 1");
        product.setPosition(1);
        dataManager.saveWithoutReload(product);

        navigationSupport.navigate(ExportDialogActionsTestView.class);
        ExportDialogActionsTestView view = UiTestUtils.getCurrentView();
        dataGrid = UiTestUtils.getComponent(view, "productsDataGrid");
        exportAction = (ExportAction) dataGrid.getAction("excelExportAction");
        assertNotNull(exportAction);
    }

    @AfterEach
    void tearDown() {
        closeDialogs();
        recorder.reset();
        dataManager.remove(dataManager.load(Product.class).all().list());
    }

    @Test
    void execute_emptyProperty_dialogUnchanged() {
        assertEquals(List.of(), exportAction.getDialogActions());

        List<String> buttons = openDialogButtonTexts(exportAction);

        // Export modes (all rows, current page; no selection) and Cancel.
        assertEquals(3, buttons.size());
        assertTrue(recorder.getCreatedActions().isEmpty());
    }

    @Test
    void setDialogActions_buttonsAfterModesBeforeCancel_inListedOrder() {
        List<String> modesAndCancel = openDialogButtonTexts(exportAction);
        closeDialogs();
        exportAction.setDialogActions(List.of(TestSecondDialogAction.ID, TestFirstDialogAction.ID));

        List<String> buttons = openDialogButtonTexts(exportAction);

        assertEquals(withDialogActions(modesAndCancel, TestSecondDialogAction.TEXT, TestFirstDialogAction.TEXT),
                buttons);
    }

    @Test
    void dialogActionsXmlProperty_loadsListInOrder() {
        ExportAction xmlExportAction = (ExportAction) dataGrid.getAction("jsonExportAction");
        assertNotNull(xmlExportAction);
        assertEquals(List.of(TestSecondDialogAction.ID, TestFirstDialogAction.ID), xmlExportAction.getDialogActions());

        List<String> modesAndCancel = openDialogButtonTexts(exportAction);
        closeDialogs();
        List<String> buttons = openDialogButtonTexts(xmlExportAction);

        assertEquals(withDialogActions(modesAndCancel, TestSecondDialogAction.TEXT, TestFirstDialogAction.TEXT),
                buttons);
    }

    @Test
    void execute_dialogAction_receivesTargetAndExportContext() {
        exportAction.setColumnKeysToExport(List.of("name"));
        exportAction.setDialogActions(List.of(TestFirstDialogAction.ID));

        exportAction.execute();

        TestDialogAction action = singleCreatedAction();
        assertSame(dataGrid, action.getTarget());
        assertTrue(action.isContextSetBeforeTarget());
        ExportDialogContext context = action.getExportDialogContext();
        assertNotNull(context);
        assertSame(dataGrid, context.getTarget());
        assertSame(exportAction, context.getExportAction());
        assertSame(exportAction.getDataGridExporter(), context.getExporter());
        //noinspection unchecked,rawtypes
        assertTrue(context.getColumnFilter().test((Grid.Column) dataGrid.getColumnByKey("name")));
        //noinspection unchecked,rawtypes
        assertFalse(context.getColumnFilter().test((Grid.Column) dataGrid.getColumnByKey("position")));
    }

    @Test
    void execute_eachDialog_createsNewActionInstance() {
        exportAction.setDialogActions(List.of(TestFirstDialogAction.ID));

        exportAction.execute();
        closeDialogs();
        exportAction.execute();

        List<TestDialogAction> created = recorder.getCreatedActions();
        assertEquals(2, created.size());
        assertNotSame(created.get(0), created.get(1));
    }

    @Test
    void dialogActionButton_click_performsActionWithTargetThenDetachesIt() {
        exportAction.setDialogActions(List.of(TestFirstDialogAction.ID));
        exportAction.execute();
        DialogInfo dialogInfo = UiTestUtils.getLastOpenedDialog();
        assertNotNull(dialogInfo);

        dialogInfo.getButtons().stream()
                .filter(button -> TestFirstDialogAction.TEXT.equals(button.getText()))
                .findFirst()
                .orElseThrow()
                .click();

        TestDialogAction action = singleCreatedAction();
        assertEquals(1, action.getPerformedCount());
        assertSame(dataGrid, action.getTargetOnPerform());
        assertFalse(dialogInfo.getDialog().isOpened());
        assertNull(action.getTarget());
    }

    @Test
    void cancelButton_click_detachesDialogActionTarget() {
        exportAction.setDialogActions(List.of(TestFirstDialogAction.ID));
        exportAction.execute();
        DialogInfo dialogInfo = UiTestUtils.getLastOpenedDialog();
        assertNotNull(dialogInfo);
        TestDialogAction action = singleCreatedAction();
        assertSame(dataGrid, action.getTarget());

        // Cancel is the last button.
        dialogInfo.getButtons().get(dialogInfo.getButtons().size() - 1).click();

        assertFalse(dialogInfo.getDialog().isOpened());
        assertNull(action.getTarget());
        assertEquals(0, action.getPerformedCount());
    }

    @Test
    void execute_invisibleDialogAction_addsNoButton() {
        exportAction.setDialogActions(List.of(TestInvisibleDialogAction.ID));

        List<String> buttons = openDialogButtonTexts(exportAction);

        assertEquals(3, buttons.size());
        assertFalse(buttons.contains(TestInvisibleDialogAction.TEXT));
        assertEquals(1, recorder.getCreatedActions().size());
    }

    @Test
    void execute_unknownDialogActionType_skipped() {
        exportAction.setDialogActions(List.of("test_unknownDialogAction", TestFirstDialogAction.ID));

        List<String> buttons = openDialogButtonTexts(exportAction);

        assertEquals(4, buttons.size());
        assertTrue(buttons.contains(TestFirstDialogAction.TEXT));
    }

    @Test
    void execute_noExportModes_dialogActionIsNotPrimary() {
        exportAction.setAvailableExportModes(List.of());
        exportAction.setDialogActions(List.of(TestFirstDialogAction.ID));

        List<String> buttons = openDialogButtonTexts(exportAction);

        // The dialog action and Cancel.
        assertEquals(2, buttons.size());
        assertEquals(TestFirstDialogAction.TEXT, buttons.get(0));
        assertEquals(ActionVariant.DEFAULT, singleCreatedAction().getVariant());
    }

    @Test
    void execute_primaryDialogAction_madeDefault() {
        exportAction.setDialogActions(List.of(TestPrimaryDialogAction.ID));

        exportAction.execute();

        assertEquals(ActionVariant.DEFAULT, singleCreatedAction().getVariant());
    }

    @Test
    void execute_disabledDialogAction_shownAsDisabledButton() {
        exportAction.setDialogActions(List.of(TestDisabledDialogAction.ID));
        exportAction.execute();
        DialogInfo dialogInfo = UiTestUtils.getLastOpenedDialog();
        assertNotNull(dialogInfo);

        Button button = dialogInfo.getButtons().stream()
                .filter(b -> TestDisabledDialogAction.TEXT.equals(b.getText()))
                .findFirst()
                .orElseThrow();

        assertFalse(button.isEnabled());
        assertFalse(singleCreatedAction().isEnabled());
    }

    @Test
    void setDialogActions_laterChangeOfPassedList_ignored() {
        List<String> passed = new ArrayList<>(List.of(TestFirstDialogAction.ID));
        exportAction.setDialogActions(passed);

        passed.add(TestSecondDialogAction.ID);

        assertEquals(List.of(TestFirstDialogAction.ID), exportAction.getDialogActions());
    }

    @Test
    void getAvailableExportModes_default_returnsPropertyModes() {
        assertEquals(gridExportProperties.getDefaultExportModes(), exportAction.getAvailableExportModes());
        assertEquals(List.of(ExportMode.ALL_ROWS, ExportMode.CURRENT_PAGE, ExportMode.SELECTED_ROWS),
                exportAction.getAvailableExportModes());
    }

    @Test
    void getAvailableExportModes_afterSet_returnsSetModes() {
        exportAction.setAvailableExportModes(List.of(ExportMode.SELECTED_ROWS));

        assertEquals(List.of(ExportMode.SELECTED_ROWS), exportAction.getAvailableExportModes());
    }

    TestDialogAction singleCreatedAction() {
        List<TestDialogAction> created = recorder.getCreatedActions();
        assertEquals(1, created.size());
        return created.get(0);
    }

    static List<String> withDialogActions(List<String> modesAndCancel, String... dialogActionTexts) {
        List<String> expected = new ArrayList<>(modesAndCancel.subList(0, modesAndCancel.size() - 1));
        expected.addAll(List.of(dialogActionTexts));
        expected.add(modesAndCancel.get(modesAndCancel.size() - 1));
        return expected;
    }

    static List<String> openDialogButtonTexts(ExportAction action) {
        action.execute();
        DialogInfo dialogInfo = UiTestUtils.getLastOpenedDialog();
        assertNotNull(dialogInfo);
        return dialogInfo.getButtons().stream()
                .map(Button::getText)
                .toList();
    }

    static void closeDialogs() {
        UiTestUtils.getOpenedDialogs().forEach(dialogInfo -> dialogInfo.getDialog().close());
    }
}
