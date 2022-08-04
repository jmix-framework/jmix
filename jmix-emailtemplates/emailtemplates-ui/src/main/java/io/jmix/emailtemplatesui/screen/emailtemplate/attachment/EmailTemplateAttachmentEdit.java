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

package io.jmix.emailtemplatesui.screen.emailtemplate.attachment;

import io.jmix.core.*;
import io.jmix.emailtemplates.entity.EmailTemplateAttachment;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.FileUploadField;
import io.jmix.ui.component.SingleFileUploadField;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;

@UiController("emltmp_EmailTemplateAttachment.edit")
@UiDescriptor("email-template-attachment-edit.xml")
@EditedEntityContainer("emailTemplateAttachmentDc")
public class EmailTemplateAttachmentEdit extends StandardEditor<EmailTemplateAttachment> {

    public static final Logger log = LoggerFactory.getLogger(EmailTemplateAttachmentEdit.class);

    @Autowired
    protected FileUploadField uploadField;

    @Autowired
    protected InstanceContainer<EmailTemplateAttachment> emailTemplateAttachmentDc;

    @Autowired
    protected EntityStates entityStates;

    @Autowired
    protected FileStorageLocator fileStorageLocator;

    protected FileStorage fileStorage;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected MessageBundle messageBundle;

    @Autowired
    protected Button commitAndCloseBtn;

    protected boolean needSave;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        if (entityStates.isNew(getEditedEntity())) {
            commitAndCloseBtn.setEnabled(false);
        } else {
            uploadField.setVisible(false);
        }
    }

    @Subscribe
    protected void onBeforeCommitChanges(BeforeCommitChangesEvent e) {
        if (needSave) {
            FileRef fileRef = saveFile();
            getEditedEntity().setContentFile(fileRef);
        }
    }

    @Subscribe("uploadField")
    protected void uploadFieldOnFileUploadSucceedEvent(SingleFileUploadField.FileUploadSucceedEvent event) {
        EmailTemplateAttachment attachment = getEditedEntity();
        attachment.setName(event.getFileName());
        commitAndCloseBtn.setEnabled(true);
        needSave = true;
    }

    protected FileRef saveFile() {
        try {
            if (fileStorage == null) {
                fileStorage = fileStorageLocator.getDefault();
            }
            return fileStorage.saveStream(getEditedEntity().getName(), new ByteArrayInputStream(uploadField.getValue()));
        } catch (FileStorageException e) {
            notifications.create(Notifications.NotificationType.ERROR)
                    .withDescription(messageBundle.getMessage("unableToSaveAttachment"))
                    .show();
            log.error("Unable to save attachment file to file storage", e);
        }
        return null;
    }
}