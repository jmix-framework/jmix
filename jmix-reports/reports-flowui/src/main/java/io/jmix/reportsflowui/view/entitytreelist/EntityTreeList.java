package io.jmix.reportsflowui.view.entitytreelist;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.router.Route;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.view.*;
import io.jmix.reports.entity.wizard.EntityTreeNode;
import io.jmix.reportsflowui.view.EntityTreeFragment;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "entityTree", layout = DefaultMainViewParent.class)
@ViewController("report_EntityTreeList.list")
@ViewDescriptor("entity-tree-lookup.xml")
@DialogMode(width = "50em", height = "37.5em")
public class EntityTreeList extends StandardListView<EntityTreeNode> {
    @ViewComponent
    private FormLayout treePanel;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Messages messages;

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

    @Subscribe
    public void onInit(InitEvent event) {
        EntityTreeFragment entityTreeFragment = uiComponents.create(EntityTreeFragment.class);
        entityTreeFragment.setVisible(true);
        entityTreeFragment.setParameters(rootEntity, scalarOnly, collectionsOnly, persistentOnly);
        entityTree = entityTreeFragment.getEntityTree();
        treePanel.add(entityTreeFragment);

        setSelectionValidator(validationContext -> {
            if (entityTree.getSingleSelectedItem() == null) {
                 notifications.create(messages.getMessage(getClass(),"selectItemForContinue"))
                        .withType(Notifications.Type.DEFAULT)
                        .show();
                return false;
            } else {
                if (entityTree.getSingleSelectedItem().getParent() == null) {
                    notifications.create(messages.getMessage(getClass(),"selectNotARoot"))
                            .withType(Notifications.Type.DEFAULT)
                            .show();
                    return false;
                }
            }
            return true;
        });
    }


}