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

package io.jmix.emailtemplates.impl;

import com.google.common.io.Files;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.email.EmailAttachment;
import io.jmix.email.EmailInfo;
import io.jmix.email.EmailInfoBuilder;
import io.jmix.emailtemplates.EmailTemplateBuilder;
import io.jmix.emailtemplates.EmailTemplates;
import io.jmix.emailtemplates.TemplateConverter;
import io.jmix.emailtemplates.TemplateParametersExtractor;
import io.jmix.emailtemplates.dto.ReportWithParams;
import io.jmix.emailtemplates.entity.*;
import io.jmix.emailtemplates.exception.ReportParameterTypeChangedException;
import io.jmix.emailtemplates.exception.TemplateNotFoundException;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.runner.ReportRunContext;
import io.jmix.reports.runner.ReportRunner;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component("emltmp_EmailTemplates")
public class EmailTemplatesImpl implements EmailTemplates {

    private static final Logger log = LoggerFactory.getLogger(EmailTemplatesImpl.class);

    @Autowired
    private DataManager dataManager;

    @Autowired
    protected ReportRunner reportRunner;

    @Autowired
    private Messages messages;

    @Autowired
    protected FileStorageLocator fileStorageLocator;

    private FileStorage fileStorage;

    @Autowired
    private TemplateParametersExtractor parametersExtractor;

    @Autowired
    protected TemplateConverter templateConverter;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected Metadata metadata;

    @Override
    public EmailInfo generateEmail(EmailTemplate emailTemplate, Collection<ReportWithParams> params)
            throws TemplateNotFoundException, ReportParameterTypeChangedException {
        if (emailTemplate == null) {
            throw new TemplateNotFoundException(messages.getMessage(EmailTemplates.class, "nullTemplate"));
        }
        List<ReportWithParams> parameters = new ArrayList<>(params);

        TemplateReport bodyReport = emailTemplate.getEmailBodyReport();
        ReportWithParams bodyReportWithParams = bodyReport != null ? getReportWithParams(bodyReport, parameters) : null;

        Map<TemplateReport, ReportWithParams> attachmentsWithParams = new HashMap<>();
        List<TemplateReport> attachedTemplateReports = emailTemplate.getAttachedTemplateReports();
        if (attachedTemplateReports != null) {
            for (TemplateReport templateReport : attachedTemplateReports) {
                ReportWithParams reportWithParams = getReportWithParams(templateReport, parameters);
                attachmentsWithParams.put(templateReport, reportWithParams);
            }
        }
        EmailInfo emailInfo = generateEmailInfoWithoutAttachments(bodyReportWithParams);
        List<EmailAttachment> templateAttachments = new ArrayList<>();
        templateAttachments.addAll(createReportAttachments(attachmentsWithParams));
        templateAttachments.addAll(createFilesAttachments(emailTemplate.getAttachedFiles()));

        emailInfo.setSubject(Boolean.TRUE.equals(emailTemplate.getUseReportSubject()) ?
                emailInfo.getSubject() : emailTemplate.getSubject());
        emailInfo.setAddresses(emailTemplate.getTo());
        emailInfo.setCc(emailTemplate.getCc());
        emailInfo.setBcc(emailTemplate.getBcc());
        emailInfo.setFrom(emailTemplate.getFrom());
        emailInfo.setAttachments(templateAttachments);

        return emailInfo;
    }

    private ReportWithParams getReportWithParams(TemplateReport templateReport, List<ReportWithParams> parameters)
            throws ReportParameterTypeChangedException {
        ReportWithParams bodyReportWithParams = parametersExtractor.getReportDefaultValues(templateReport.getReport(),
                templateReport.getParameterValues());
        ReportWithParams bodyReportExternalParams = parameters.stream()
                .filter(e -> e.getReport().equals(templateReport.getReport()))
                .findFirst()
                .orElse(null);
        if (bodyReportExternalParams != null) {
            for (String key : bodyReportExternalParams.getParams().keySet()) {
                bodyReportWithParams.put(key, bodyReportExternalParams.getParams().get(key));
            }
        }
        return bodyReportWithParams;
    }

    private List<EmailAttachment> createFilesAttachments(List<EmailTemplateAttachment> attachedFiles) {
        List<EmailAttachment> attachmentsList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(attachedFiles)) {
            attachmentsList = attachedFiles.stream()
                    .map(this::createEmailAttachment)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        }
        return attachmentsList;
    }

    private EmailAttachment createEmailAttachment(EmailTemplateAttachment attachment) {
        try {
            if (fileStorage == null) {
                fileStorage = fileStorageLocator.getDefault();
            }
            byte[] bytes = IOUtils.toByteArray(fileStorage.openStream(attachment.getContentFile()));

            return new EmailAttachment(bytes, attachment.getName(), attachment.getName());
        } catch (FileStorageException | IOException e) {
            log.error("Could not load file from storage", e);
        }
        return null;
    }

    @Override
    public EmailInfo generateEmail(EmailTemplate emailTemplate, Map<String, Object> params)
            throws TemplateNotFoundException, ReportParameterTypeChangedException {
        if (emailTemplate == null) {
            throw new TemplateNotFoundException(messages.getMessage(EmailTemplates.class, "nullTemplate"));
        }

        List<ReportWithParams> paramList = new ArrayList<>();
        Report bodyReport = emailTemplate.getReport();
        if (bodyReport == null && emailTemplate instanceof JsonEmailTemplate) {
            bodyReport = templateConverter.convertToReport((JsonEmailTemplate) emailTemplate);
        }
        paramList.add(createParamsMapForReport(bodyReport, params));
        for (TemplateReport templateReport : emailTemplate.getAttachedTemplateReports()) {
            paramList.add(createParamsMapForReport(templateReport.getReport(), params));
        }
        return generateEmail(emailTemplate, paramList);
    }

    @Override
    public EmailInfo generateEmail(String emailTemplateCode, Map<String, Object> params)
            throws TemplateNotFoundException, ReportParameterTypeChangedException {
        EmailTemplate emailTemplate = getEmailTemplateByCode(emailTemplateCode);
        return generateEmail(emailTemplate, params);
    }

    @Override
    public EmailInfo generateEmail(String emailTemplateCode, Collection<ReportWithParams> params)
            throws TemplateNotFoundException, ReportParameterTypeChangedException {
        EmailTemplate emailTemplate = getEmailTemplateByCode(emailTemplateCode);
        return generateEmail(emailTemplate, params);
    }

    protected EmailTemplate getEmailTemplateByCode(String emailTemplateCode) throws TemplateNotFoundException {
        EmailTemplate emailTemplate = dataManager.load(EmailTemplate.class)
                .query("select e from emltmp_EmailTemplate e where e.code = :code")
                .parameter("code", emailTemplateCode)
                .optional()
                .orElse(null);

        if (emailTemplate != null) {
            MetaClass metaClass = metadata.getClass(emailTemplate);
            emailTemplate = (EmailTemplate) dataManager.load(metaClass.getJavaClass())
                    .query("select e from " + metaClass.getName() + " e where e.code = :code")
                    .parameter("code", emailTemplateCode)
                    .fetchPlan("emailTemplate-fetchPlan")
                    .optional()
                    .orElse(null);
        } else {
            throw new TemplateNotFoundException(messages.formatMessage(EmailTemplates.class, "notFoundTemplate", emailTemplateCode));
        }

        return emailTemplate;
    }

    @Override
    public void checkParameterTypeChanged(ReportInputParameter inputParameter, ParameterValue parameterValue)
            throws ReportParameterTypeChangedException {
        if (!Objects.equals(inputParameter.getType(), parameterValue.getParameterType())) {
            throw new ReportParameterTypeChangedException(
                    messages.formatMessage(EmailTemplates.class, "parameterTypeChanged",
                            inputParameter.getReport().getName(), inputParameter.getAlias()));
        }
    }

    @Override
    public EmailTemplateBuilder buildFromTemplate(EmailTemplate emailTemplate) {
        return getEmailTemplateBuilder(emailTemplate);
    }

    @Override
    public EmailTemplateBuilder buildFromTemplate(String code) throws TemplateNotFoundException {
        return getEmailTemplateBuilder(getEmailTemplateByCode(code));
    }

    protected EmailTemplateBuilder getEmailTemplateBuilder(EmailTemplate emailTemplate) {
        EmailTemplateBuilder templateBuilder = applicationContext.getBean(EmailTemplateBuilder.class);
        templateBuilder.setEmailTemplate(emailTemplate);
        return templateBuilder;
    }

    protected EmailInfo generateEmailInfoWithoutAttachments(ReportWithParams reportWithParams) {
        String body = "";
        String subject = "";
        if (reportWithParams != null && reportWithParams.getReport() != null) {
            ReportOutputDocument outputDocument = reportRunner.run(new ReportRunContext(reportWithParams.getReport()).setParams(reportWithParams.getParams()));
            body = new String(outputDocument.getContent(), UTF_8);
            subject = outputDocument.getDocumentName();
        }

        return EmailInfoBuilder.create()
                .setSubject(subject)
                .setBody(body)
                .setBodyContentType(EmailInfo.HTML_CONTENT_TYPE)
                .build();
    }

    protected List<EmailAttachment> createReportAttachments(Map<TemplateReport, ReportWithParams> reportsWithParams) {
        List<EmailAttachment> attachmentsList = new ArrayList<>();
        for (Map.Entry<TemplateReport, ReportWithParams> entry : reportsWithParams.entrySet()) {
            TemplateReport templateReport = entry.getKey();
            ReportWithParams reportWithParams = entry.getValue();
            EmailAttachment emailAttachment = createEmailAttachment(templateReport.getName(), reportWithParams);
            attachmentsList.add(emailAttachment);
        }
        return attachmentsList;
    }

    protected EmailAttachment createEmailAttachment(String templateName, ReportWithParams reportWithParams) {
        ReportOutputDocument outputDocument = reportRunner.run(new ReportRunContext(reportWithParams.getReport()).setParams(reportWithParams.getParams()));
        String fileName = outputDocument.getDocumentName();
        if (StringUtils.isNotBlank(templateName)) {
            String extension = Files.getFileExtension(templateName);
            String docExtension = Files.getFileExtension(fileName);
            if (StringUtils.isNotBlank(extension)) {
                fileName = templateName;
            } else if (StringUtils.isNotBlank(docExtension)) {
                fileName = templateName + "." + docExtension;
            } else {
                fileName = templateName;
            }
        }
        return new EmailAttachment(outputDocument.getContent(), fileName);
    }

    protected ReportWithParams createParamsMapForReport(Report report, Map<String, Object> params) {
        ReportWithParams reportWithParams = new ReportWithParams(report);
        if (MapUtils.isNotEmpty(params)) {
            Map<String, Object> paramsMap = new HashMap<>();
            for (ReportInputParameter parameter : report.getInputParameters()) {
                paramsMap.put(parameter.getAlias(), params.get(parameter.getAlias()));
            }
            reportWithParams.setParams(paramsMap);
        }
        return reportWithParams;
    }
}
