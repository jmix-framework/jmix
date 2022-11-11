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

import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.reports.entity.wizard.EntityTreeNode;
import io.jmix.ui.Notifications;
import io.jmix.ui.WindowParam;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.Tree;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@UiController("report_EntityTree.fragment")
@UiDescriptor("entity-tree-fragment.xml")
public class EntityTreeFragment extends ScreenFragment {

    @Autowired
    protected DataManager dataManager;

    @Autowired
    private Tree<EntityTreeNode> entityTree;

    @WindowParam
    protected EntityTreeNode rootEntity;

    @WindowParam
    protected boolean collectionsOnly = false;

    @WindowParam
    protected boolean scalarOnly = false;

    @WindowParam
    protected boolean persistentOnly = false;

    @Autowired
    protected TextField<String> reportPropertyName;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected CollectionContainer<EntityTreeNode> reportEntityTreeNodeDc;

    @Autowired
    protected CollectionLoader<EntityTreeNode> reportEntityTreeNodeDl;

    @Autowired
    protected MessageBundle messageBundle;

    @Autowired
    protected Notifications notifications;

    protected Comparator<EntityTreeNode> nodeComparator = (o1, o2) -> {
        Collator collator = Collator.getInstance();
        return collator.compare(o1.getHierarchicalLocalizedNameExceptRoot(), o2.getHierarchicalLocalizedNameExceptRoot());
    };

    @Subscribe(target = Target.PARENT_CONTROLLER)
    public void onBeforeShow(Screen.BeforeShowEvent event) {
        reportEntityTreeNodeDl.load();
        entityTree.expand(rootEntity);
    }

    @Install(to = "reportEntityTreeNodeDl", target = Target.DATA_LOADER)
    private List<EntityTreeNode> reportEntityTreeNodeDlLoadDelegate(LoadContext<EntityTreeNode> loadContext) {
        List<EntityTreeNode> treeNodes = new ArrayList<>();
        String searchValue = StringUtils.defaultIfBlank(reportPropertyName.getValue(), "").toLowerCase().trim();
        fill(rootEntity, searchValue, treeNodes);
        if (CollectionUtils.isNotEmpty(treeNodes)) { //add root entity only if at least one child is found by search string
            treeNodes.add(rootEntity);
        }
        return treeNodes;
    }

    protected void fill(final EntityTreeNode parentNode, String searchValue, List<EntityTreeNode> result) {
        parentNode.getChildren().stream()
                .sorted(nodeComparator)
                .filter(childNode -> needToShowProperty(childNode, parentNode))
                .filter(childNode -> isSuitable(searchValue, childNode))
                .forEach(child -> {
                    result.add(child);
                    if (!child.getChildren().isEmpty()) {
                        fill(child, result);
                    }
                });
    }

    protected boolean needToShowProperty(EntityTreeNode childNode, EntityTreeNode parentNode) {
        MetaClass parentMetaClass = metadata.getClass(childNode.getParentMetaClassName());
        MetaProperty metaProperty = parentMetaClass.getProperty(childNode.getMetaPropertyName());

        boolean isCollection = metaProperty.getRange().getCardinality().isMany();

        boolean ignoreScalarProperties = collectionsOnly && !isCollection;
        boolean ignoreCollections = scalarOnly && isCollection;
        boolean isSystem = metadataTools.isSystemLevel(metaProperty);
        boolean ignoreNotPersistent = persistentOnly && !metadataTools.isJpa(metaProperty);
        if (ignoreScalarProperties || isSystem || ignoreCollections || ignoreNotPersistent) {
            return false;
        }

        if (collectionsOnly && parentNode.getParent() != null && parentNode.getParent().getParent() == null) {
            //for collections max selection depth is limited to 2 cause reporting is not supported collection multiplying. And it is good )
            return false;
        }

        if (childNode.getChildren().isEmpty() && scalarOnly && metaProperty.getRange().isClass()) {
            //doesn't fetch if it is a last entity and is a class cause we can`t select it in UI anyway
            return false;
        }
        return true;
    }

    protected boolean isSuitable(String searchValue, EntityTreeNode child) {
        return StringUtils.isEmpty(searchValue) || child.getLocalizedName().toLowerCase().contains(searchValue);
    }

    protected void fill(final EntityTreeNode parentNode, List<EntityTreeNode> result) {
        fill(parentNode, "", result);
    }

    @Subscribe("reportPropertyNameSearchButton")
    public void onReportPropertyNameSearchButtonClick(Button.ClickEvent event) {
        reportEntityTreeNodeDl.load();
        if (reportEntityTreeNodeDc.getItems().isEmpty()) {
            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withCaption(messageBundle.getMessage("valueNotFound"))
                    .show();
        } else {
            if (StringUtils.isEmpty(reportPropertyName.getValue())) {
                entityTree.collapseTree();
                entityTree.expand(rootEntity);
            } else {
                entityTree.expandTree();
            }
        }
    }
}
