/*
 * Copyright 2024 Haulmont.
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

package component.composite.component;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.component.composite.CompositeComponent;
import io.jmix.flowui.component.composite.CompositeDescriptor;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.data.grid.ContainerDataGridItems;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import org.springframework.lang.Nullable;
import test_support.entity.sales.Product;

@CompositeDescriptor("test-datagrid-panel.xml")
public class TestDataGridPanel extends CompositeComponent<VerticalLayout> {

    private int counter = 0;

    private JmixButton testBtn;

    public TestDataGridPanel() {
        addPostInitListener(this::onPostInit);
    }

    private void onPostInit(PostInitEvent postInitEvent) {
        BaseAction testAction = ((BaseAction) getActions().getAction("testAction"));
        testAction.addActionPerformedListener(actionPerformedEvent -> {
            counter++;
        });

        testBtn = getInnerComponent("testBtn");
    }

    public void setDataContainer(CollectionContainer<Product> dataContainer) {
        DataGrid<Product> dataGrid = getInnerComponent("dataGrid");
        dataGrid.setItems(new ContainerDataGridItems<>(dataContainer));
    }

    @Nullable
    public DataGridItems<Product> getItems() {
        DataGrid<Product> dataGrid = getInnerComponent("dataGrid");
        return dataGrid.getItems();
    }

    public void click() {
        testBtn.click();
    }

    public int getClicks() {
        return counter;
    }

    public JmixButton getTestBtn() {
        return testBtn;
    }
}
