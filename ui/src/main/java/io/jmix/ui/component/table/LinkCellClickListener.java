/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component.table;

import io.jmix.core.DataManager;
import io.jmix.core.Entity;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanRepository;
import io.jmix.core.Id;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.Table;
import io.jmix.ui.screen.EditorScreen;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenContext;
import io.jmix.ui.screen.StandardOutcome;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.function.Consumer;


@SuppressWarnings({"unchecked", "rawtypes"})
public class LinkCellClickListener implements Consumer<Table.Column.ClickEvent> {

    protected ApplicationContext applicationContext;

    public LinkCellClickListener(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void accept(Table.Column.ClickEvent clickEvent) {
        if (!clickEvent.isText()) {
            return;
        }

        Table.Column<?> column = clickEvent.getSource();
        Table owner = column.getOwner();
        if (owner == null || owner.getFrame() == null) {
            return;
        }

        Object rowItem = clickEvent.getItem();
        MetaPropertyPath mpp = column.getMetaPropertyPathNN();
        Object item = EntityValues.getValueEx(rowItem, mpp);

        Entity entity;
        if (EntityValues.isEntity(item)) {
            entity = (Entity) item;
        } else {
            entity = (Entity) rowItem;
        }

        if (EntityValues.isSoftDeleted(entity)) {
            ScreenContext context = ComponentsHelper.getScreenContext(owner);
            context.getNotifications().create(Notifications.NotificationType.HUMANIZED)
                    .withCaption(applicationContext.getBean(Messages.class)
                            .getMessage("OpenAction.objectIsDeleted"))
                    .show();
            return;
        }

        entity = loadEntity(entity);

        MetaClass metaClass = applicationContext.getBean(Metadata.class).getClass(entity);
        String linkScreenId = loadLinkScreenId(column, metaClass);

        OpenMode openMode = loadLinkScreenOpenMode(column);

        Screen editor = applicationContext.getBean(ScreenBuilders.class)
                .editor(metaClass.getJavaClass(), owner.getFrame().getFrameOwner())
                .withScreenId(linkScreenId)
                .editEntity(entity)
                .withOpenMode(openMode)
                .build();

        editor.addAfterCloseListener(afterCloseEvent -> {
            // move focus to component
            owner.focus();

            if (afterCloseEvent.closedWith(StandardOutcome.COMMIT)
                    && editor instanceof EditorScreen) {
                onEditScreenAfterCommit(mpp, rowItem, (EditorScreen) editor, owner);
            }
        });

        editor.show();
    }

    protected Entity loadEntity(Entity entity) {
        return applicationContext.getBean(DataManager.class).load(Id.of(entity))
                .fetchPlan(applicationContext.getBean(FetchPlanRepository.class)
                        .getFetchPlan(entity.getClass(), FetchPlan.INSTANCE_NAME))
                .one();
    }

    protected String loadLinkScreenId(Table.Column column, MetaClass metaClass) {
        String linkScreenId = null;
        if (column.getXmlDescriptor() != null) {
            linkScreenId = column.getXmlDescriptor().attributeValue("linkScreenId");
        }
        if (StringUtils.isEmpty(linkScreenId)) {
            linkScreenId = applicationContext.getBean(WindowConfig.class).getEditorScreenId(metaClass);
        }

        return linkScreenId;
    }

    protected OpenMode loadLinkScreenOpenMode(Table.Column column) {
        OpenMode openMode = OpenMode.THIS_TAB;
        if (column.getXmlDescriptor() != null) {
            String linkScreenOpenMode = column.getXmlDescriptor().attributeValue("linkScreenOpenMode");
            if (StringUtils.isNotEmpty(linkScreenOpenMode)) {
                openMode = OpenMode.valueOf(linkScreenOpenMode);
            }
        }

        return openMode;
    }

    protected void onEditScreenAfterCommit(MetaPropertyPath mpp, Object rowItem, EditorScreen editor, Table owner) {
        Object rowEditedEntity;
        if (mpp.getRange().isClass()) {
            rowEditedEntity = rowItem;
            Object editedEntity = editor.getEditedEntity();
            EntityValues.setValueEx(rowEditedEntity, mpp, editedEntity);
        } else {
            rowEditedEntity = editor.getEditedEntity();
        }

        if (owner.getItems() != null) {
            owner.getItems().updateItem(rowEditedEntity);
        }
    }
}
