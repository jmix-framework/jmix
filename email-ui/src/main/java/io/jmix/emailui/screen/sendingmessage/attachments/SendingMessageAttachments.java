/*
 * Copyright 2020 Haulmont.
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

package io.jmix.emailui.screen.sendingmessage.attachments;

import io.jmix.email.entity.SendingAttachment;
import io.jmix.email.entity.SendingMessage;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("email_SendingMessage.attachments")
@UiDescriptor("sending-message-attachments.xml")
@LookupComponent("table")
public class SendingMessageAttachments extends StandardLookup<SendingAttachment> {

    protected SendingMessage message;

    @Autowired
    protected CollectionLoader<SendingAttachment> sendingAttachmentsDl;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        sendingAttachmentsDl.setParameter("messageId", message.getId());
        sendingAttachmentsDl.load();
    }

    public void setMessage(SendingMessage message) {
        this.message = message;
    }
}