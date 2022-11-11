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

package data_context.view;

import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.*;
import io.jmix.flowui.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.sales.OrderLine;
import test_support.entity.sales.OrderLineParam;

@ViewController
@ViewDescriptor("line-view.xml")
@EditedEntityContainer("lineDc")
public class LineView extends StandardDetailView<OrderLine> {

    private static final Logger log = LoggerFactory.getLogger(LineView.class);

    @ViewComponent
    private TypedTextField<Integer> qtyField;

    @Autowired
    private DataComponents dataComponents;

    @ViewComponent
    public DataGrid<OrderLineParam> paramsDataGrid;

    @ViewComponent
    private DataContext dataContext;
    @ViewComponent
    private InstanceContainer<OrderLine> lineDc;
    @ViewComponent
    private InstanceLoader<OrderLine> lineDl;
    @ViewComponent
    public CollectionPropertyContainer<OrderLineParam> paramsDc;

    @Subscribe
    protected void onInit(View.InitEvent event) {
        log.debug("onInit: dataContext={}", getViewData().getDataContext());
    }

    @Subscribe
    private void onBeforeShow(View.BeforeShowEvent event) {
        lineDl.load();
    }

    public void changeSaveAndClose(int quantity) {
        qtyField.setTypedValue(quantity);
        closeWithSave();
    }
}
