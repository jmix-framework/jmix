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

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.view.Supply;
import io.jmix.flowui.view.ViewComponent;
import test_support.entity.sales.Customer;

@FragmentDescriptor("supply-dependency-injector-fragment.xml")
public class SupplyDependencyInjectorFragment extends Fragment<VerticalLayout> {

    @ViewComponent
    public DataGrid<Customer> dataGrid;
    @ViewComponent
    public JmixSelect<Customer> component;

    @Supply(to = "dataGrid.name", subject = "renderer")
    protected Renderer<Customer> dataGridNameRenderer() {
        return new ComponentRenderer<>(this::createGradeComponent, this::gradeComponentUpdater);
    }

    @Supply(to = "component", subject = "renderer")
    protected ComponentRenderer<Span, Customer> componentRenderer() {
        return new ComponentRenderer<>(customer -> {
            Span span = new Span();
            span.setText(customer.getName());
            return span;
        });
    }

    protected void gradeComponentUpdater(Span span, Customer customer) {
        span.setText(customer.getName());
    }

    protected Span createGradeComponent() {
        return new Span();
    }
}
