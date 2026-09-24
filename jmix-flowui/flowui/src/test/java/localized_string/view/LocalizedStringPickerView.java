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

package localized_string.view;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.component.valuepicker.JmixValuePicker;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

/**
 * Hosts the pickers of the localized string edit action. The action opens its view through
 * {@code DialogWindows}, which needs an origin, so the pickers cannot be created standalone.
 */
@Route(value = "localized-string-picker-view")
@ViewController("LocalizedStringPickerView")
@ViewDescriptor("localized-string-picker-view.xml")
public class LocalizedStringPickerView extends StandardView {

    @ViewComponent
    public JmixValuePicker<String> plainPicker;

    @ViewComponent
    public JmixValuePicker<String> requiredPicker;

    @ViewComponent
    public JmixValuePicker<String> boundPicker;

    @ViewComponent
    public JmixValuePicker<String> titlePicker;

    @ViewComponent
    public JmixValuePicker<String> labelPicker;

    @ViewComponent
    public JmixValuePicker<String> lobPicker;

    @ViewComponent
    public JmixValuePicker<String> multilinePicker;

    @ViewComponent
    public InstanceContainer<test_support.entity.localized_string.LsItem> itemDc;
}
