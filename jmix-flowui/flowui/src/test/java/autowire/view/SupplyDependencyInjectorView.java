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

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.view.*;
import test_support.entity.sales.Customer;

@Route("supply-dependency-injector-view")
@ViewController("SupplyDependencyInjectorView")
@ViewDescriptor("supply-dependency-injector-view.xml")
public class SupplyDependencyInjectorView extends StandardView {

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
