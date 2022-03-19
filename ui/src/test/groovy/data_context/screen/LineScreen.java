/*
 * Copyright 2020 Haulmont.
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

package data_context.screen;

import io.jmix.ui.component.Table;
import io.jmix.ui.component.TextField;
import io.jmix.ui.model.CollectionPropertyContainer;
import io.jmix.ui.model.DataComponents;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.EditedEntityContainer;
import io.jmix.ui.screen.StandardEditor;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.sales.OrderLine;
import test_support.entity.sales.OrderLineParam;

@UiController
@UiDescriptor("line-screen.xml")
@EditedEntityContainer("lineDc")
public class LineScreen extends StandardEditor<OrderLine> {

    private static final Logger log = LoggerFactory.getLogger(LineScreen.class);

    @Autowired
    private TextField<Integer> qtyField;

    @Autowired
    private DataComponents dataComponents;

    @Autowired
    public Table<OrderLineParam> paramsTable;

    @Autowired
    private DataContext dataContext;
    @Autowired
    private InstanceContainer<OrderLine> lineDc;
    @Autowired
    public CollectionPropertyContainer<OrderLineParam> paramsDc;

    @Subscribe
    protected void onInit(InitEvent event) {
        log.debug("onInit: dataContext={}", getScreenData().getDataContext());
    }

    @Subscribe
    private void onBeforeShow(BeforeShowEvent event) {
        OrderLine mergedOrderLine = dataContext.merge(getEditedEntity());
        lineDc.setItem(mergedOrderLine);
    }

    public void changeCommitAndClose(int quantity) {
        qtyField.setValue(quantity);
        closeWithCommit();
    }
}
