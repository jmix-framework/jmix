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
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.impl.model.Category;
import io.jmix.dynattr.impl.model.CategoryAttribute;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiComponents;
import io.jmix.ui.actions.Action;
import io.jmix.ui.components.Label;
import io.jmix.ui.components.Table;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.LookupComponent;
import io.jmix.ui.screen.StandardLookup;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.apache.commons.lang3.BooleanUtils;

import javax.inject.Inject;

@UiController("sys$Category.browse")
@UiDescriptor("category-browse.xml")
@LookupComponent("categoriesTable")
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

    @Subscribe("categoriesTable.applyChanges")
    protected void onCategoriesTableApplyChanges(Action.ActionPerformedEvent event) {

        notifications.create(Notifications.NotificationType.TRAY)
                .withCaption(messages.getMessage("notification.changesApplied"))
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
            labelContent = messages.getMessage(categoryAttribute.getDataType().name());
        }

        return new Table.PlainTextCell(labelContent);
    }
}
