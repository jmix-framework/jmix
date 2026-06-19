/*
 * Copyright 2026 Haulmont.
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

package io.jmix.reportsflowui.view.entitytreelist;

import com.vaadin.flow.component.formlayout.FormLayout;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.view.*;
import io.jmix.reports.entity.wizard.EntityTreeNode;
import io.jmix.reportsflowui.view.reportwizard.EntityTreeComposite;
import org.springframework.beans.factory.annotation.Autowired;

@ViewController("report_EntityTreeNode.list")
@ViewDescriptor("entity-tree-node-list-view.xml")
@LookupComponent("treePanel.treeDataGrid")
@DialogMode(height = "37.5em")
public class EntityTreeNodeListView extends StandardListView<EntityTreeNode> {

    @ViewComponent
    protected FormLayout treePanel;

    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Notifications notifications;
    @ViewComponent
    protected MessageBundle messageBundle;

    protected EntityTreeNode rootEntity;
    protected boolean scalarOnly = false;
    protected boolean collectionsOnly = false;
    protected boolean persistentOnly = false;
    protected TreeDataGrid<EntityTreeNode> entityTree;

    public void setParameters(EntityTreeNode rootEntity, boolean scalarOnly, boolean collectionsOnly, boolean persistentOnly) {
        this.rootEntity = rootEntity;
        this.scalarOnly = scalarOnly;
        this.collectionsOnly = collectionsOnly;
        this.persistentOnly = persistentOnly;
        createEntityTree();
    }

    @Override
    public io.jmix.flowui.component.LookupComponent<EntityTreeNode> getLookupComponent() {
        return entityTree;
    }

    protected TreeDataGrid<EntityTreeNode> createEntityTree() {
        EntityTreeComposite entityTreeComposite = uiComponents.create(EntityTreeComposite.class);
        entityTreeComposite.setId("entityTreeComposite");
        entityTreeComposite.setVisible(true);
        entityTreeComposite.setParameters(rootEntity, scalarOnly, collectionsOnly, persistentOnly);
        entityTree = entityTreeComposite.getEntityTree();
        treePanel.add(entityTreeComposite);

        setSelectionValidator(validationContext -> {
            if (entityTree.getSingleSelectedItem() == null) {
                notifications.create(messageBundle.getMessage("selectItemForContinue"))
                        .show();
                return false;
            } else {
                if (entityTree.getSingleSelectedItem().getParent() == null) {
                    notifications.create(messageBundle.getMessage("selectNotARoot"))
                            .show();
                    return false;
                }
            }
            return true;
        });

        return entityTree;
    }
}
