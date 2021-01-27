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

package io.jmix.reportsui.screen.report.wizard.region;

import io.jmix.core.Messages;
import io.jmix.reports.entity.wizard.EntityTreeNode;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.Tree;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;

@UiController("report_ReportEntityTree.lookup")
@UiDescriptor("entity-tree-lookup.xml")
@LookupComponent("entityTreeFrame.entityTree")
public class EntityTreeLookup extends StandardLookup {

    //    @Named("entityTreeFrame.reportEntityTreeNodeDs")
//    protected AbstractTreeDatasource reportEntityTreeNodeDs;
    @Named("entityTreeFrame.entityTree")
    protected Tree entityTree;
    @Named("entityTreeFrame.reportPropertyName")
    protected TextField<String> reportPropertyName;
    @Named("entityTreeFrame.reportPropertyNameSearchButton")
    protected Button reportPropertyNameSearchButton;

    @Autowired
    protected Messages messages;

    @Autowired
    protected Notifications notifications;

    protected EntityTreeNode rootNode;

    @Subscribe
    protected void onInit(InitEvent event) {
        //todo params
//        params.put("component$reportPropertyName", reportPropertyName);
//
//        reportEntityTreeNodeDs.refresh(params);
//        rootNode = (EntityTreeNode) params.get("rootEntity");
//        entityTree.expandTree();
//
//
//        this.setLookupValidator(() -> {
//            if (entityTree.getSingleSelected() == null) {
//                notifications.create(Notifications.NotificationType.TRAY)
//                        .withCaption(messages.getMessage("selectItemForContinue"))
//                        .show();
//                return false;
//            } else {
//                if (((EntityTreeNode) entityTree.getSingleSelected()).getParent() == null) {
//                    notifications.create(Notifications.NotificationType.TRAY)
//                            .withCaption(messages.getMessage("selectNotARoot"))
//                            .show();
//
//                    return false;
//                }
//            }
//            return true;
//        });
    }

}