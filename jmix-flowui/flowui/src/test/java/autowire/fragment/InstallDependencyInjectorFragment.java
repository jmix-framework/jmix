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

package autowire.fragment;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.core.LoadContext;
import io.jmix.core.SaveContext;
import io.jmix.flowui.component.combobox.JmixComboBox;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.view.Install;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.ViewComponent;
import test_support.entity.sales.Customer;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@FragmentDescriptor("install-dependency-injector-fragment.xml")
public class InstallDependencyInjectorFragment extends Fragment<VerticalLayout> {

    @ViewComponent
    public DataContext dataContext;
    @ViewComponent
    public CollectionLoader<Customer> collectionDl;
    @ViewComponent
    public JmixComboBox<Customer> component;
    @ViewComponent
    public DataGrid<Customer> dataGrid;

    @Install(target = Target.DATA_CONTEXT)
    protected Set<Customer> saveDelegate(SaveContext saveContext) {
        return Collections.emptySet();
    }

    @Install(to = "collectionDl", target = Target.DATA_LOADER)
    protected List<Customer> collectionDlLoadDelegate(LoadContext<Customer> loadContext) {
        return Collections.emptyList();
    }

    @Install(to = "component", subject = "itemLabelGenerator")
    protected String componentItemLabelGenerator(Customer customer) {
        return "";
    }

    @Install(to = "dataGrid", subject = "partNameGenerator")
    protected String dataGridPartNameGenerator(Customer customer) {
        return "";
    }

    @Install(to = "dataGrid", subject = "partNameGenerator")
    protected boolean dataGridDropFilter(Customer customer) {
        return true;
    }

    @Install(to = "dataGrid", subject = "partNameGenerator")
    protected boolean dataGridDragFilter(Customer customer) {
        return true;
    }
}
