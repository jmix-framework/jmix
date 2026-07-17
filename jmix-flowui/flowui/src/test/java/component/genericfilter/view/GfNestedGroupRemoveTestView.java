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

/**
 * A run-time configuration whose root contains a nested group with a single property condition,
 * used to reproduce the removal behaviour of a condition nested inside a group.
 */
@Route(value = "gf-nested-group-remove-test-view")
@ViewController("GfNestedGroupRemoveTestView")
@ViewDescriptor("gf-nested-group-remove-test-view.xml")
public class GfNestedGroupRemoveTestView extends StandardView {

    @ViewComponent
    public GenericFilter genericFilter;

    public GroupFilter nestedGroup;
    public PropertyFilter<String> nestedCondition;

    @Subscribe
    public void onInit(final InitEvent event) {
        nestedCondition = genericFilter.filterComponentBuilder()
                .<String>propertyFilter()
                .property("number")
                .operation(PropertyFilter.Operation.EQUAL)
                .build();
        nestedCondition.setValue("n1");

        nestedGroup = genericFilter.filterComponentBuilder()
                .groupFilter()
                .add(nestedCondition)
                .build();

        genericFilter.runtimeConfigurationBuilder()
                .id("c1")
                .name("C1")
                .add(nestedGroup)
                .makeCurrent()
                .buildAndRegister();
    }
}
