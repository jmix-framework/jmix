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

package io.jmix.flowui.component.upload;

import com.google.common.base.Strings;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadI18N;
import io.jmix.core.Messages;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;

public class JmixUpload extends Upload implements ApplicationContextAware, InitializingBean {

    protected Messages messages;
    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        messages = applicationContext.getBean(Messages.class);

        initI18n();
    }

    protected void initI18n() {
        UploadI18N uploadI18N = new UploadI18N();

        UploadI18N.DropFiles dropFiles = new UploadI18N.DropFiles();
        dropFiles.setOne(messages.getMessage("upload.dropFiles.one"));
        dropFiles.setMany(messages.getMessage("upload.dropFiles.many"));
        uploadI18N.setDropFiles(dropFiles);

        UploadI18N.AddFiles addFiles = new UploadI18N.AddFiles();
        addFiles.setOne(messages.getMessage("upload.addFiles.one"));
        addFiles.setMany(messages.getMessage("upload.addFiles.many"));
        uploadI18N.setAddFiles(addFiles);

        UploadI18N.Error error = new UploadI18N.Error();
        error.setTooManyFiles(messages.getMessage("upload.error.tooManyFiles"));
        error.setFileIsTooBig(messages.getMessage("upload.error.fileIsTooBig"));
        error.setIncorrectFileType(messages.getMessage("upload.error.incorrectFileType"));
        uploadI18N.setError(error);

        UploadI18N.Uploading uploading = new UploadI18N.Uploading();

        UploadI18N.Uploading.Status status = new UploadI18N.Uploading.Status();
        status.setConnecting(messages.getMessage("upload.uploading.status.connecting"));
        status.setStalled(messages.getMessage("upload.uploading.status.stalled"));
        status.setProcessing(messages.getMessage("upload.uploading.status.processing"));
        status.setHeld(messages.getMessage("upload.uploading.status.held"));
        uploading.setStatus(status);

        UploadI18N.Uploading.RemainingTime remainingTime = new UploadI18N.Uploading.RemainingTime();
        remainingTime.setPrefix(messages.getMessage("upload.uploading.remainingTime.prefix"));
        remainingTime.setUnknown(messages.getMessage("upload.uploading.remainingTime.unknown"));
        uploading.setRemainingTime(remainingTime);

        UploadI18N.Uploading.Error uploadingError = new UploadI18N.Uploading.Error();
        uploadingError.setServerUnavailable(messages.getMessage("upload.uploading.error.serverUnavailable"));
        uploadingError.setUnexpectedServerError(messages.getMessage("upload.uploading.error.unexpectedServerError"));
        uploadingError.setForbidden(messages.getMessage("upload.uploading.error.forbidden"));
        uploading.setError(uploadingError);

        uploadI18N.setUploading(uploading);

        UploadI18N.File file = new UploadI18N.File();
        file.setRetry(messages.getMessage("upload.file.retry"));
        file.setStart(messages.getMessage("upload.file.start"));
        file.setRemove(messages.getMessage("upload.file.remove"));
        uploadI18N.setFile(file);

        ArrayList<String> unitsList = new ArrayList<>();

        String[] units = messages.getMessage("upload.units").split("[\\s,]+");
        for (String unit : units) {
            if (!Strings.isNullOrEmpty(unit)) {
                unitsList.add(unit);
            }
        }

        uploadI18N.setUnits(unitsList);

        setI18n(uploadI18N);
    }
}
