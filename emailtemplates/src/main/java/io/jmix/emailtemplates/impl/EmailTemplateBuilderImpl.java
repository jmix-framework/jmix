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


import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.email.EmailException;
import io.jmix.email.EmailInfo;
import io.jmix.email.Emailer;
import io.jmix.emailtemplates.EmailTemplateBuilder;
import io.jmix.emailtemplates.EmailTemplates;
import io.jmix.emailtemplates.dto.ReportWithParams;
import io.jmix.emailtemplates.entity.EmailTemplate;
import io.jmix.emailtemplates.entity.EmailTemplateAttachment;
import io.jmix.emailtemplates.entity.JsonEmailTemplate;
import io.jmix.emailtemplates.entity.TemplateReport;
import io.jmix.emailtemplates.exception.ReportParameterTypeChangedException;
import io.jmix.emailtemplates.exception.TemplateNotFoundException;
import io.jmix.reports.entity.Report;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("emltmp_EmailTemplateBuilder")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EmailTemplateBuilderImpl implements EmailTemplateBuilder {

    protected EmailTemplate emailTemplate;

    protected List<ReportWithParams> reportParams = new ArrayList<>();

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected EmailTemplates emailTemplates;
    @Autowired
    protected Emailer emailer;
    @Autowired
    protected MetadataTools metadataTools;

    public void setEmailTemplate(EmailTemplate emailTemplate) {
        this.emailTemplate = cloneTemplate(emailTemplate);
    }

    @Override
    public EmailTemplateBuilder setSubject(String subject) {
        emailTemplate.setSubject(subject);
        emailTemplate.setUseReportSubject(false);
        return this;
    }

    @Override
    public EmailTemplateBuilder setFrom(String from) {
        emailTemplate.setFrom(from);
        return this;
    }

    @Override
    public EmailTemplateBuilder addTo(String to) {
        String toAddresses = to;
        if (StringUtils.isNotBlank(emailTemplate.getTo())) {
            toAddresses = emailTemplate.getTo() + ", " + to;
        }
        emailTemplate.setTo(toAddresses);
        return this;
    }

    @Override
    public EmailTemplateBuilder setTo(String to) {
        emailTemplate.setTo(to);
        return this;
    }

    @Override
    public EmailTemplateBuilder addCc(String cc) {
        String ccAddresses = cc;
        if (StringUtils.isNotBlank(emailTemplate.getCc())) {
            ccAddresses = emailTemplate.getCc() + ", " + cc;
        }
        emailTemplate.setCc(ccAddresses);
        return this;
    }

    @Override
    public EmailTemplateBuilder setCc(String cc) {
        emailTemplate.setCc(cc);
        return this;
    }

    @Override
    public EmailTemplateBuilder addBcc(String bcc) {
        String bccAddresses = bcc;
        if (StringUtils.isNotBlank(emailTemplate.getBcc())) {
            bccAddresses = emailTemplate.getBcc() + ", " + bcc;
        }
        emailTemplate.setBcc(bccAddresses);
        return this;
    }

    @Override
    public EmailTemplateBuilder setBcc(String bcc) {
        emailTemplate.setBcc(bcc);
        return this;
    }

    @Override
    public EmailTemplateBuilder addAttachmentReport(Report report) {
        TemplateReport templateReport = metadata.create(TemplateReport.class);
        templateReport.setReport(report);
        emailTemplate.getAttachedTemplateReports().add(templateReport);
        return this;
    }

    @Override
    public EmailTemplateBuilder setAttachmentReports(Collection<Report> reports) {
        List<TemplateReport> templateReports = reports.stream().map(r -> {
            TemplateReport templateReport = metadata.create(TemplateReport.class);
            templateReport.setReport(r);
            return templateReport;
        }).collect(Collectors.toList());
        emailTemplate.setAttachedTemplateReports(templateReports);
        return this;
    }


    @Override
    public EmailTemplateBuilder addAttachmentFile(EmailTemplateAttachment file) {
        emailTemplate.getAttachedFiles().add(file);
        return this;
    }

    @Override
    public EmailTemplateBuilder setAttachmentFiles(List<EmailTemplateAttachment> files) {
        emailTemplate.setAttachedFiles(files);
        return this;
    }

    @Override
    public EmailTemplateBuilder setAttachmentParameter(Report report, String key, Object value) {
        ReportWithParams reportWithParams = reportParams.stream()
                .filter(e -> e.getReport().equals(report))
                .findFirst()
                .orElse(null);

        if (reportWithParams == null) {
            reportWithParams = new ReportWithParams(report);
            reportParams.add(reportWithParams);
        }

        reportWithParams.put(key, value);
        return this;
    }

    @Override
    public EmailTemplateBuilder setAttachmentParameters(ReportWithParams reportWithParams) {
        Report report = reportWithParams.getReport();
        ReportWithParams existsParams = reportParams.stream()
                .filter(e -> e.getReport().equals(report))
                .findFirst()
                .orElse(null);

        if (existsParams != null) {
            reportParams.remove(existsParams);
        }
        reportParams.add(reportWithParams);
        return this;
    }

    @Override
    public EmailTemplateBuilder setAttachmentParameters(Report report, Map<String, Object> params) {
        ReportWithParams reportWithParams = new ReportWithParams(report);
        reportWithParams.setParams(params);
        setAttachmentParameters(reportWithParams);
        return this;
    }

    @Override
    public EmailTemplateBuilder setBodyParameter(String key, Object value) {
        TemplateReport emailBodyReport = emailTemplate.getEmailBodyReport();
        ReportWithParams reportWithParams = reportParams.stream()
                .filter(e -> e.getReport().equals(emailBodyReport.getReport()))
                .findFirst()
                .orElse(null);

        if (reportWithParams == null) {
            reportWithParams = new ReportWithParams(emailBodyReport.getReport());
            reportParams.add(reportWithParams);
        }

        reportWithParams.put(key, value);

        return this;
    }

    @Override
    public EmailTemplateBuilder setBodyParameters(Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            setBodyParameter(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public EmailTemplateBuilder setAttachmentParameters(Collection<ReportWithParams> reportsWithParams) {
        TemplateReport emailBodyReport = emailTemplate.getEmailBodyReport();
        ReportWithParams reportWithParams = reportParams.stream()
                .filter(e -> e.getReport().equals(emailBodyReport.getReport()))
                .findFirst()
                .orElse(null);

        reportParams.clear();
        if (reportWithParams != null) {
            reportParams.add(reportWithParams);
        }
        reportParams.addAll(reportsWithParams);
        return this;
    }

    @Override
    public EmailInfo generateEmail() throws ReportParameterTypeChangedException, TemplateNotFoundException {
        return emailTemplates.generateEmail(emailTemplate, reportParams);
    }

    @Override
    public EmailTemplate build() {
        return cloneTemplate(emailTemplate);
    }

    protected EmailTemplate cloneTemplate(EmailTemplate emailTemplate) {
        EmailTemplate clonedTemplate = metadataTools.deepCopy(emailTemplate);
        if (clonedTemplate instanceof JsonEmailTemplate) {
            clonedTemplate.setEmailBodyReport(emailTemplate.getEmailBodyReport());
            ((JsonEmailTemplate) clonedTemplate).setReport(emailTemplate.getReport());
        }
        List<TemplateReport> attachedTemplateReports = new ArrayList<>();
        List<TemplateReport> templateAttachedTemplateReports = emailTemplate.getAttachedTemplateReports();
        if (templateAttachedTemplateReports != null) {
            for (TemplateReport templateReport : templateAttachedTemplateReports) {
                TemplateReport newTemplateReport = metadataTools.deepCopy(templateReport);
                attachedTemplateReports.add(newTemplateReport);
            }
        }
        clonedTemplate.setAttachedTemplateReports(attachedTemplateReports);
        return clonedTemplate;
    }

    @Override
    public void sendEmail() throws TemplateNotFoundException, ReportParameterTypeChangedException, EmailException {
        emailer.sendEmail(generateEmail());
    }

    @Override
    public void sendEmail(boolean async) throws TemplateNotFoundException, ReportParameterTypeChangedException, EmailException {
        if (async) {
            sendEmailAsync();
        } else {
            sendEmail();
        }
    }

    @Override
    public void sendEmailAsync() throws TemplateNotFoundException, ReportParameterTypeChangedException {
        emailer.sendEmailAsync(generateEmail());
    }
}
