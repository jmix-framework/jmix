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

import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.model.CollectionPropertyContainer;
import io.jmix.flowui.model.InstanceLoader;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.sales.OrderLineParam;
import test_support.entity.sales.OrderLineParamNote;

@ViewController
@ViewDescriptor("param-view.xml")
@EditedEntityContainer("paramDc")
public class ParamView extends StandardDetailView<OrderLineParam> {

    @ViewComponent
    private TypedTextField<String> valueField;
    @ViewComponent
    private InstanceLoader<OrderLineParam> paramDl;
    @ViewComponent
    public DataGrid<OrderLineParamNote> notesDataGrid;
    @ViewComponent
    public CollectionPropertyContainer<OrderLineParamNote> notesDc;

    @Autowired
    private DialogWindows dialogWindows;

    @Subscribe
    public void onBeforeShow(View.BeforeShowEvent event) {
        paramDl.load();
    }

    public void changeValueSaveAndClose(String value) {
        valueField.setTypedValue(value);
        closeWithSave();
    }

    public NoteView buildNoteViewForEdit() {
        notesDataGrid.select(notesDc.getItems().get(0));
        return dialogWindows.detail(notesDataGrid)
                .withViewClass(NoteView.class)
                .open()
                .getView();
    }
}
