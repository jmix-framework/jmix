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
import io.jmix.flowui.component.logicalfilter.GroupFilter;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.view.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A run-time configuration with a numeric condition in the root and a condition nested in a group,
 * used to check how filter values are cleared.
 */
@Route(value = "gf-clear-values-test-view")
@ViewController("GfClearValuesTestView")
@ViewDescriptor("gf-clear-values-test-view.xml")
public class GfClearValuesTestView extends StandardView {

    @ViewComponent
    public GenericFilter genericFilter;

    public PropertyFilter<BigDecimal> amountCondition;
    public PropertyFilter<LocalDate> dateCondition;
    public GroupFilter nestedGroup;
    public PropertyFilter<String> nestedCondition;
    public GroupFilter deepGroup;
    public PropertyFilter<BigDecimal> deepCondition;

    @Subscribe
    public void onInit(final InitEvent event) {
        amountCondition = genericFilter.filterComponentBuilder()
                .<BigDecimal>propertyFilter()
                .property("amount")
                .operation(PropertyFilter.Operation.EQUAL)
                .build();

        dateCondition = genericFilter.filterComponentBuilder()
                .<LocalDate>propertyFilter()
                .property("date")
                .operation(PropertyFilter.Operation.EQUAL)
                .build();

        nestedCondition = genericFilter.filterComponentBuilder()
                .<String>propertyFilter()
                .property("number")
                .operation(PropertyFilter.Operation.EQUAL)
                .build();
        nestedCondition.setValue("n1");

        deepCondition = genericFilter.filterComponentBuilder()
                .<BigDecimal>propertyFilter()
                .property("amount")
                .operation(PropertyFilter.Operation.GREATER)
                .build();

        deepGroup = genericFilter.filterComponentBuilder()
                .groupFilter()
                .add(deepCondition)
                .build();

        nestedGroup = genericFilter.filterComponentBuilder()
                .groupFilter()
                .add(nestedCondition)
                .add(deepGroup)
                .build();

        genericFilter.runtimeConfigurationBuilder()
                .id("c1")
                .name("C1")
                .add(amountCondition)
                .add(dateCondition)
                .add(nestedGroup)
                .makeCurrent()
                .buildAndRegister();
    }
}
