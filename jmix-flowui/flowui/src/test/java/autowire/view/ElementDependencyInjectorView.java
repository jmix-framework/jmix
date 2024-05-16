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

package autowire.view;

import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.tabsheet.JmixTabSheet;
import io.jmix.flowui.facet.DataLoadCoordinator;
import io.jmix.flowui.facet.Timer;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.*;
import io.jmix.flowui.view.*;
import test_support.entity.sales.Customer;


@Route("element-dependency-injector-view")
@ViewController("ElementDependencyInjectorView")
@ViewDescriptor("element-dependency-injector-view.xml")
public class ElementDependencyInjectorView extends StandardView {

    @ViewComponent
    public JmixButton component;
    @ViewComponent
    public InstanceContainer<Customer> instanceDc;
    @ViewComponent
    public CollectionContainer<Customer> collectionDc;
    @ViewComponent
    public KeyValueCollectionContainer keyValueCollectionDc;
    @ViewComponent
    public InstanceLoader<Customer> instanceDl;
    @ViewComponent
    public CollectionLoader<Customer> collectionDl;
    @ViewComponent
    public KeyValueCollectionLoader keyValueCollectionDl;
    @ViewComponent
    public DataContext dataContext;
    @ViewComponent
    public MessageBundle messageBundle;
    @ViewComponent
    public Action viewAction;
    @ViewComponent("entityPicker.componentAction")
    public Action componentAction;
    @ViewComponent
    public Timer facet_1;
    @ViewComponent
    public DataLoadCoordinator facet_2;

    @ViewComponent("tabSheet")
    public JmixTabSheet tabSheet;

    @ViewComponent("tabSheet.tab1")
    public Tab tabSheetTab1;

    @ViewComponent("tabSheet.tab2")
    public Tab tabSheetTab2;
}
