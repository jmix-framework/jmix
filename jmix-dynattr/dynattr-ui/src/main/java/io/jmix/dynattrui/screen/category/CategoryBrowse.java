/*
 * Copyright 2020 Haulmont.
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

package io.jmix.dynattrui.screen.category;

import io.jmix.core.*;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.model.Category;
import io.jmix.dynattr.model.CategoryAttribute;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.Label;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.InstanceLoader;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("dynat_Category.browse")
@UiDescriptor("category-browse.xml")
@LookupComponent("categoriesTable")
public class CategoryBrowse extends StandardLookup<Category> {

    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Messages messages;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected DynAttrMetadata dynAttrMetadata;

    @Autowired
    protected CollectionContainer<CategoryAttribute> attributesDc;
    @Autowired
    protected InstanceContainer<Category> categoryDc;
    @Autowired
    protected InstanceLoader<Category> categoryDl;
    @Autowired
    private CollectionLoader<Category> categoriesDl;
    @Autowired
    private AccessManager accessManager;
    @Autowired
    private Button applyChangesBtn;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        attributesDc.getSorter().sort(Sort.by(Sort.Direction.ASC, "orderNo"));
        setupFieldsLock();
    }

    @Subscribe("categoriesTable.applyChanges")
    protected void onCategoriesTableApplyChanges(Action.ActionPerformedEvent event) {
        dynAttrMetadata.reload();
        notifications.create(Notifications.NotificationType.TRAY)
                .withCaption(messages.getMessage(CategoryBrowse.class, "notification.changesApplied"))
                .show();
    }

    @Install(to = "categoriesTable.entityType", subject = "columnGenerator")
    protected Label<String> categoriesTableEntityTypeColumnGenerator(Category category) {
        Label<String> dataTypeLabel = uiComponents.create(Label.NAME);
        MetaClass metaClass = metadata.getSession().getClass(category.getEntityType());
        dataTypeLabel.setValue(messageTools.getEntityCaption(metaClass));
        return dataTypeLabel;
    }

    @Install(to = "attributesTable.dataType", subject = "columnGenerator")
    protected Table.PlainTextCell attributesTableDataTypeColumnGenerator(CategoryAttribute categoryAttribute) {
        String labelContent;
        if (BooleanUtils.isTrue(categoryAttribute.getIsEntity())) {
            Class<?> clazz = categoryAttribute.getJavaType();

            if (clazz != null) {
                MetaClass metaClass = metadata.getSession().getClass(clazz);
                labelContent = messageTools.getEntityCaption(metaClass);
            } else {
                labelContent = "";
            }
        } else {
            String key = AttributeType.class.getSimpleName() + "." + categoryAttribute.getDataType().toString();
            labelContent = messages.getMessage(AttributeType.class, key);
        }

        return new Table.PlainTextCell(labelContent);
    }

    @Install(to = "categoriesTable.edit", subject = "afterCommitHandler")
    private void categoriesTableEditAfterCommitHandler(Category category) {
        categoriesDl.load();
    }

    @Install(to = "categoriesTable.create", subject = "afterCommitHandler")
    private void categoriesTableCreateAfterCommitHandler(Category category) {
        categoriesDl.load();
    }

    @Subscribe(id = "categoriesDc", target = Target.DATA_CONTAINER)
    protected void onCategoriesDcItemChange(InstanceContainer.ItemChangeEvent<Category> event) {
        Category category = event.getItem();
        if (category != null) {
            categoryDl.setEntityId(category.getId());
            categoryDl.load();
        } else {
            categoryDc.setItem(null);
        }
    }

    protected void setupFieldsLock() {
        CrudEntityContext crudEntityContext = new CrudEntityContext(categoryDc.getEntityMetaClass());
        accessManager.applyRegisteredConstraints(crudEntityContext);
        if (!crudEntityContext.isUpdatePermitted()) {
            applyChangesBtn.setEnabled(false);
        }
    }
}
