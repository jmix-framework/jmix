/*
 * Copyright 2022 Haulmont.
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

package io.jmix.datatoolsflowui.view.entityinfo;

import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlans;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.PersistenceHints;
import io.jmix.datatools.EntitySqlGenerationService;
import io.jmix.datatoolsflowui.view.entityinfo.model.InfoValue;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@ViewController("EntityInfoView")
@ViewDescriptor("entity-info-view.xml")
@DialogMode(width = "50em", height = "37.5em", resizable = true)
public class EntityInfoView extends StandardView {

    @ViewComponent
    protected DataGrid<InfoValue> infoDataGrid;
    @ViewComponent
    protected CollectionContainer<InfoValue> infoDc;

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected MessageBundle messageBundle;
    @Autowired
    protected Notifications notifications;
    @Autowired(required = false)
    protected EntitySqlGenerationService sqlGenerationService;

    protected Object entity;

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public Object getEntity() {
        return entity;
    }

    @Subscribe
    public void onInit(InitEvent event) {
        initGrid();
    }

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        if (entity == null) {
            throw new IllegalStateException("Entity is not passed. Can't collect system info");
        }

        collectEntityInfo();
    }

    protected void initGrid() {
        infoDataGrid.removeAllActions();
        infoDataGrid.getColumnByKey("key").setHeader(messageBundle.getMessage("entityInfo.name"));
        infoDataGrid.getColumnByKey("value").setHeader(messageBundle.getMessage("entityInfo.value"));
    }

    protected void collectEntityInfo() {
        EntityStates entityStates = getApplicationContext().getBean(EntityStates.class);
        boolean isNewEntity = entityStates.isNew(entity);

        if (!isNewEntity) {
            entity = reloadEntity(entity);
        }

        MetaClass metaClass = metadata.getClass(entity);

        List<InfoValue> items = new ArrayList<>();
        items.add(createItem("entityInfo.entityName", metaClass.getName()));

        Class<?> javaClass = metaClass.getJavaClass();
        items.add(createItem("entityInfo.entityClass", javaClass.getName()));

        if ((metadataTools.isJpaEmbeddable(metaClass) || metadataTools.isJpaEntity(metaClass))
                && isNewEntity) {
            items.add(createItem("entityInfo.state",
                    messageBundle.getMessage("entityInfo.isNew")));
        }

        if (metadataTools.isJpaEmbeddable(metaClass)) {
            items.add(createItem("entityInfo.specificInstance",
                    messageBundle.getMessage("entityInfo.embeddableInstance")));
        } else if (!metadataTools.isJpaEntity(metaClass)) {
            items.add(createItem("entityInfo.specificInstance",
                    messageBundle.getMessage("entityInfo.nonPersistentInstance")));
        }

        addItem(items, metadataTools.getDatabaseTable(metaClass), "entityInfo.entityTable");
        addItem(items, EntityValues.getId(entity), "entityInfo.id");
        addItem(items, EntityValues.getVersion(entity), "entityInfo.version");

        if (EntityValues.isAuditSupported(entity)) {
            addItem(items, EntityValues.getCreatedDate(entity), "entityInfo.createdDate", metadataTools::format);
            addItem(items, EntityValues.getCreatedBy(entity), "entityInfo.createdBy");

            addItem(items,
                    EntityValues.getLastModifiedDate(entity), "entityInfo.lastModifiedDate", metadataTools::format);
            addItem(items, EntityValues.getLastModifiedBy(entity), "entityInfo.lastModifiedBy");
        }

        if (EntityValues.isSoftDeleted(entity)) {
            addItem(items, EntityValues.getDeletedDate(entity), "entityInfo.deletedDate", metadataTools::format);
            addItem(items, EntityValues.getDeletedBy(entity), "entityInfo.deletedBy");
        }

        infoDc.setItems(items);
    }

    protected Object reloadEntity(Object entity) {
        Object id = EntityValues.getId(entity);
        if (id == null) {
            return entity;
        }

        FetchPlans fetchPlans = getApplicationContext().getBean(FetchPlans.class);
        FetchPlan fetchPlan = fetchPlans.builder(entity.getClass())
                .addSystem()
                .build();

        DataManager dataManager = getApplicationContext().getBean(DataManager.class);
        return dataManager.load(entity.getClass())
                .id(id)
                .hint(PersistenceHints.SOFT_DELETION, false)
                .fetchPlan(fetchPlan)
                .one();
    }

    protected void addItem(List<InfoValue> items, @Nullable Object value, String messageKey) {
        addItem(items, value, messageKey, null);
    }

    protected void addItem(List<InfoValue> items, @Nullable Object value, String messageKey,
                           @Nullable Function<Object, String> formatter) {
        if (value != null) {
            items.add(createItem(messageKey, formatter != null
                    ? formatter.apply(value)
                    : value));
        }
    }

    protected InfoValue createItem(String messageKey, Object value) {
        InfoValue item = metadata.create(InfoValue.class);
        item.setKey(messageBundle.getMessage(messageKey));
        item.setValue(metadataTools.format(value));

        return item;
    }
}
