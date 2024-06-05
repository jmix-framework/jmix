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

package component.fragment.component;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.data.grid.ContainerDataGridItems;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import org.springframework.lang.Nullable;
import test_support.entity.sales.Product;

@FragmentDescriptor("test-datagrid-fragment.xml")
public class TestDataGridFragment extends Fragment<VerticalLayout> {

    private int counter = 0;

    private JmixButton testBtn;

    public TestDataGridFragment() {
        addReadyListener(this::onReady);
    }

    private void onReady(ReadyEvent readyEvent) {
        BaseAction testAction = ((BaseAction) getFragmentActions().getAction("testAction"));
        testAction.addActionPerformedListener(actionPerformedEvent -> counter++);

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
