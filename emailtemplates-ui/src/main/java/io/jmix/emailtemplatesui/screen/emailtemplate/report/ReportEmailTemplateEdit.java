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

package io.jmix.emailtemplatesui.screen.emailtemplate.report;


import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.core.Metadata;
import io.jmix.emailtemplates.entity.ReportEmailTemplate;
import io.jmix.emailtemplates.entity.TemplateGroup;
import io.jmix.emailtemplates.entity.TemplateReport;
import io.jmix.emailtemplatesui.screen.emailtemplate.parameters.EmailTemplateParametersFragment;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.ui.Fragments;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.EntityComboBox;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.VBoxLayout;
import io.jmix.ui.model.*;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;

@UiController("emltmp_ReportEmailTemplate.edit")
@UiDescriptor("report-email-template-edit.xml")
@EditedEntityContainer("emailTemplateDc")
public class ReportEmailTemplateEdit extends StandardEditor<ReportEmailTemplate> {

    @Autowired
    @Qualifier("defaultGroup.subject")
    private TextField<String> subjectField;

    @Autowired
    private EntityComboBox<Report> emailBody;

    @Autowired
    private Metadata metadata;

    @Autowired
    private Notifications notifications;

    @Autowired
    private MessageBundle messageBundle;

    @Autowired
    protected VBoxLayout bodyDefaultValuesBox;

    @Autowired
    protected Fragments fragments;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected CollectionContainer<Report> emailBodiesDc;

    @Autowired
    protected DataLoader emailBodiesDl;

    protected EmailTemplateParametersFragment bodyParametersFragment;

    @Autowired
    protected InstanceContainer<TemplateReport> emailBodyReportDc;

    @Autowired
    protected DataLoader emailTemplateDl;

    @Autowired
    private EntityStates entityStates;

    @Autowired
    private CollectionLoader<TemplateGroup> groupDl;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent e) {
        emailBodiesDl.load();
        emailTemplateDl.load();
        groupDl.load();

        bodyParametersFragment = fragments.create(this, EmailTemplateParametersFragment.class)
                .setIsDefaultValues(true)
                .setHideReportCaption(true);
        bodyDefaultValuesBox.add(bodyParametersFragment.getFragment());

        bodyParametersFragment.setTemplateReport(getEditedEntity().getEmailBodyReport());

        if (getEditedEntity().getEmailBodyReport() != null) {
            bodyParametersFragment.createComponents();
        } else {
            bodyParametersFragment.clearComponents();
        }

        emailBody.setValue(getEditedEntity().getReport());
        emailBody.addValueChangeListener(reportValueChangeEvent -> updateParametersComponents());

        setSubjectVisibility();
    }

    @Subscribe("useReportSubject")
    protected void useReportSubjectChkBoxValueChange(HasValue.ValueChangeEvent<Boolean> e) {
        setSubjectVisibility();
        if (BooleanUtils.isTrue(e.getValue())) {
            getEditedEntity().setSubject(null);
        }
    }

    private void updateParametersComponents() {
        Report value = emailBody.getValue();
        if (value != null) {
            if (value.getDefaultTemplate() != null) {
                if (ReportOutputType.HTML == value.getDefaultTemplate().getReportOutputType()) {
                    TemplateReport templateReport = metadata.create(TemplateReport.class);
                    templateReport.setParameterValues(new ArrayList<>());
                    templateReport.setReport(value);
                    emailBodyReportDc.setItem(templateReport);

                    getEditedEntity().setEmailBodyReport(templateReport);

                    bodyParametersFragment.setTemplateReport(getEditedEntity().getEmailBodyReport());
                    bodyParametersFragment.createComponents();
                } else {
                    resetTemplateReport();
                    emailBody.setValue(null);
                    notifications.create(Notifications.NotificationType.ERROR)
                            .withDescription(messageBundle.getMessage("notification.reportIsNotHtml"))
                            .show();
                }
            } else {
                resetTemplateReport();
                emailBody.setValue(null);
                notifications.create(Notifications.NotificationType.ERROR)
                        .withDescription(messageBundle.getMessage("notification.reportHasNoDefaultTemplate"))
                        .show();
            }
        } else {
            resetTemplateReport();
        }
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    protected void onPreCommit(DataContext.PreCommitEvent event) {
        TemplateReport templateReport = emailBodyReportDc.getItemOrNull();
        if (entityStates.isNew(getEditedEntity())) {
            if (templateReport != null) {
                event.getSource().merge(templateReport);
            }
        } else {
            ReportEmailTemplate original = dataManager.load(ReportEmailTemplate.class)
                    .id(getEditedEntity().getId())
                    .fetchPlan( "emailTemplate-fetchPlan")
                    .one();
            TemplateReport originalEmailBodyReport = original.getEmailBodyReport();
            if (originalEmailBodyReport != null && !originalEmailBodyReport.equals(templateReport)) {
                event.getSource().remove(originalEmailBodyReport);
            }
        }
    }

    protected void resetTemplateReport() {
        emailBodyReportDc.setItem(null);
        bodyParametersFragment.setTemplateReport(getEditedEntity().getEmailBodyReport());
        bodyParametersFragment.clearComponents();
    }

    public void setSubjectVisibility() {
        subjectField.setVisible(BooleanUtils.isNotTrue(getEditedEntity().getUseReportSubject()));
    }

    public void runReport() {
    }
}