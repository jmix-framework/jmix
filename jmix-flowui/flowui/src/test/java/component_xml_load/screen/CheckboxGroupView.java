/*
 * Copyright 2022 Haulmont.
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

package component_xml_load.screen;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.checkboxgroup.JmixCheckboxGroup;
import io.jmix.flowui.view.ComponentId;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.UiController;
import io.jmix.flowui.view.UiDescriptor;
import test_support.entity.sales.Order;

@Route("checkbox-group-view")
@UiController("CheckboxGroupView")
@UiDescriptor("checkbox-group-view.xml")
public class CheckboxGroupView extends StandardView {

    @ComponentId
    public JmixCheckboxGroup<Order> checkboxGroup;
}
