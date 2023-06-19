package io.jmix.reportsflowui.view.importdialog;


import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.FileBuffer;
import io.jmix.core.FileStorage;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.validation.ValidationErrors;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.upload.TemporaryStorage;
import io.jmix.flowui.view.*;
import io.jmix.reports.ReportImportExport;
import io.jmix.reports.entity.ReportImportOption;
import io.jmix.reports.entity.ReportImportResult;
import jakarta.annotation.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.EnumSet;

@ViewController("report_ReportImportDialogView")
@ViewDescriptor("report-import-view.xml")
@DialogMode(width = "30em")
public class ReportImportDialogView extends StandardView {

    private static final Logger log = LoggerFactory.getLogger(ReportImportDialogView.class);

    @ViewComponent
    protected JmixCheckbox importRoles;
    @ViewComponent
    protected HorizontalLayout uploadBox;

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

    protected Upload upload;

    @Subscribe
    protected void onInit(InitEvent event) {
        importRoles.setValue(Boolean.TRUE);

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
            ReportImportResult result = importReport();

            if (!result.hasErrors()) {
                notifications.create(
                                messageBundle.formatMessage("importResult",
                                        result.getCreatedReports().size(),
                                        result.getUpdatedReports().size()))
                        .withType(Notifications.Type.SUCCESS)
                        .withPosition(Notification.Position.TOP_END)
                        .show();
                close(StandardOutcome.SAVE);
            } else {
                StringBuilder exceptionTraces = new StringBuilder();
                result.getInnerExceptions().forEach(t -> exceptionTraces.append(t.toString()));

                log.error(exceptionTraces.toString());

                notifications.create(messageBundle.getMessage("reportException.unableToImportReport"),
                                exceptionTraces.toString())
                        .withType(Notifications.Type.ERROR)
                        .show();
                close(StandardOutcome.CLOSE);
            }
        }
        viewValidation.showValidationErrors(validationErrors);
    }

    protected ReportImportResult importReport() {
        ReportImportResult reportImportResult = new ReportImportResult();

        byte[] bytes;
        try {
            FileBuffer fileBuffer = (FileBuffer) upload.getReceiver();
            bytes = fileBuffer.getInputStream().readAllBytes();
        } catch (IOException e) {
            reportImportResult.addException(e);
            return reportImportResult;
        }

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
        FileBuffer fileBuffer = (FileBuffer) upload.getReceiver();

        ValidationErrors errors = viewValidation.validateUiComponents(this.getContent());
        if (fileBuffer.getFileData() == null) {
            errors.add(messageBundle.getMessage("reportException.noFile"));
            return errors;
        }
        String extension = FilenameUtils.getExtension(fileBuffer.getFileName());
        if (!StringUtils.equalsIgnoreCase(extension, DownloadFormat.ZIP.getFileExt())) {
            errors.add(messageBundle.formatMessage("reportException.wrongFileType", extension));
        }
        return errors;
    }
}