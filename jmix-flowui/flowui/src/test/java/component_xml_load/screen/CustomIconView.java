/*
 * Copyright 2025 Haulmont.
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
import io.jmix.flowui.kit.action.BaseAction;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route("custom-icon-view")
@ViewController("CustomIconView")
@ViewDescriptor("custom-icon-view.xml")
public class CustomIconView extends StandardView {

    @ViewComponent
    public BaseAction iconAttributeAction;
    @ViewComponent
    public BaseAction iconAction;
    @ViewComponent
    public BaseAction svgIconAction;
    @ViewComponent
    public BaseAction fontIconAction;
    @ViewComponent
    public BaseAction imageAction;

    @ViewComponent
    public JmixButton iconAttributeButton;
    @ViewComponent
    public JmixButton iconButton;
    @ViewComponent
    public JmixButton svgIconButton;
    @ViewComponent
    public JmixButton fontIconButton;
    @ViewComponent
    public JmixButton imageButton;

    @ViewComponent
    public JmixButton iconAttributeActionButton;
    @ViewComponent
    public JmixButton iconActionButton;
    @ViewComponent
    public JmixButton svgIconActionButton;
    @ViewComponent
    public JmixButton fontIconActionButton;
    @ViewComponent
    public JmixButton imageActionButton;

    @ViewComponent
    public JmixButton overrideIconAttributeActionButton;
    @ViewComponent
    public JmixButton overrideIconActionButton;
    @ViewComponent
    public JmixButton overrideSvgIconActionButton;
    @ViewComponent
    public JmixButton overrideFontIconActionButton;
    @ViewComponent
    public JmixButton overrideImageActionButton;
}
