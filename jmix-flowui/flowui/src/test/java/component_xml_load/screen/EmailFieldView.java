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
import io.jmix.flowui.component.textfield.JmixEmailField;
import io.jmix.flowui.screen.ComponentId;
import io.jmix.flowui.screen.StandardScreen;
import io.jmix.flowui.screen.UiController;
import io.jmix.flowui.screen.UiDescriptor;

@Route("email-field")
@UiController
@UiDescriptor("email-field.xml")
public class EmailFieldView extends StandardScreen {

    @ComponentId
    public JmixEmailField emailFieldId;

    @ComponentId
    public JmixEmailField emailFieldWithValueId;
}
