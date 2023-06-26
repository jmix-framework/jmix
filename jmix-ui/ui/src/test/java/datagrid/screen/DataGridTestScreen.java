/*
 * Copyright 2022 Haulmont.
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

package datagrid.screen;

import io.jmix.ui.UiComponents;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.Label;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.sales.Order;

@UiController
@UiDescriptor("datagrid-test-screen.xml")
public class DataGridTestScreen extends Screen {
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    public DataGrid<Order> dataGrid;
    @Autowired
    public Button showBtn;
    @Autowired
    public Button hideBtn;

    @SuppressWarnings("unchecked")
    @Install(to = "dataGrid.generated", subject = "columnGenerator")
    protected Component dataGridGeneratedColumnGenerator(final DataGrid.ColumnGeneratorEvent<Order> columnGeneratorEvent) {
        Label<String> label = uiComponents.create(Label.class);
        label.setValue("Generated");
        return label;
    }

    @Subscribe("hideBtn")
    protected void onHideBtnClick(final Button.ClickEvent event) {
        dataGrid.getColumnNN("generated").setVisible(false);
    }

    @Subscribe("showBtn")
    protected void onShowBtnClick(final Button.ClickEvent event) {
        dataGrid.getColumnNN("generated").setVisible(true);
    }
}
