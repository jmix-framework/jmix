/*
 * Copyright 2021 Haulmont.
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

import io.jmix.reports.entity.wizard.EntityTreeNode;
import io.jmix.ui.Notifications;
import io.jmix.ui.component.Tree;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@UiController("report_ReportEntityTree.lookup")
@UiDescriptor("entity-tree-lookup.xml")
@LookupComponent("entityTreeFragment.entityTree")
public class EntityTreeLookup extends StandardLookup<EntityTreeNode> {
    @Autowired
    @Qualifier("entityTreeFragment.entityTree")
    protected Tree<EntityTreeNode> entityTree;

    @Autowired
    protected MessageBundle messageBundle;

    @Autowired
    protected Notifications notifications;

    @Subscribe
    protected void onInit(InitEvent event) {
        setSelectValidator(validationContext -> {
            if (entityTree.getSingleSelected() == null) {
                notifications.create(Notifications.NotificationType.TRAY)
                        .withCaption(messageBundle.getMessage("selectItemForContinue"))
                        .show();
                return false;
            } else {
                if (entityTree.getSingleSelected().getParent() == null) {
                    notifications.create(Notifications.NotificationType.TRAY)
                            .withCaption(messageBundle.getMessage("selectNotARoot"))
                            .show();

                    return false;
                }
            }
            return true;
        });
    }
}