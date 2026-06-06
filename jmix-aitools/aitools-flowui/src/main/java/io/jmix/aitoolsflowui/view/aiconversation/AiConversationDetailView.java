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

package io.jmix.aitoolsflowui.view.aiconversation;

import com.vaadin.flow.router.Route;
import io.jmix.aitools.entity.AiConversation;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "aitols-ai-conversations/:id", layout = DefaultMainViewParent.class)
@ViewController("aitols_AiConversation.detail")
@ViewDescriptor("ai-conversation-detail-view.xml")
@EditedEntityContainer("aiConversationDc")
@DialogMode(width = "32em", resizable = true)
public class AiConversationDetailView extends StandardDetailView<AiConversation> {

    @ViewComponent
    private TypedTextField<String> createdByField;
    @ViewComponent
    private TypedDateTimePicker<?> createdDateField;

    @Subscribe
    public void onInitEntity(final InitEntityEvent<AiConversation> event) {
        createdByField.setVisible(false);
        createdDateField.setVisible(false);
    }
}
