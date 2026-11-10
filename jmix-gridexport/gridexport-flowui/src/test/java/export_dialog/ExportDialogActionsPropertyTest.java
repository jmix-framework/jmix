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

import export_dialog.view.ExportDialogActionsTestView;
import io.jmix.core.DataManager;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.gridexportflowui.action.ExportAction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.GridExportFlowuiTestConfiguration;
import test_support.action.TestDialogActionRecorder;
import test_support.action.TestFirstDialogAction;
import test_support.action.TestSecondDialogAction;
import test_support.entity.Product;

import java.util.List;

import static export_dialog.ExportDialogActionsTest.closeDialogs;
import static export_dialog.ExportDialogActionsTest.openDialogButtonTexts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Dialog actions listed in {@code jmix.gridexport.dialog-actions} are shown by every export action that does not
 * set its own list.
 */
@UiTest(viewBasePackages = "export_dialog.view")
@SpringBootTest(classes = {GridExportFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class},
        properties = "jmix.gridexport.dialog-actions=" + TestFirstDialogAction.ID + "," + TestSecondDialogAction.ID)
class ExportDialogActionsPropertyTest {

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    DataManager dataManager;
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
    void execute_propertyIds_buttonsAfterModesBeforeCancel_inPropertyOrder() {
        assertEquals(List.of(TestFirstDialogAction.ID, TestSecondDialogAction.ID), exportAction.getDialogActions());

        List<String> buttons = openDialogButtonTexts(exportAction);

        // Export modes (all rows, current page; no selection), the two dialog actions and Cancel.
        assertEquals(5, buttons.size());
        assertEquals(List.of(TestFirstDialogAction.TEXT, TestSecondDialogAction.TEXT), buttons.subList(2, 4));
    }

    @Test
    void setDialogActions_replacesPropertyList() {
        exportAction.setDialogActions(List.of(TestSecondDialogAction.ID));

        List<String> buttons = openDialogButtonTexts(exportAction);

        assertEquals(4, buttons.size());
        assertEquals(TestSecondDialogAction.TEXT, buttons.get(2));
        assertFalse(buttons.contains(TestFirstDialogAction.TEXT));
    }

    @Test
    void setDialogActions_emptyList_removesPropertyDialogActions() {
        exportAction.setDialogActions(List.of());

        List<String> buttons = openDialogButtonTexts(exportAction);

        assertEquals(3, buttons.size());
        assertFalse(buttons.contains(TestFirstDialogAction.TEXT));
        assertFalse(buttons.contains(TestSecondDialogAction.TEXT));
    }

    @Test
    void dialogActionsXmlProperty_replacesPropertyList() {
        ExportAction xmlExportAction = (ExportAction) dataGrid.getAction("jsonExportAction");
        assertNotNull(xmlExportAction);

        assertEquals(List.of(TestSecondDialogAction.ID, TestFirstDialogAction.ID), xmlExportAction.getDialogActions());
    }
}
