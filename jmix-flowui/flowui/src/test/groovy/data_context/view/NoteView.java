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

package data_context.view;

import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import test_support.entity.sales.OrderLineParamNote;

@ViewController
@ViewDescriptor("note-view.xml")
@EditedEntityContainer("noteDc")
public class NoteView extends StandardDetailView<OrderLineParamNote> {

    @ViewComponent
    private TypedTextField<String> textField;
    @ViewComponent
    private InstanceLoader<OrderLineParamNote> noteDl;

    @Subscribe
    public void onBeforeShow(View.BeforeShowEvent event) {
        noteDl.load();
    }

    public void changeTextSaveAndClose(String text) {
        textField.setTypedValue(text);
        closeWithSave();
    }
}
