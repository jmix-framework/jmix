package io.jmix.reportsui.screen.report.wizard.region;

import io.jmix.core.Messages;
import io.jmix.reports.entity.wizard.EntityTreeNode;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Tree;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("report_EntityTree.fragment")
@UiDescriptor("entity-tree-frame.xml")
public class EntityTreeFragment extends ScreenFragment {

    @Autowired
    private Tree<EntityTreeNode> entityTree;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected Messages messages;

    @Autowired
    private CollectionContainer<EntityTreeNode> reportEntityTreeNodeDc;

    @Subscribe("search")
    public void onSearch(Action.ActionPerformedEvent event) {
                if (!reportEntityTreeNodeDc.getItems().isEmpty()) {
                    entityTree.collapseTree();
                    //todo
                    //entityTree.expand(rootNode.getId());
                } else {
                    notifications.create(Notifications.NotificationType.HUMANIZED)
                            .withCaption(messages.getMessage("valueNotFound"))
                            .show();
                }
    }

}
