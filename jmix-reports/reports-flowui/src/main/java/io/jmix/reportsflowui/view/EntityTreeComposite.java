package io.jmix.reportsflowui.view;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.TreeDataGrid;
import io.jmix.flowui.data.grid.ContainerTreeDataGridItems;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataComponents;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.Target;
import io.jmix.flowui.view.View;
import io.jmix.reports.entity.wizard.EntityTreeNode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EntityTreeComposite extends Composite<FormLayout>
        implements ApplicationContextAware, HasSize, HasEnabled, InitializingBean, HasComponents {

    protected TreeDataGrid<EntityTreeNode> entityTree;
    protected EntityTreeNode rootEntity;
    protected UiComponents uiComponents;
    protected DataComponents dataComponents;
    protected ApplicationContext applicationContext;
    protected CollectionLoader<EntityTreeNode> reportEntityTreeNodeDl;
    protected TextField reportPropertyName;
    protected Notifications notifications;
    protected MetadataTools metadataTools;
    protected FormLayout formLayout;
    protected Messages messages;
    protected Metadata metadata;

    protected boolean scalarOnly = false;
    protected boolean collectionsOnly = false;
    protected boolean persistentOnly = false;
    protected CollectionContainer<EntityTreeNode> reportEntityTreeNodeDc;

    protected Comparator<EntityTreeNode> nodeComparator = (o1, o2) -> {
        Collator collator = Collator.getInstance();
        return collator.compare(o1.getHierarchicalLocalizedNameExceptRoot(), o2.getHierarchicalLocalizedNameExceptRoot());
    };

    @Subscribe(target = Target.PARENT_CONTROLLER)
    public void onBeforeShow(View.BeforeShowEvent event) {
        reportEntityTreeNodeDl.load();
        entityTree.expand(rootEntity);
    }

    public TreeDataGrid<EntityTreeNode> getEntityTree() {
        return entityTree;
    }

    public void setParameters(EntityTreeNode rootEntity, boolean scalarOnly, boolean collectionsOnly, boolean persistentOnly) {
        setRootEntity(rootEntity);
        setScalarOnly(scalarOnly);
        setCollectionsOnly(collectionsOnly);
        setPersistentOnly(persistentOnly);
        reportEntityTreeNodeDl.load();
    }

    public void setScalarOnly(boolean scalarOnly) {
        this.scalarOnly = scalarOnly;
    }

    public void setCollectionsOnly(boolean collectionsOnly) {
        this.collectionsOnly = collectionsOnly;
    }

    public void setPersistentOnly(boolean persistentOnly) {
        this.persistentOnly = persistentOnly;
    }

    @Override
    protected FormLayout initContent() {
        reportPropertyName = uiComponents.create(TextField.class);
        JmixButton reportPropertyNameSearchButton = uiComponents.create(JmixButton.class);
        reportPropertyNameSearchButton.setIcon(VaadinIcon.SEARCH.create());
        reportPropertyNameSearchButton.addClickListener(event -> {
            reportEntityTreeNodeDl.load();
            if (reportEntityTreeNodeDc.getItems().isEmpty()) {
                notifications.create(messages.getMessage(getClass(), "valueNotFound"))
                        .show();
            } else {
                if (StringUtils.isEmpty(reportPropertyName.getValue())) {
                    entityTree.collapse();
                    entityTree.expand(rootEntity);
                } else {
                    entityTree.expand();
                }
            }
        });

        HorizontalLayout reportPropertyHBox = uiComponents.create(HorizontalLayout.class);
        reportPropertyHBox.add(reportPropertyName);
        reportPropertyHBox.add(reportPropertyNameSearchButton);
        reportPropertyHBox.setPadding(false);

        entityTree = uiComponents.create(TreeDataGrid.class);
        entityTree.setDataProvider(new ContainerTreeDataGridItems<EntityTreeNode>(reportEntityTreeNodeDc, "parent"));
        MetaPropertyPath metaPropertyPath = metadataTools.resolveMetaPropertyPathOrNull(metadata.getClass(EntityTreeNode.class), "name");
        entityTree.addHierarchyColumn("name", metaPropertyPath);
        entityTree.setId("treeDataGrid");

        formLayout = uiComponents.create(FormLayout.class);
        formLayout.setId("entityTreeFormLayout");
        formLayout.setWidth("30em");
        formLayout.setHeightFull();
        formLayout.add(reportPropertyHBox);
        formLayout.add(entityTree);

        return formLayout;
    }

    @Override
    public void afterPropertiesSet() {
        initComponent();
    }

    protected void initComponent() {
        this.uiComponents = applicationContext.getBean(UiComponents.class);
        this.messages = applicationContext.getBean(Messages.class);
        this.dataComponents = applicationContext.getBean(DataComponents.class);
        this.metadata = applicationContext.getBean(Metadata.class);
        this.metadataTools = applicationContext.getBean(MetadataTools.class);


        reportEntityTreeNodeDc = dataComponents.createCollectionContainer(EntityTreeNode.class);
        reportEntityTreeNodeDl = dataComponents.createCollectionLoader();
        reportEntityTreeNodeDl.setContainer(reportEntityTreeNodeDc);
        reportEntityTreeNodeDl.setLoadDelegate(loadContext -> {
            List<EntityTreeNode> treeNodes = new ArrayList<>();
            if (rootEntity != null) {
                String searchValue = StringUtils.defaultIfBlank(reportPropertyName.getValue(), "").toLowerCase().trim();
                fill(rootEntity, searchValue, treeNodes);
                if (CollectionUtils.isNotEmpty(treeNodes)) { //add root entity only if at least one child is found by search string
                    treeNodes.add(rootEntity);
                }
            }
            return treeNodes;
        });
    }

    protected void fill(final EntityTreeNode parentNode, String searchValue, List<EntityTreeNode> result) {
        parentNode.getChildren().stream()
                .sorted(nodeComparator)
                .filter(childNode -> needToShowProperty(childNode, parentNode))
                .filter(childNode -> isSuitable(searchValue, childNode))
                .forEach(child -> {
                    result.add(child);
                    if (!child.getChildren().isEmpty()) {
                        if (child.getLocalizedName().toLowerCase().contains(searchValue)) {
                            fill(child, result);
                        } else {
                            fill(child, searchValue, result);
                        }
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
        return StringUtils.isEmpty(searchValue)
                || child.getLocalizedName().toLowerCase().contains(searchValue)
                || isSuitableRecursively(searchValue, child);
    }

    protected boolean isSuitableRecursively(String searchValue, EntityTreeNode node) {
        return !node.getChildren().isEmpty() && node.getChildren()
                .stream()
                .anyMatch(child -> isSuitable(searchValue, child));
    }

    protected void fill(final EntityTreeNode parentNode, List<EntityTreeNode> result) {
        fill(parentNode, "", result);
    }

    public void setRootEntity(EntityTreeNode rootEntity) {
        this.rootEntity = rootEntity;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
