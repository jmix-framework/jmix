/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowuidata.genericfilter;

import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.security.event.UserRemovedEvent;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.flowui.component.genericfilter.FilterConfigurationPersistence;
import io.jmix.flowui.component.genericfilter.model.FilterConfigurationModel;
import io.jmix.flowuidata.entity.FilterConfiguration;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component("flowui_DatabaseFilterConfigurationPersistence")
public class DatabaseFilterConfigurationPersistence implements FilterConfigurationPersistence {

    private final DataManager dataManager;
    private final EntityStates entityStates;

    private final CurrentUserSubstitution currentUserSubstitution;

    public DatabaseFilterConfigurationPersistence(DataManager dataManager, EntityStates entityStates,
                                                  CurrentUserSubstitution currentUserSubstitution) {
        this.dataManager = dataManager;
        this.entityStates = entityStates;
        this.currentUserSubstitution = currentUserSubstitution;
    }

    @Override
    public void remove(FilterConfigurationModel configurationModel) {
        dataManager.remove(modelToEntity(configurationModel, null));
    }

    @Override
    public void save(FilterConfigurationModel configurationModel) {
        FilterConfiguration entity = loadInternal(configurationModel.getConfigurationId(),
                configurationModel.getComponentId(),
                currentUserSubstitution.getEffectiveUser().getUsername());

        entity = modelToEntity(configurationModel, entity);

        dataManager.save(entity);
    }

    @Override
    @Nullable
    public FilterConfigurationModel load(String configurationId, String componentId, String username) {
        FilterConfiguration entity = loadInternal(configurationId, componentId, username);

        return entity == null ? null : entityToModel(entity);
    }

    @Override
    public List<FilterConfigurationModel> load(String componentId, String username) {
        List<FilterConfiguration> entities = dataManager.load(FilterConfiguration.class)
                .condition(LogicalCondition.and()
                        .add(PropertyCondition.equal("componentId", componentId).skipNullOrEmpty())
                        .add(LogicalCondition.or()
                                .add(PropertyCondition.isSet("username", false).skipNullOrEmpty())
                                .add(PropertyCondition.equal("username", username).skipNullOrEmpty())))
                .list();

        return entities.stream()
                .map(this::entityToModel)
                .toList();
    }

    @Nullable
    protected FilterConfiguration loadInternal(String configurationId, String componentId, String username) {
        return dataManager.load(FilterConfiguration.class)
                .condition(LogicalCondition.and()
                        .add(PropertyCondition.equal("configurationId", configurationId).skipNullOrEmpty())
                        .add(PropertyCondition.equal("componentId", componentId).skipNullOrEmpty())
                        .add(LogicalCondition.or()
                                .add(PropertyCondition.isSet("username", false).skipNullOrEmpty())
                                .add(PropertyCondition.equal("username", username).skipNullOrEmpty())))
                .optional()
                .orElse(null);
    }

    @SuppressWarnings({"SpringEventListenerInspection"})
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    protected void onUserRemove(UserRemovedEvent event) {
        List<FilterConfiguration> configurations = dataManager.load(FilterConfiguration.class)
                .query("e.username = :username")
                .parameter("username", event.getUsername())
                .list();
        dataManager.remove(configurations.toArray());
    }

    private FilterConfiguration modelToEntity(FilterConfigurationModel model, @Nullable FilterConfiguration destination) {
        FilterConfiguration entity = destination;
        if (entity == null) {
            entity = dataManager.create(FilterConfiguration.class);
            entity.setId(model.getId());
            entityStates.setNew(entity, entityStates.isNew(model));
        }

        entity.setId(model.getId());
        entity.setComponentId(model.getComponentId());
        entity.setConfigurationId(model.getConfigurationId());
        entity.setName(model.getName());
        entity.setUsername(model.getUsername());
        entity.setDefaultForAll(model.getDefaultForAll());
        entity.setDefaultForMe(model.getDefaultForMe());
        entity.setRootCondition(model.getRootCondition());
        entity.setSysTenantId(model.getSysTenantId());

        return entity;
    }

    private FilterConfigurationModel entityToModel(FilterConfiguration entity) {
        FilterConfigurationModel model = dataManager.create(FilterConfigurationModel.class);

        model.setId(entity.getId());
        model.setComponentId(entity.getComponentId());
        model.setConfigurationId(entity.getConfigurationId());
        model.setName(entity.getName());
        model.setUsername(entity.getUsername());
        model.setDefaultForAll(entity.getDefaultForAll());
        model.setDefaultForMe(entity.getDefaultForMe());
        model.setRootCondition(entity.getRootCondition());
        model.setSysTenantId(entity.getSysTenantId());

        entityStates.setNew(model, false);
        return model;
    }
}
