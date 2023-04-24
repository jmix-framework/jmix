/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component.upload;

import com.vaadin.flow.component.upload.UploadI18N;
import io.jmix.core.Messages;
import io.jmix.flowui.kit.component.upload.JmixUploadI18N;
import org.springframework.stereotype.Component;

/**
 * Class provides helper methods to work with {@link JmixUploadI18N}.
 */
@Component("flowui_UploadFieldI18NSupport")
public class UploadFieldI18NSupport {

    protected Messages messages;

    public UploadFieldI18NSupport(Messages messages) {
        this.messages = messages;
    }

    /**
     * @return i18n object that contains default localizations for {@link FileUploadField}
     */
    public JmixUploadI18N getI18nUploadField() {
        return getI18nDefaults("fileUploadField");
    }

    /**
     * @return i18n object that contains default localizations for {@link FileStorageUploadField}
     */
    public JmixUploadI18N getI18nFileStorageUploadField() {
        return getI18nDefaults("fileStorageUploadField");
    }

    protected JmixUploadI18N getI18nDefaults(String componentName) {
        JmixUploadI18N jmixUploadI18N = new JmixUploadI18N();

        JmixUploadI18N.UploadDialog uploadDialog = new JmixUploadI18N.UploadDialog();
        uploadDialog.setTitle(messages.getMessage(componentName + ".uploadDialog.title"));
        uploadDialog.setCancel(messages.getMessage(componentName + ".uploadDialog.cancel.text"));
        jmixUploadI18N.setUploadDialog(uploadDialog);

        UploadI18N.Error error = new UploadI18N.Error();
        error.setFileIsTooBig(messages.getMessage(componentName + ".error.fileTooBig"));
        error.setIncorrectFileType(messages.getMessage(componentName + ".error.incorrectFileType"));
        jmixUploadI18N.setError(error);

        UploadI18N.Uploading uploading = new UploadI18N.Uploading();

        UploadI18N.Uploading.Status status = new UploadI18N.Uploading.Status();
        status.setConnecting(messages.getMessage(componentName + ".status.connecting"));
        status.setProcessing(messages.getMessage(componentName + ".status.processing"));
        uploading.setStatus(status);

        UploadI18N.Uploading.RemainingTime remainingTime = new UploadI18N.Uploading.RemainingTime();
        remainingTime.setPrefix(messages.getMessage(componentName + ".remainingTime"));
        remainingTime.setUnknown(messages.getMessage(componentName + ".remainingTime.unknown"));

        uploading.setRemainingTime(remainingTime);

        jmixUploadI18N.setUploading(uploading);

        return jmixUploadI18N;
    }
}
