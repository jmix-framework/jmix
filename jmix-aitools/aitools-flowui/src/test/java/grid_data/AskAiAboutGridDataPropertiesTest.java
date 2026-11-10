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
import io.jmix.aitoolsflowui.gridexport.AskAiAboutGridDataAction;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.testassist.dialog.DialogInfo;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import io.jmix.gridexportflowui.action.ExportAction;
import io.jmix.gridexportflowui.exporter.ExportMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.AiGridDataTestConfiguration;
import test_support.AiToolsFlowuiTestConfiguration;
import test_support.entity.GridOrder;
import test_support.view.GridDataTestView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * "Ask AI" listed in {@code jmix.gridexport.dialog-actions} takes the rows the user may choose from the export modes
 * of {@code jmix.gridexport.default-export-modes}.
 */
@UiTest(viewBasePackages = {"io.jmix.aitoolsflowui.view", "io.jmix.aitoolsflowui.gridexport.view",
        "test_support.view"})
@SpringBootTest(classes = {AiToolsFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class,
        AiGridDataTestConfiguration.class},
        properties = {
                "jmix.gridexport.dialog-actions=" + AskAiAboutGridDataAction.ID,
                "jmix.gridexport.default-export-modes=ALL_ROWS,SELECTED_ROWS"
        })
class AskAiAboutGridDataPropertiesTest {

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    Metadata metadata;
    @Autowired
    Messages messages;

    DataGrid<GridOrder> grid;
    ExportAction exportAction;
    List<GridOrder> orders;

    @BeforeEach
    void setUp() {
        navigationSupport.navigate(GridDataTestView.class);
        GridDataTestView view = UiTestUtils.getCurrentView();
        grid = UiTestUtils.getComponent(view, "ordersDataGrid");
        exportAction = (ExportAction) Objects.requireNonNull(grid.getAction("excelExportAction"));

        CollectionContainer<GridOrder> ordersDc = ViewControllerUtils.getViewData(view).getContainer("ordersDc");
        orders = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            GridOrder order = metadata.create(GridOrder.class);
            order.setNumber("N" + i);
            orders.add(order);
        }
        ordersDc.setItems(orders);
    }

    @AfterEach
    void tearDown() {
        UiTestUtils.getOpenedDialogs().forEach(dialogInfo -> dialogInfo.getDialog().close());
    }

    @Test
    void exportDialog_onlySelectedModeAllowed_noSelection_askAiButtonDisabled() {
        exportAction.execute();

        assertFalse(askAiButton().isEnabled());
    }

    @Test
    void exportDialog_onlySelectedModeAllowed_dialogOffersOnlySelected() {
        grid.select(orders.get(0));
        exportAction.execute();

        Button askAiButton = askAiButton();
        assertTrue(askAiButton.isEnabled());
        askAiButton.click();

        JmixRadioButtonGroup<ExportMode> rowsField =
                UiTestUtils.getComponent(UiTestUtils.getLastOpenedViewDialog(), "rowsField");
        assertEquals(List.of(ExportMode.SELECTED_ROWS), rowsField.getListDataView().getItems().toList());
        assertTrue(rowsField.isReadOnly());
    }

    Button askAiButton() {
        DialogInfo dialogInfo = UiTestUtils.getLastOpenedDialog();
        assertNotNull(dialogInfo);
        String askAiText = messages.getMessage("io.jmix.aitoolsflowui.gridexport", "askAiAboutGridDataAction.text");
        return dialogInfo.getButtons().stream()
                .filter(button -> askAiText.equals(button.getText()))
                .findFirst()
                .orElseThrow();
    }
}
