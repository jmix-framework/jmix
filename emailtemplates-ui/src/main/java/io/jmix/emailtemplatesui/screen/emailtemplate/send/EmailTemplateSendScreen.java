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

package io.jmix.emailtemplatesui.screen.emailtemplate.send;


import com.haulmont.yarg.util.converter.ObjectToStringConverter;
import io.jmix.core.CoreProperties;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.email.EmailException;
import io.jmix.email.EmailInfo;
import io.jmix.email.Emailer;
import io.jmix.emailtemplates.EmailTemplates;
import io.jmix.emailtemplates.dto.ReportWithParams;
import io.jmix.emailtemplates.entity.EmailTemplate;
import io.jmix.emailtemplates.entity.ParameterValue;
import io.jmix.emailtemplates.entity.TemplateReport;
import io.jmix.emailtemplates.exception.ReportParameterTypeChangedException;
import io.jmix.emailtemplates.exception.TemplateNotFoundException;
import io.jmix.emailtemplatesui.screen.emailtemplate.parameters.EmailTemplateParametersFragment;
import io.jmix.reports.ParameterClassResolver;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.exception.ReportParametersValidationException;
import io.jmix.reportsui.screen.ReportParameterValidator;
import io.jmix.ui.*;
import io.jmix.ui.component.*;
import io.jmix.ui.download.ByteArrayDataProvider;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@UiController("emltmp_EmailTemplateSendScreen")
@UiDescriptor("email-template-send-screen.xml")
public class EmailTemplateSendScreen extends Screen {

    private final static Charset PREVIEW_CHARSET = StandardCharsets.UTF_16;

    @WindowParam
    private EmailTemplate emailTemplate;

    @Autowired
    private InstanceContainer<EmailTemplate> emailTemplateDc;

    @Autowired
    private VBoxLayout defaultBodyParameters;
    protected EmailTemplateParametersFragment emailTemplateParametersFragment;

    @Autowired
    private VBoxLayout attachmentParameters;
    protected EmailTemplateParametersFragment attachmentParametersFragment;

    @Autowired
    private GroupBoxLayout attachmentGroupBox;

    @Autowired
    protected ReportParameterValidator reportParameterValidator;
    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected UiScreenProperties screenProperties;
    @Autowired
    protected EmailTemplates emailTemplates;

    @Autowired
    private Downloader downloader;

    @Autowired
    protected Emailer emailer;

    @Autowired
    protected CoreProperties coreProperties;

    @Autowired
    private Metadata metadata;

    @Autowired
    private Fragments fragments;

    @Autowired
    private Notifications notifications;

    @Autowired
    private Messages messages;

    @Autowired
    protected ObjectToStringConverter objectToStringConverter;

    @Autowired
    protected ParameterClassResolver classResolver;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected ScreenValidation screenValidation;

    @Autowired
    @Qualifier("defaultGroup.subject")
    private TextField<String> subjectField;

    public EmailTemplate getEmailTemplate() {
        return emailTemplate;
    }

    public void setEmailTemplate(EmailTemplate emailTemplate) {
        this.emailTemplate = emailTemplate;
    }

    @Subscribe
    public void onAfterInit(AfterInitEvent event) {
        if (emailTemplate == null) {
            throw new IllegalStateException("'emailTemplate' parameter is required");
        }

        emailTemplate = dataManager.load(emailTemplate.getClass())
                .id(emailTemplate.getId())
                .fetchPlan("emailTemplate-fetchPlan")
                .one();

        emailTemplateDc.setItem(emailTemplate);

        Map<String, Object> params = ((MapScreenOptions) event.getOptions()).getParams();
        setParameters(params);

        emailTemplateParametersFragment = fragments.create(this, EmailTemplateParametersFragment.class)
                .setTemplateReport(emailTemplate.getEmailBodyReport())
                .setHideReportCaption(true)
                .createComponents();
        defaultBodyParameters.add(emailTemplateParametersFragment.getFragment());

        if (CollectionUtils.isNotEmpty(emailTemplate.getAttachedTemplateReports())) {
            attachmentParametersFragment = fragments.create(this, EmailTemplateParametersFragment.class)
                    .setTemplateReports(emailTemplate.getAttachedTemplateReports())
                    .createComponents();
            attachmentParameters.add(attachmentParametersFragment.getFragment());
        } else {
            attachmentGroupBox.setVisible(false);
        }

    }

    @Subscribe("subject")
    protected void subjectFileValueChangeEvent(HasValue.ValueChangeEvent<String> e) {
        if (!StringUtils.equals(e.getPrevValue(), e.getValue())) {
            emailTemplate.setUseReportSubject(false);
        }
    }

    public void setParameters(Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String alias = entry.getKey();
            Object value = entry.getValue();
            setParameter(alias, value);
        }
    }

    public void setParameter(String alias, Object value) {
        List<TemplateReport> templateReports = new ArrayList<>();
        if (emailTemplate.getEmailBodyReport() != null) {
            templateReports.add(emailTemplate.getEmailBodyReport());
        }
        templateReports.addAll(emailTemplate.getAttachedTemplateReports());
        for (TemplateReport templateReport : templateReports) {
            ReportInputParameter inputParameter = templateReport.getReport().getInputParameters().stream()
                    .filter(e -> alias.equals(e.getAlias()))
                    .findFirst()
                    .orElse(null);
            if (inputParameter != null) {
                ParameterValue parameterValue = templateReport.getParameterValues().stream()
                        .filter(pv -> pv.getAlias().equals(alias))
                        .findFirst()
                        .orElse(null);
                if (parameterValue == null) {
                    parameterValue = metadata.create(ParameterValue.class);
                    parameterValue.setAlias(alias);
                    parameterValue.setParameterType(inputParameter.getType());
                    parameterValue.setTemplateReport(templateReport);
                    templateReport.getParameterValues().add(parameterValue);
                }
                Class parameterClass = classResolver.resolveClass(inputParameter);
                if (!ParameterType.ENTITY_LIST.equals(inputParameter.getType())) {
                    String stringValue = objectToStringConverter.convertToString(parameterClass, value);
                    parameterValue.setDefaultValue(stringValue);
                }
            }
        }
    }


    protected boolean validateAll() {
        ValidationErrors validationErrors = screenValidation.validateUiComponents(getWindow());

        if (!validationErrors.isEmpty()) {
            screenValidation.showValidationErrors(this, validationErrors);
        } else {
            return crossValidateParameters();
        }

        return false;
    }

    protected boolean crossValidateParameters() {
        boolean isValid = true;
        isValid = crossValidateParameters(emailTemplateParametersFragment);
        if (isValid) {
            isValid = crossValidateParameters(attachmentParametersFragment);
        }

        return isValid;
    }

    private boolean crossValidateParameters(EmailTemplateParametersFragment parametersFrame) {
        boolean isValid = true;
        if (parametersFrame != null && parametersFrame.collectParameters() != null) {
            for (ReportWithParams reportWithParams : parametersFrame.collectParameters()) {
                if (BooleanUtils.isTrue(reportWithParams.getReport().getValidationOn())) {
                    try {
                        reportParameterValidator.crossValidateParameters(reportWithParams.getReport(),
                                reportWithParams.getParams());
                    } catch (ReportParametersValidationException e) {
                        Notifications.NotificationType notificationType = Notifications.NotificationType.valueOf(
                                screenProperties.getValidationNotificationType()
                        );
                        notifications.create(notificationType)
                                .withCaption(messages.getMessage("validationFail.caption"))
                                .withDescription(e.getMessage())
                                .show();
                        isValid = false;
                    }
                }
            }
        }
        return isValid;
    }

    @Subscribe("cancelButton")
    public void onCancelButtonClick(Button.ClickEvent event) {
        close(FrameOwner.WINDOW_CLOSE_ACTION);
    }

    @Subscribe("previewButton")
    public void onPreviewButtonClick(Button.ClickEvent e) throws TemplateNotFoundException, ReportParameterTypeChangedException {
        if (!validateAll()) {
            return;
        }
        EmailInfo emailInfo = getEmailInfo();
        downloader.download(new ByteArrayDataProvider(emailInfo.getBody().getBytes(PREVIEW_CHARSET),
                uiProperties.getSaveExportedByteArrayDataThresholdBytes(),
                coreProperties.getTempDir()), emailInfo.getSubject() + ".html");
    }

    @Subscribe("sendButton")
    public void onSendButtonClick(Button.ClickEvent e) throws TemplateNotFoundException, ReportParameterTypeChangedException {
        if (!validateAll()) {
            return;
        }
        if (BooleanUtils.isNotTrue(emailTemplate.getUseReportSubject()) && subjectField.getValue() == null) {
            notifications.create(Notifications.NotificationType.WARNING)
                    .withDescription(messages.getMessage(EmailTemplateSendScreen.class, "emptySubject"))
                    .show();
            return;
        }
        EmailInfo emailInfo = getEmailInfo();

        try {
            emailer.sendEmail(emailInfo);
            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withDescription(messages.getMessage(EmailTemplateSendScreen.class, "emailSent"))
                    .show();
            close(WINDOW_COMMIT_AND_CLOSE_ACTION);
        } catch (EmailException exception) {
            notifications.create(Notifications.NotificationType.ERROR)
                    .withDescription(StringUtils.join(exception.getMessages(), "\n"))
                    .show();
        }
    }

    private EmailInfo getEmailInfo() throws ReportParameterTypeChangedException, TemplateNotFoundException {
        return emailTemplates.generateEmail(emailTemplate, new ArrayList<>());
    }
}