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

package io.jmix.aitoolsflowui.view.chathub;


import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

/**
 * Ready-made host view for {@link AiChatHubFragment} — the chat hub landing screen.
 */
@Route(value = "aitls/chats", layout = DefaultMainViewParent.class)
@ViewController(id = "aitls_AiChatHubView")
@ViewDescriptor(path = "ai-chat-hub-view.xml")
public class AiChatHubView extends StandardView {
}