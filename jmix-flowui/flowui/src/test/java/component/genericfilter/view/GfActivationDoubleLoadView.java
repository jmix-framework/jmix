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
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.configuration.RunTimeConfiguration;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import test_support.entity.sales.Order;

/**
 * No {@code DataLoadCoordinator}. A configuration with a value is made current synchronously in
 * {@code onInit} (so {@code applyFilterIfNeeded} loads it once), and a second configuration is
 * activated via the builder's deferred {@code makeCurrent()}. The deferred activation must NOT add a
 * second load — exactly one load is expected on open.
 */
@Route(value = "gf-activation-double-load-view")
@ViewController("GfActivationDoubleLoadView")
@ViewDescriptor("gf-activation-nodlc-view.xml")
public class GfActivationDoubleLoadView extends StandardView {

    @ViewComponent
    public GenericFilter genericFilter;
    @ViewComponent
    private CollectionLoader<Order> ordersDl;

    public int loadCount;

    @Subscribe
    public void onInit(final InitEvent event) {
        ordersDl.addPostLoadListener(e -> loadCount++);

        PropertyFilter<String> declValue = genericFilter.filterComponentBuilder()
                .<String>propertyFilter()
                .property("number")
                .operation(PropertyFilter.Operation.EQUAL)
                .build();
        RunTimeConfiguration declConfiguration = genericFilter.runtimeConfigurationBuilder()
                .id("decl")
                .name("Declarative-like default")
                .add(declValue, "d1")
                .buildAndRegister();
        // Make it current synchronously, emulating a default configuration that applyFilterIfNeeded loads.
        genericFilter.setCurrentConfiguration(declConfiguration);

        PropertyFilter<String> runtimeValue = genericFilter.filterComponentBuilder()
                .<String>propertyFilter()
                .property("number")
                .operation(PropertyFilter.Operation.EQUAL)
                .build();
        genericFilter.runtimeConfigurationBuilder()
                .id("rt")
                .name("Runtime")
                .add(runtimeValue, "n1")
                .makeCurrent()
                .buildAndRegister();
    }
}
