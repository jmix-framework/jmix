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
import io.jmix.reportsui.screen.report.browse.ReportBrowser;
import io.jmix.ui.Actions;
import io.jmix.ui.Fragments;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ItemTrackingAction;
import io.jmix.ui.action.list.AddAction;
import io.jmix.ui.component.Form;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.VBoxLayout;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.download.FileDataProvider;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@UiController("emailtemplates_EmailTemplateAttachmentsFragment")
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
    protected DataContext dataContext;

    @Autowired
    @Qualifier("reportsTable.add")
    protected AddAction addTemplateReportAction;

    @Subscribe
    public void onAttachEvent(AttachEvent e) {
        Action downloadAction = actions.create(ItemTrackingAction.class, "download")
                .withHandler(event -> downloadAttachment());
        filesTable.addAction(downloadAction);

        addTemplateReportAction.setScreenClass(ReportBrowser.class);

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

    @Install(to = "reportsTable.add", subject = "transformation")
    protected Collection<TemplateReport> reportsTableAddTransformation(Collection<Report> entities) {
        return entities.stream()
                .map(this::transformToTemplateReport)
                .collect(Collectors.toList());
    }

    protected TemplateReport transformToTemplateReport(Report o) {
        Report report = o;
        report = dataManager.load(Report.class)
                .id(report.getId())
                .fetchPlan("emailTemplate-fetchPlan")
                .one();
        TemplateReport templateReport = dataContext.create(TemplateReport.class);
        templateReport.setReport(report);
        templateReport.setEmailTemplate(emailTemplateDc.getItem());
        templateReport.setParameterValues(new ArrayList<>());
        return templateReport;
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
