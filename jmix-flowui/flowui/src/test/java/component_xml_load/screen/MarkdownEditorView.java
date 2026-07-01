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

import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.flowui.component.markdowneditor.MarkdownEditor;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.entity.sales.Order;

@Route("markdown-editor-view")
@ViewController("MarkdownEditorView")
@ViewDescriptor("markdown-editor-view.xml")
public class MarkdownEditorView extends StandardView {

    @ViewComponent
    public MarkdownEditor markdownEditor;
    @ViewComponent
    public MarkdownEditor dataBoundMarkdownEditor;
    @ViewComponent
    public InstanceContainer<Order> orderDc;

    @Autowired
    private DataManager dataManager;

    @Subscribe
    public void onInit(InitEvent event) {
        Order order = dataManager.create(Order.class);
        order.setNumber("## Markdown value");
        orderDc.setItem(order);
    }
}
