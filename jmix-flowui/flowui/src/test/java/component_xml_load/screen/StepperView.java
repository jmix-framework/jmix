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

package component_xml_load.screen;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.stepper.Stepper;
import io.jmix.flowui.kit.component.stepper.StepperStep;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "stepper-view")
@ViewController("StepperView")
@ViewDescriptor("stepper-view.xml")
public class StepperView extends StandardView {

    @ViewComponent
    public Stepper stepperId;
    @ViewComponent
    public StepperStep firstStep;
    @ViewComponent
    public StepperStep hiddenStep;
    @ViewComponent
    public StepperStep emptyStep;
    @ViewComponent
    public VerticalLayout firstContent;
    @ViewComponent
    public Span hiddenContent;
}
