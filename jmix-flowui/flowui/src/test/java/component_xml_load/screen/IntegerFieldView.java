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
import io.jmix.flowui.component.textfield.JmixIntegerField;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route("interger-field-view")
@ViewDescriptor("integer-field-view.xml")
@ViewController("IntegerFieldView")
public class IntegerFieldView extends StandardView {

    @ViewComponent
    public JmixIntegerField integerFieldWithValueId;

    @ViewComponent
    public JmixIntegerField integerFieldId;
}
