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
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.configuration.RunTimeConfiguration;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.view.*;

/**
 * Sets a base condition on the data loader after activating a configuration in {@code onInit}.
 */
@Route(value = "gf-base-after-activation-view")
@ViewController("GfBaseConditionAfterActivationView")
@ViewDescriptor("gf-activation-nodlc-view.xml")
public class GfBaseConditionAfterActivationView extends StandardView {

    @ViewComponent
    public GenericFilter genericFilter;

    @Subscribe
    public void onInit(final InitEvent event) {
        PropertyFilter<String> number1 = genericFilter.filterComponentBuilder()
                .<String>propertyFilter()
                .property("number")
                .operation(PropertyFilter.Operation.EQUAL)
                .build();
        RunTimeConfiguration c1 = genericFilter.runtimeConfigurationBuilder()
                .id("c1")
                .name("C1")
                .add(number1, "n1")
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

        genericFilter.setCurrentConfiguration(c1);
        genericFilter.getDataLoader().setCondition(PropertyCondition.greater("amount", 0));
    }
}
