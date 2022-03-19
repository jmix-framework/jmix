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

package io.jmix.emailtemplatesui.screen.emailtemplate.browse;


import io.jmix.core.AccessManager;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.emailtemplates.entity.EmailTemplate;
import io.jmix.emailtemplates.entity.JsonEmailTemplate;
import io.jmix.emailtemplates.entity.ReportEmailTemplate;
import io.jmix.emailtemplatesui.role.accesscontext.TemplateBlocksButtonContext;
import io.jmix.emailtemplatesui.role.accesscontext.TemplateGroupsButtonContext;
import io.jmix.emailtemplatesui.screen.emailtemplate.send.EmailTemplateSendScreen;
import io.jmix.emailtemplatesui.screen.templateblock.TemplateBlockBrowse;
import io.jmix.emailtemplatesui.screen.templategroup.TemplateGroupBrowse;
import io.jmix.ui.Actions;
import io.jmix.ui.Screens;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ItemTrackingAction;
import io.jmix.ui.action.list.CreateAction;
import io.jmix.ui.component.*;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@UiController("emltmp_EmailTemplate.browse")
@UiDescriptor("email-template-browse.xml")
public class EmailTemplateBrowse extends StandardLookup<EmailTemplate> {

    @Autowired
    protected Screens screens;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected GroupTable<EmailTemplate> emailTemplatesTable;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected PopupButton createBtn;

    @Autowired
    protected Actions actions;

    @Autowired
    protected Button groupsButton;

    @Autowired
    protected Button blocksButton;

    @Autowired
    @Qualifier("emailTemplatesTable.createFromDesigner")
    protected CreateAction<JsonEmailTemplate> createFromDesignerAction;

    @Autowired
    @Qualifier("emailTemplatesTable.createFromReport")
    protected CreateAction<ReportEmailTemplate> createFromReportAction;

    @Autowired
    protected Button sendButton;

    @Autowired
    protected AccessManager accessManager;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        Action sendAction = new ItemTrackingAction("sendAction").
                withHandler(actionPerformedEvent -> onSendEmailClick());
        emailTemplatesTable.addAction(sendAction);
        sendButton.setAction(sendAction);

        createFromDesignerAction.setNewEntitySupplier(() -> metadata.create(JsonEmailTemplate.class));
        createFromDesignerAction.setScreenId(metadata.getClass(JsonEmailTemplate.class).getName() + ".edit");

        createFromReportAction.setNewEntitySupplier(() -> metadata.create(ReportEmailTemplate.class));
        createFromReportAction.setScreenId(metadata.getClass(ReportEmailTemplate.class).getName() + ".edit");

        groupsButton.setEnabled(isGroupsButtonPermitted());
        blocksButton.setEnabled(isBlocksButtonPermitted());
    }

    @Subscribe("blocksButton")
    private void onBlocksButtonClick(Button.ClickEvent event) {
        screens.create(TemplateBlockBrowse.class, OpenMode.NEW_TAB).show();
    }

    @Subscribe("groupsButton")
    public void onGroupsButtonClick(Button.ClickEvent event) {
        screens.create(TemplateGroupBrowse.class, OpenMode.NEW_WINDOW).show();
    }

    protected void onSendEmailClick() {
        EmailTemplate template = emailTemplatesTable.getSingleSelected();
        if (template != null) {
            template = dataManager
                    .load(EmailTemplate.class)
                    .id(template.getId())
                    .fetchPlan("emailTemplate-fetchPlan")
                    .optional().orElse(null);
            screens.create(EmailTemplateSendScreen.class,
                    OpenMode.DIALOG,
                    new MapScreenOptions(ParamsMap.of("emailTemplate", template)))
                    .show();
        }
    }

    protected boolean isGroupsButtonPermitted() {
        TemplateGroupsButtonContext templateGroupsButtonContext = new TemplateGroupsButtonContext();
        accessManager.applyRegisteredConstraints(templateGroupsButtonContext);
        return templateGroupsButtonContext.isPermitted();
    }

    protected boolean isBlocksButtonPermitted() {
        TemplateBlocksButtonContext templateBlocksButtonContext = new TemplateBlocksButtonContext();
        accessManager.applyRegisteredConstraints(templateBlocksButtonContext);
        return templateBlocksButtonContext.isPermitted();
    }

}