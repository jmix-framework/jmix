package io.jmix.reportsui.screen.report.wizard.region;

import io.jmix.core.DataManager;
import io.jmix.reports.entity.wizard.EntityTreeNode;
import io.jmix.ui.component.Tree;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("report_EntityTree.fragment")
@UiDescriptor("entity-tree-frame.xml")
public class EntityTreeFragment extends ScreenFragment {

    @Autowired
    protected DataManager dataManager;
    @Autowired
    private CollectionContainer<EntityTreeNode> reportEntityTreeNodeDc;
    @Autowired
    private Tree<EntityTreeNode> entityTree;

    @Subscribe(target = Target.PARENT_CONTROLLER)
    public void onBeforeShow(Screen.BeforeShowEvent event) {
        RegionEditor regionEditor = (RegionEditor) getHostScreen();

        reportEntityTreeNodeDc.getMutableItems().add(regionEditor.getRootEntity());
        reportEntityTreeNodeDc.getMutableItems().addAll(regionEditor.getRootEntity().getChildren());

        entityTree.expand(regionEditor.getRootEntity());
    }

}
