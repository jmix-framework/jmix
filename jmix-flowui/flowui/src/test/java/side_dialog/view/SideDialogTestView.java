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

package side_dialog.view;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.component.sidedialog.SideDialog;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route("SideDialogTestView")
@ViewController("SideDialogTestView")
@ViewDescriptor("side-dialog-test-view.xml")
public class SideDialogTestView extends StandardView {

    @Autowired
    public Dialogs dialogs;

    public SideDialog sideDialog;

    @Subscribe(id = "openSideDialogBtn", subject = "clickListener")
    public void onOpenSideDialogBtnClick(ClickEvent<JmixButton> event) {
        Span content = new Span();
        content.setId("content");

        Span footer = new Span();
        footer.setId("content");

        sideDialog = dialogs.createSideDialog()
                .withHeaderProvider(dialog -> {
                    Span header = new Span();
                    header.setId("header");
                    return header;
                })
                .withContentComponents(content)
                .withFooterComponents(footer)
                .open();
    }

    @Subscribe(id = "closeSideDialogBtn", subject = "clickListener")
    public void onCloseSideDialogBtnClick(ClickEvent<JmixButton> event) {
        sideDialog.close();
    }
}
