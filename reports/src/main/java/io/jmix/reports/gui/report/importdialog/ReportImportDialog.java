/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.reports.gui.report.importdialog;

import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.CheckBox;
import com.haulmont.cuba.gui.components.FileUploadField;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.export.ExportFormat;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import io.jmix.reports.app.service.ReportService;
import io.jmix.reports.entity.ReportImportOption;
import io.jmix.reports.entity.ReportImportResult;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.HBoxLayout;
import io.jmix.ui.component.ValidationErrors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

public class ReportImportDialog extends AbstractWindow {
    @Autowired
    protected FileUploadField fileUpload;
    @Autowired
    protected Label fileName;
    @Autowired
    protected CheckBox importRoles;
    @Autowired
    protected FileUploadingAPI fileUploadingApi;
    @Autowired
    protected ReportService reportService;
    @Autowired
    protected HBoxLayout dropZone;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        initWindowActions();
        fileUpload.addFileUploadSucceedListener(e -> {
            fileName.setValue(fileUpload.getFileName());
        });
        importRoles.setValue(Boolean.TRUE);

        dropZone.setVisible(false);
    }

    protected void initWindowActions() {
        addAction(new AbstractAction("windowCommit") {
            @Override
            public void actionPerform(Component component) {
                if (validateAll()) {
                    importReport();
                    close(COMMIT_ACTION_ID);
                }
            }

            @Override
            public String getCaption() {
                return messages.getMainMessage("actions.Ok");
            }
        });
        addAction(new AbstractAction("windowClose") {
            @Override
            public void actionPerform(Component component) {
                close(CLOSE_ACTION_ID);
            }

            @Override
            public String getCaption() {
                return messages.getMainMessage("actions.Cancel");
            }
        });
    }

    protected void importReport() {
        try {
            UUID fileID = fileUpload.getFileId();
            File file = fileUploadingApi.getFile(fileID);
            byte[] bytes = FileUtils.readFileToByteArray(file);
            fileUploadingApi.deleteFile(fileID);
            ReportImportResult result = reportService.importReportsWithResult(bytes, getImportOptions());
            showNotification(formatMessage("importResult",
                    result.getCreatedReports().size(), result.getUpdatedReports().size()),
                    NotificationType.HUMANIZED);
        } catch (Exception e) {
            String msg = getMessage("reportException.unableToImportReport");
            showNotification(msg, e.toString(), NotificationType.ERROR);
        }
    }

    protected EnumSet<ReportImportOption> getImportOptions() {
        if (BooleanUtils.isNotTrue(importRoles.getValue())) {
            return EnumSet.of(ReportImportOption.DO_NOT_IMPORT_ROLES);
        }
        return null;
    }

    @Override
    protected void postValidate(ValidationErrors errors) {
        super.postValidate(errors);
        if (fileUpload.getFileId() == null) {
            errors.add(getMessage("reportException.noFile"));
            return;
        }
        String extension = FilenameUtils.getExtension(fileUpload.getFileName());
        if (!StringUtils.equalsIgnoreCase(extension, ExportFormat.ZIP.getFileExt())) {
            errors.add(formatMessage("reportException.wrongFileType", extension));
        }
    }
}
