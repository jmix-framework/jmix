package io.jmix.reportsflowui.view.entitytreelist;

import com.vaadin.flow.component.formlayout.FormLayout;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.view.*;
import io.jmix.reports.entity.wizard.EntityTreeNode;
import io.jmix.reportsflowui.view.reportwizard.EntityTreeComposite;
import org.springframework.beans.factory.annotation.Autowired;

@ViewController("EntityTreeList.lookup")
@ViewDescriptor("entity-tree-lookup.xml")
@LookupComponent("treePanel.treeDataGrid")
@DialogMode(height = "37.5em")
public class EntityTreeLookupView extends StandardListView<EntityTreeNode> {

    @ViewComponent
    protected FormLayout treePanel;

    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Notifications notifications;
    @Autowired
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
    }

    @Override
    public io.jmix.flowui.component.LookupComponent<EntityTreeNode> getLookupComponent() {
        if (entityTree == null) {
            createEntityTree();
        }
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
