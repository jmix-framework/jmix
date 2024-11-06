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
import io.jmix.flowui.view.*;
import io.jmix.messagetemplates.entity.MessageTemplate;

@Route(value = "msgtmp/messagetemplate/:id", layout = DefaultMainViewParent.class)
@ViewController("msgtmp_MessageTemplate.detail")
@ViewDescriptor("message-template-detail-view.xml")
@EditedEntityContainer("messageTemplateDc")
public class MessageTemplateDetailView extends StandardDetailView<MessageTemplate> {
}
