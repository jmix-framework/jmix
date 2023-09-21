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

package view_root_component.view;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;

@Route("view-root-component")
@ViewController("viewRootComponent")
@ViewDescriptor("view-root-component.xml")
public class ViewRootComponentView extends View<Div> {

    @ViewComponent
    private JmixButton simpleBtn;

    @Subscribe("simpleBtn")
    public void onSimpleBtnClick(final ClickEvent<JmixButton> event) {

    }
}
