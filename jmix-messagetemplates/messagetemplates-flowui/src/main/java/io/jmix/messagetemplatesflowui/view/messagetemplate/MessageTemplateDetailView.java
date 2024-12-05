/*
 * Copyright 2024 Haulmont.
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

package io.jmix.messagetemplatesflowui.view.messagetemplate;

import com.vaadin.flow.router.Route;
import io.jmix.core.DataManager;
import io.jmix.core.Sort;
import io.jmix.flowui.view.*;
import io.jmix.messagetemplates.entity.MessageTemplate;
import io.jmix.messagetemplates.entity.MessageTemplateBlock;
import io.jmix.messagetemplatesflowui.kit.component.GrapesJs;
import io.jmix.messagetemplatesflowui.kit.component.GrapesJsBlock;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "msgtmp/messagetemplate/:id", layout = DefaultMainViewParent.class)
@ViewController("msgtmp_MessageTemplate.detail")
@ViewDescriptor("message-template-detail-view.xml")
@EditedEntityContainer("messageTemplateDc")
public class MessageTemplateDetailView extends StandardDetailView<MessageTemplate> {

    @ViewComponent
    protected GrapesJs grapesJsEditor;

    @Autowired
    protected DataManager dataManager;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        initBlocks();
        initGrapesJsEditor();
    }

    protected void initBlocks() {
        List<GrapesJsBlock> grapesJsBlocks = loadBlocks();
        grapesJsEditor.addBlocks(grapesJsBlocks);
    }

    protected void initGrapesJsEditor() {
        grapesJsEditor.setValue(getEditedEntity().getHtml());
        grapesJsEditor.addValueChangeEventListener(this::onGrapesJsValueChange);
    }

    protected List<GrapesJsBlock> loadBlocks() {
        return dataManager.load(MessageTemplateBlock.class)
                .all()
                .sort(Sort.by("name"))
                .list()
                .stream()
                .map(this::blockMapper)
                .toList();
    }

    protected GrapesJsBlock blockMapper(MessageTemplateBlock block) {
        return GrapesJsBlock.create(block.getName())
                .withLabel(block.getLabel())
                .withCategory(block.getCategory())
                .withContent(block.getContent())
                .withAttributes(block.getAttributes())
                .withIcon(block.getIcon())
                .build();
    }

    protected void onGrapesJsValueChange(GrapesJs.GrapesJsValueChangedEvent event) {
        getEditedEntity().setHtml(event.getValue());
    }
}
