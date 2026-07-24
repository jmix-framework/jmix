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
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.view.*;

/**
 * Activates a configuration via {@code makeCurrent()} in {@code BeforeShowEvent}, i.e. when the filter
 * is already attached — the synchronous activation path. Switching afterwards must apply only the new
 * configuration's condition (the baseline was captured before, so it stays clean).
 */
@Route(value = "gf-activation-beforeshow-view")
@ViewController("GfActivationBeforeShowTestView")
@ViewDescriptor("gf-activation-nodlc-view.xml")
public class GfActivationBeforeShowTestView extends StandardView {

    @ViewComponent
    public GenericFilter genericFilter;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
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

    public void switchTo(String configurationId) {
        genericFilter.setCurrentConfiguration(genericFilter.getConfiguration(configurationId));
    }
}
