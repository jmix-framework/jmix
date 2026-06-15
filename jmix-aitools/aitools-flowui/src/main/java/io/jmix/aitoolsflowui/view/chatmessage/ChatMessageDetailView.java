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

package io.jmix.aitoolsflowui.view.chatmessage;

import com.vaadin.flow.router.Route;
import io.jmix.aitools.entity.ChatMessage;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "aitls/chat-messages/:id", layout = DefaultMainViewParent.class)
@ViewController("aitls_ChatMessage.detail")
@ViewDescriptor("chat-message-detail-view.xml")
@EditedEntityContainer("chatMessageDc")
@DialogMode(width = "32em", resizable = true)
public class ChatMessageDetailView extends StandardDetailView<ChatMessage> {
}
