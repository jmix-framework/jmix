package io.jmix.reportsflowui.view.entitytreelist;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.view.*;
import io.jmix.reports.entity.wizard.EntityTreeNode;
import io.jmix.reportsflowui.view.EntityTreeFragment;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "entityTree", layout = DefaultMainViewParent.class)
@ViewController("EntityTreeList.lookup")
@ViewDescriptor("entity-tree-lookup.xml")
@LookupComponent("entityTree")
@DialogMode(width = "50em", height = "37.5em")
public class EntityTreeLookupView extends StandardListView<EntityTreeNode> {

    @ViewComponent
    private FormLayout treePanel;

    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Notifications notifications;
    @Autowired
    private MessageBundle messageBundle;

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
        return createEntityTree();
    }

    private TreeDataGrid<EntityTreeNode> createEntityTree() {
        EntityTreeFragment entityTreeFragment = uiComponents.create(EntityTreeFragment.class);
        entityTreeFragment.setVisible(true);
        entityTreeFragment.setParameters(rootEntity, scalarOnly, collectionsOnly, persistentOnly);
        entityTree = entityTreeFragment.getEntityTree();
        treePanel.add(entityTreeFragment);

        setSelectionValidator(validationContext -> {
            if (entityTree.getSingleSelectedItem() == null) {
                notifications.create(messageBundle.getMessage("selectItemForContinue"))
                        .withType(Notifications.Type.DEFAULT)
                        .show();
                return false;
            } else {
                if (entityTree.getSingleSelectedItem().getParent() == null) {
                    notifications.create(messageBundle.getMessage("selectNotARoot"))
                            .withType(Notifications.Type.DEFAULT)
                            .show();
                    return false;
                }
            }
            return true;
        });
        return entityTree;
    }
}
