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

package io.jmix.reportsflowui.view.importtemplatedialog;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import io.jmix.core.FileStorage;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.upload.TemporaryStorage;
import io.jmix.flowui.view.*;
import io.jmix.reports.ReportImportExport;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;

@ViewController("report_ImportTemplateDialogView")
@ViewDescriptor("import-template-dialog-view.xml")
public class ImportTemplateDialogView extends StandardView {

    @Autowired
    protected TemporaryStorage temporaryStorage;
    @Autowired
    protected ReportImportExport reportImportExport;
    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected ViewValidation viewValidation;
    @Autowired
    protected FileStorage fileStorage;
    @Autowired
    protected UiComponents uiComponents;

    @ViewComponent
    private HorizontalLayout uploadBox;

    protected Upload upload;

    public String getFileName() {
        FileBuffer fileBuffer = (FileBuffer) upload.getReceiver();
        return fileBuffer.getFileName();
    }

    public InputStream getFileContent() {
        FileBuffer fileBuffer = (FileBuffer) upload.getReceiver();
        return fileBuffer.getInputStream();
    }

    @Subscribe
    protected void onInit(InitEvent event) {
        createUpload();
    }

    protected void createUpload() {
        upload = uiComponents.create(Upload.class);
        uploadBox.add(upload);

        FileBuffer buffer = new FileBuffer();
        upload.setReceiver(buffer);
        upload.setWidth("100%");
    }

    @Subscribe("closeBtn")
    public void onCloseBtnClick(ClickEvent<Button> event) {
        closeWithDefaultAction();
    }

    @Subscribe("commitBtn")
    public void onCommitBtnClick(ClickEvent<Button> event) {
        ValidationErrors validationErrors = getValidationErrors();

        if (validationErrors.isEmpty()) {
            close(StandardOutcome.SAVE);
        }
        viewValidation.showValidationErrors(validationErrors);
    }

    protected ValidationErrors getValidationErrors() {
        FileBuffer fileBuffer = (FileBuffer) upload.getReceiver();

        ValidationErrors errors = viewValidation.validateUiComponents(this.getContent());
        if (fileBuffer.getFileData() == null) {
            errors.add(messageBundle.getMessage("reportException.noFile"));
            return errors;
        }
//        String extension = FilenameUtils.getExtension(fileBuffer.getFileName());
//        if (!StringUtils.equalsIgnoreCase(extension, DownloadFormat.ZIP.getFileExt())) {
//            errors.add(messageBundle.formatMessage("reportException.wrongFileType", extension));
//        }
        return errors;
    }
}
