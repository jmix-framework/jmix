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

package io.jmix.emailtemplatesui.screen.emailtemplate.attachment;


import io.jmix.core.DataManager;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;
import io.jmix.emailtemplates.entity.EmailTemplate;
import io.jmix.emailtemplates.entity.EmailTemplateAttachment;
import io.jmix.emailtemplates.entity.TemplateReport;
import io.jmix.emailtemplatesui.screen.emailtemplate.parameters.EmailTemplateParametersFragment;
import io.jmix.reports.entity.Report;
import io.jmix.ui.Actions;
import io.jmix.ui.Fragments;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ItemTrackingAction;
import io.jmix.ui.component.Form;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.VBoxLayout;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.download.FileDataProvider;
import io.jmix.ui.model.CollectionPropertyContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@UiController("emltmp_EmailTemplateAttachmentsFragment")
@UiDescriptor("email-template-attachments-fragment.xml")
public class EmailTemplateAttachmentsFragment extends ScreenFragment {

    @Autowired
    protected Fragments fragments;

    @Autowired
    protected InstanceContainer<EmailTemplate> emailTemplateDc;

    @Autowired
    protected Table<EmailTemplateAttachment> filesTable;

    @Autowired
    protected VBoxLayout defaultValuesBox;

    @Autowired
    protected Form attachmentGroup;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected Downloader downloader;

    @Autowired
    protected FileStorageLocator fileStorageLocator;

    protected FileStorage fileStorage;

    protected EmailTemplateParametersFragment parametersFragment;

    @Autowired
    protected Actions actions;

    @Autowired
    private ScreenBuilders screenBuilders;

    @Autowired
    private CollectionPropertyContainer<TemplateReport> attachedReportsDc;

    @Subscribe
    public void onAttachEvent(AttachEvent e) {
        Action downloadAction = actions.create(ItemTrackingAction.class, "download")
                .withHandler(event -> downloadAttachment());
        filesTable.addAction(downloadAction);

        parametersFragment = fragments.create(this, EmailTemplateParametersFragment.class)
                .setHideReportCaption(true)
                .setIsDefaultValues(true)
                .createComponents();
        defaultValuesBox.add(parametersFragment.getFragment());
    }

    @Subscribe(id = "attachedReportsDc", target = Target.DATA_CONTAINER)
    protected void attachedReportsDcOnItemChangeEvent(InstanceContainer.ItemChangeEvent<TemplateReport> e) {
        TemplateReport templateReport = e.getItem();
        if (templateReport != null) {
            parametersFragment.setTemplateReport(templateReport);
            parametersFragment.createComponents();
            attachmentGroup.setVisible(true);
        } else {
            parametersFragment.clearComponents();
            attachmentGroup.setVisible(false);
        }
    }

    @Subscribe("reportsTable.add")
    protected void onReportsTableAdd(Action.ActionPerformedEvent event) {
        screenBuilders.lookup(Report.class, this)
                .withSelectHandler(reports -> {
                    List<TemplateReport> attachedReports =  reports.stream()
                            .map(this::createTemplateReport)
                            .collect(Collectors.toList());
                    attachedReportsDc.getMutableItems().addAll(attachedReports);
                })
                .build()
                .show();
    }

    protected TemplateReport createTemplateReport(Report report) {
        Report reloaded = reloadReport(report);
        TemplateReport templateReport = dataManager.create(TemplateReport.class);
        templateReport.setReport(reloaded);
        templateReport.setEmailTemplate(emailTemplateDc.getItem());
        templateReport.setParameterValues(new ArrayList<>());
        return templateReport;
    }

    private Report reloadReport(Report report) {
        return dataManager.load(Report.class)
                .id(report.getId())
                .fetchPlan("emailTemplate-fetchPlan")
                .one();
    }

    protected void downloadAttachment() {
        if (fileStorage == null) {
            fileStorage = fileStorageLocator.getDefault();

        }
        EmailTemplateAttachment attachment = filesTable.getSingleSelected();
        if (attachment != null) {
            downloader.download(new FileDataProvider(attachment.getContentFile(), fileStorage), attachment.getName());
        }
    }
}
