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
import io.jmix.core.DataManager;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.testassist.dialog.DialogInfo;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.gridexportflowui.action.ExportAction;
import io.jmix.gridexportflowui.action.ExportDialogContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import read_rows.view.ReadRowsTestView;
import test_support.GridExportFlowuiTestConfiguration;
import test_support.TestExportDialogActionContributor;
import test_support.entity.Product;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Other add-ons can add their own actions to the export option dialog without the export knowing about them.
 */
@UiTest(viewBasePackages = "read_rows.view")
@SpringBootTest(classes = {GridExportFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
class ExportDialogActionContributorTest {

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    DataManager dataManager;
    @Autowired
    @Qualifier("firstExportDialogActionContributor")
    TestExportDialogActionContributor firstContributor;
    @Autowired
    @Qualifier("secondExportDialogActionContributor")
    TestExportDialogActionContributor secondContributor;

    DataGrid<Product> dataGrid;
    ExportAction exportAction;

    @BeforeEach
    void setUp() {
        Product product = dataManager.create(Product.class);
        product.setName("Product 1");
        product.setPosition(1);
        dataManager.saveWithoutReload(product);

        navigationSupport.navigate(ReadRowsTestView.class);
        ReadRowsTestView view = UiTestUtils.getCurrentView();
        dataGrid = UiTestUtils.getComponent(view, "productsDataGrid");
        exportAction = (ExportAction) dataGrid.getAction("excelExportAction");
        assertNotNull(exportAction);
    }

    @AfterEach
    void tearDown() {
        firstContributor.reset();
        secondContributor.reset();
        UiTestUtils.getOpenedDialogs().forEach(dialogInfo -> dialogInfo.getDialog().close());
        dataManager.remove(dataManager.load(Product.class).all().list());
    }

    @Test
    void execute_contributorsWithoutActions_dialogUnchanged() {
        List<String> buttons = openDialogButtonTexts();

        // Export modes (all rows, current page; no selection) and Cancel.
        assertEquals(3, buttons.size());
        assertFalse(buttons.contains("First"));
    }

    @Test
    void execute_contributedActions_inContributorOrderBeforeCancel() {
        List<String> modesAndCancel = openDialogButtonTexts();
        UiTestUtils.getOpenedDialogs().forEach(dialogInfo -> dialogInfo.getDialog().close());
        firstContributor.setEnabled(true);
        secondContributor.setEnabled(true);

        List<String> buttons = openDialogButtonTexts();

        List<String> expected = new ArrayList<>(modesAndCancel.subList(0, modesAndCancel.size() - 1));
        expected.add("First");
        expected.add("Second");
        expected.add(modesAndCancel.get(modesAndCancel.size() - 1));
        assertEquals(expected, buttons);
    }

    @Test
    void execute_contributor_receivesExportContext() {
        exportAction.setColumnKeysToExport(List.of("name"));

        exportAction.execute();

        ExportDialogContext context = firstContributor.getLastContext();
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
    void contributedAction_click_performsHandler() {
        firstContributor.setEnabled(true);
        exportAction.execute();
        DialogInfo dialogInfo = UiTestUtils.getLastOpenedDialog();
        assertNotNull(dialogInfo);

        dialogInfo.getButtons().stream()
                .filter(button -> "First".equals(button.getText()))
                .findFirst()
                .orElseThrow()
                .click();

        assertEquals(1, firstContributor.getPerformedCount());
    }

    @Test
    void execute_invisibleContributedAction_addsNoButton() {
        firstContributor.setEnabled(true);
        firstContributor.setActionVisible(false);

        List<String> buttons = openDialogButtonTexts();

        assertEquals(3, buttons.size());
        assertFalse(buttons.contains("First"));
    }

    @Test
    void execute_noExportModes_contributedActionIsNotMadePrimary() {
        exportAction.setAvailableExportModes(List.of());
        firstContributor.setEnabled(true);

        exportAction.execute();

        List<Action> returned = firstContributor.getReturnedActions();
        assertEquals(1, returned.size());
        assertEquals(ActionVariant.DEFAULT, returned.get(0).getVariant());
    }

    List<String> openDialogButtonTexts() {
        exportAction.execute();
        DialogInfo dialogInfo = UiTestUtils.getLastOpenedDialog();
        assertNotNull(dialogInfo);
        return dialogInfo.getButtons().stream()
                .map(Button::getText)
                .toList();
    }
}
