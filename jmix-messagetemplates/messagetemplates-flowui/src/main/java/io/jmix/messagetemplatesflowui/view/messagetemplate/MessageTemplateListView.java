/*
 * Copyright 2024 Haulmont.
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

package io.jmix.messagetemplatesflowui.view.messagetemplate;

import com.vaadin.flow.router.Route;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.accesscontext.UiEntityContext;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.messagetemplates.entity.MessageTemplate;
import io.jmix.messagetemplates.entity.MessageTemplateParameter;
import io.jmix.messagetemplates.entity.TemplateType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "msgtmp/messagetemplate", layout = DefaultMainViewParent.class)
@ViewController("msgtmp_MessageTemplate.list")
@ViewDescriptor("message-template-list-view.xml")
@LookupComponent("messageTemplatesDataGrid")
@DialogMode(width = "64em")
public class MessageTemplateListView extends StandardListView<MessageTemplate> {

    protected static final int MAX_CODE_LENGTH = 255;

    @ViewComponent
    protected DataGrid<MessageTemplate> messageTemplatesDataGrid;
    @ViewComponent
    protected CollectionContainer<MessageTemplate> messageTemplatesDc;
    @ViewComponent
    protected CollectionLoader<MessageTemplate> messageTemplatesDl;

    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected AccessManager accessManager;

    protected boolean isCreatePermitted;

    @Subscribe
    public void onInit(InitEvent event) {
        applySecurityConstraints();
    }

    protected void applySecurityConstraints() {
        MetaClass metaClass = messageTemplatesDc.getEntityMetaClass();

        UiEntityContext uiEntityContext = new UiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(uiEntityContext);

        isCreatePermitted = uiEntityContext.isCreatePermitted();
    }

    public void setTemplateType(String templateType) {
        messageTemplatesDl.setParameter("templateType", TemplateType.valueOf(templateType));
    }

    @Install(to = "messageTemplatesDataGrid.copy", subject = "enabledRule")
    public boolean messageTemplatesDataGridCopyEnabledRule() {
        return isCreatePermitted;
    }

    @Subscribe("messageTemplatesDataGrid.copy")
    public void onMessageTemplatesDataGridCopy(ActionPerformedEvent event) {
        MessageTemplate selectedItem = messageTemplatesDataGrid.getSingleSelectedItem();

        if (selectedItem != null) {
            copyTemplate(selectedItem);
            messageTemplatesDl.load();
        }
    }

    protected void copyTemplate(MessageTemplate messageTemplate) {
        messageTemplate = reloadTemplateWithParameters(messageTemplate);

        MessageTemplate copiedTemplate = metadataTools.deepCopy(messageTemplate);
        copiedTemplate.setId(UuidProvider.createUuid());
        copiedTemplate.setCode(generateTemplateCode(copiedTemplate.getCode()));

        SaveContext saveContext = new SaveContext();
        saveContext.setDiscardSaved(true);
        saveContext.saving(copiedTemplate);

        if (copiedTemplate.getParameters() != null) {
            updateParameters(copiedTemplate);
            saveContext.saving(copiedTemplate.getParameters());
        }

        dataManager.save(saveContext);
    }

    protected void updateParameters(MessageTemplate copiedTemplate) {
        for (MessageTemplateParameter parameter : copiedTemplate.getParameters()) {
            parameter.setId(UuidProvider.createUuid());
            parameter.setTemplate(copiedTemplate);
        }
    }

    protected MessageTemplate reloadTemplateWithParameters(MessageTemplate template) {
        return dataManager.load(Id.of(template))
                .fetchPlan(this::fetchPlanBuilder)
                .one();
    }

    protected void fetchPlanBuilder(FetchPlanBuilder fetchPlanBuilder) {
        fetchPlanBuilder.addFetchPlan(FetchPlan.BASE)
                .add("parameters", FetchPlan.BASE)
                .build();
    }

    protected String generateTemplateCode(String existedCode) {
        return generateTemplateCode(existedCode, 0);
    }

    protected String generateTemplateCode(String existedCode, int iteration) {
        if (iteration == 1) {
            ++iteration; // like in file system: duplication of file 'a.txt' is a 'a (2).txt', NOT 'a (1).txt'
        }

        String templateCode = StringUtils.stripEnd(existedCode, null);
        if (iteration > 0) {
            String suffix = "-%s".formatted(iteration);
            String newTemplateCode = templateCode;

            while (newTemplateCode.length() + suffix.length() > MAX_CODE_LENGTH) {
                newTemplateCode = StringUtils.chop(newTemplateCode);
            }

            templateCode = newTemplateCode + suffix;
        }

        if (isTemplateCodeExist(templateCode)) {
            return generateTemplateCode(existedCode, ++iteration);
        }

        return templateCode;
    }

    protected boolean isTemplateCodeExist(String templateCode) {
        LoadContext<MessageTemplate> loadContext
                = new LoadContext<>(messageTemplatesDc.getEntityMetaClass());

        loadContext.setQueryString("select t from msgtmp_MessageTemplate t where t.code = :templateCode")
                .setParameter("templateCode", templateCode);

        return dataManager.getCount(loadContext) > 0;
    }
}
