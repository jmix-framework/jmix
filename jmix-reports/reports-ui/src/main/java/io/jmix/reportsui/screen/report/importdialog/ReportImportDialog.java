/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reportsui.screen.report.importdialog;

import io.jmix.reports.ReportImportExport;
import io.jmix.reports.entity.ReportImportOption;
import io.jmix.reports.entity.ReportImportResult;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.*;
import io.jmix.ui.download.DownloadFormat;
import io.jmix.ui.screen.*;
import io.jmix.ui.upload.TemporaryStorage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.UUID;

@UiController("report_ReportImport.dialog")
@UiDescriptor("report-import-dialog.xml")
public class ReportImportDialog extends Screen {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ReportImportDialog.class);
    @Autowired
    protected FileStorageUploadField fileUploadField;
    @Autowired
    protected Label<String> fileName;
    @Autowired
    protected CheckBox importRoles;
    @Autowired
    protected TemporaryStorage temporaryStorage;
    @Autowired
    protected ReportImportExport reportImportExport;
    @Autowired
    protected HBoxLayout dropZone;
    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected ScreenValidation screenValidation;

    @Subscribe
    protected void onInit(InitEvent event) {
        fileUploadField.setMode(FileStorageUploadField.FileStoragePutMode.MANUAL);
        importRoles.setValue(Boolean.TRUE);
    }

    @Subscribe("fileUploadField")
    protected void onFileUploadFileUploadSucceed(SingleFileUploadField.FileUploadSucceedEvent event) {
        fileName.setValue(fileUploadField.getFileName());
    }

    @Subscribe("closeBtn")
    public void onCloseBtnClick(Button.ClickEvent event) {
        closeWithDefaultAction();
    }

    @Subscribe("commitBtn")
    public void onCommitBtnClick(Button.ClickEvent event) {
        ValidationErrors validationErrors = getValidationErrors();

        if (validationErrors.isEmpty()) {
            ReportImportResult result = importReport();

            if (!result.hasErrors()) {
                notifications.create(Notifications.NotificationType.HUMANIZED)
                        .withCaption(messageBundle.formatMessage("importResult",
                                result.getCreatedReports().size(),
                                result.getUpdatedReports().size()))
                        .show();
                close(StandardOutcome.COMMIT);
            } else {
                StringBuilder exceptionTraces = new StringBuilder();
                result.getInnerExceptions().forEach(t -> exceptionTraces.append(t.toString()));

                log.error(exceptionTraces.toString());

                notifications.create(Notifications.NotificationType.ERROR)
                        .withCaption(messageBundle.getMessage("reportException.unableToImportReport"))
                        .withDescription(exceptionTraces.toString())
                .show();
                close(StandardOutcome.CLOSE);
            }
        }
        screenValidation.showValidationErrors(getWindow().getFrameOwner(), validationErrors);
    }

    protected ReportImportResult importReport() {
        ReportImportResult reportImportResult = new ReportImportResult();

        UUID fileId = fileUploadField.getFileId();
        File file = temporaryStorage.getFile(fileId);

        byte[] bytes;
        try {
            bytes = FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            reportImportResult.addException(e);
            return reportImportResult;
        }

        temporaryStorage.deleteFile(fileId);
        return reportImportExport.importReportsWithResult(bytes, getImportOptions());
    }


    @Nullable
    protected EnumSet<ReportImportOption> getImportOptions() {
        if (BooleanUtils.isNotTrue(importRoles.getValue())) {
            return EnumSet.of(ReportImportOption.DO_NOT_IMPORT_ROLES);
        }
        return null;
    }


    protected ValidationErrors getValidationErrors() {
        ValidationErrors errors = screenValidation.validateUiComponents(getWindow());
        if (fileUploadField.getFileId() == null) {
            errors.add(messageBundle.getMessage("reportException.noFile"));
            return errors;
        }
        String extension = FilenameUtils.getExtension(fileUploadField.getFileName());
        if (!StringUtils.equalsIgnoreCase(extension, DownloadFormat.ZIP.getFileExt())) {
            errors.add(messageBundle.formatMessage("reportException.wrongFileType", extension));
        }
        return errors;
    }
}
