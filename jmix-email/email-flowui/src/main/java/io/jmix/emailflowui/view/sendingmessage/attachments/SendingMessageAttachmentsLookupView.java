/*
 * Copyright 2023 Haulmont.
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

package io.jmix.emailflowui.view.sendingmessage.attachments;

import io.jmix.email.entity.SendingAttachment;
import io.jmix.email.entity.SendingMessage;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;

@ViewController("email_sendingMessageAttachmentsLookupView")
@ViewDescriptor("sending-message-attachments-lookup-view.xml")
@DialogMode(width = "50em", height = "37.5em", resizable = true)
@LookupComponent("attachmentsDataGrid")
public class SendingMessageAttachmentsLookupView extends StandardListView<SendingAttachment> {

    @ViewComponent
    protected CollectionLoader<SendingAttachment> sendingAttachmentsDl;

    public void setMessage(SendingMessage message) {
        sendingAttachmentsDl.setParameter("messageId", message.getId());
        sendingAttachmentsDl.load();
    }
}