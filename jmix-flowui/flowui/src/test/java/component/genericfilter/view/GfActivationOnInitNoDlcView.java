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

package component.genericfilter.view;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.genericfilter.Configuration;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import test_support.entity.sales.Order;

/**
 * No {@code DataLoadCoordinator}. Two configurations on {@code number}; the first is activated via the
 * builder's {@code makeCurrent()} during {@code onInit} (deferred-to-attach path).
 */
@Route(value = "gf-activation-oninit-nodlc-view")
@ViewController("GfActivationOnInitNoDlcView")
@ViewDescriptor("gf-activation-nodlc-view.xml")
public class GfActivationOnInitNoDlcView extends StandardView {

    @ViewComponent
    public GenericFilter genericFilter;
    @ViewComponent
    private CollectionLoader<Order> ordersDl;

    public int loadCount;
    public Configuration currentRightAfterMakeCurrent;

    @Subscribe
    public void onInit(final InitEvent event) {
        ordersDl.addPostLoadListener(e -> loadCount++);

        PropertyFilter<String> number1 = genericFilter.filterComponentBuilder()
                .<String>propertyFilter()
                .property("number")
                .operation(PropertyFilter.Operation.EQUAL)
                .build();
        genericFilter.runtimeConfigurationBuilder()
                .id("c1")
                .name("C1")
                .add(number1, "n1")
                .makeCurrent()
                .buildAndRegister();

        // Captured during onInit: activation is deferred, so this must still be the empty configuration.
        currentRightAfterMakeCurrent = genericFilter.getCurrentConfiguration();

        PropertyFilter<String> number2 = genericFilter.filterComponentBuilder()
                .<String>propertyFilter()
                .property("number")
                .operation(PropertyFilter.Operation.EQUAL)
                .build();
        genericFilter.runtimeConfigurationBuilder()
                .id("c2")
                .name("C2")
                .add(number2, "n2")
                .buildAndRegister();
    }
}
