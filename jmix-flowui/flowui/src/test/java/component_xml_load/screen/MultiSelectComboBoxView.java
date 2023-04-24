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
import io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox;
import io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import test_support.entity.sales.ProductTag;

@Route("multi-select-combo-box-view")
@ViewController("MultiSelectComboBoxView")
@ViewDescriptor("multi-select-combo-box-view.xml")
public class MultiSelectComboBoxView extends StandardView {

    @ViewComponent
    public JmixMultiSelectComboBox<ProductTag> multiSelectComboBoxId;

    @ViewComponent
    public JmixMultiSelectComboBoxPicker<ProductTag> multiSelectComboBoxPickerId;

    @ViewComponent
    public JmixMultiSelectComboBox<ProductTag> multiSelectComboBoxMetaClassId;

    @ViewComponent
    public JmixMultiSelectComboBoxPicker<ProductTag> multiSelectComboBoxPickerMetaClassId;

    @ViewComponent
    public CollectionContainer<ProductTag> productTagsDc;

}
