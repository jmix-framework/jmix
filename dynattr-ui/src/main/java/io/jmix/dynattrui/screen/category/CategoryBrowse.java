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

import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.Sort;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.impl.model.Category;
import io.jmix.dynattr.impl.model.CategoryAttribute;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.Label;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.model.InstanceLoader;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.LoadDataBeforeShow;
import io.jmix.ui.screen.LookupComponent;
import io.jmix.ui.screen.StandardLookup;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.Target;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.apache.commons.lang3.BooleanUtils;

import javax.inject.Inject;

@UiController("sys$Category.browse")
@UiDescriptor("category-browse.xml")
@LookupComponent("categoriesTable")
@LoadDataBeforeShow
public class CategoryBrowse extends StandardLookup<Category> {

    @Inject
    protected Notifications notifications;
    @Inject
    protected Messages messages;
    @Inject
    protected UiComponents uiComponents;
    @Inject
    protected Metadata metadata;
    @Inject
    protected MessageTools messageTools;
    @Inject
    protected DynAttrMetadata dynAttrMetadata;

    @Inject
    protected CollectionContainer<CategoryAttribute> attributesDc;
    @Inject
    protected InstanceContainer<Category> categoryDc;
    @Inject
    protected InstanceLoader<Category> categoryDl;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        attributesDc.getSorter().sort(Sort.by(Sort.Direction.ASC, "orderNo"));
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
}
