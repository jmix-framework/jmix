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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.jmix.core.DataManager;
import io.jmix.core.DevelopmentException;
import io.jmix.core.EntityStates;
import io.jmix.core.annotation.Internal;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.core.security.event.UserRemovedEvent;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.flowui.component.genericfilter.FilterConfigurationPersistence;
import io.jmix.flowui.component.genericfilter.model.FilterConfigurationModel;
import io.jmix.flowui.entity.filter.LogicalFilterCondition;
import io.jmix.flowuidata.entity.FilterConfiguration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Internal
@Component("flowui_DatabaseFilterConfigurationPersistence")
public class DatabaseFilterConfigurationPersistence implements FilterConfigurationPersistence {

    protected final DataManager dataManager;
    protected final EntityStates entityStates;

    protected final CurrentUserSubstitution currentUserSubstitution;

    protected ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
            .build()
            .configure(SerializationFeature.INDENT_OUTPUT, true);


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

    protected FilterConfiguration modelToEntity(FilterConfigurationModel model, @Nullable FilterConfiguration destination) {
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
        entity.setRootCondition(filterConditionToJson(model.getRootCondition()));
        entity.setSysTenantId(model.getSysTenantId());

        return entity;
    }

    protected FilterConfigurationModel entityToModel(FilterConfiguration entity) {
        FilterConfigurationModel model = dataManager.create(FilterConfigurationModel.class);

        model.setId(entity.getId());
        model.setComponentId(entity.getComponentId());
        model.setConfigurationId(entity.getConfigurationId());
        model.setName(entity.getName());
        model.setUsername(entity.getUsername());
        model.setDefaultForAll(entity.getDefaultForAll());
        model.setDefaultForMe(entity.getDefaultForMe());
        model.setRootCondition(filterConditionFromJson(entity.getRootCondition()));
        model.setSysTenantId(entity.getSysTenantId());

        entityStates.setNew(model, false);
        return model;
    }

    protected String filterConditionToJson(LogicalFilterCondition filterCondition) {
        try {
            return objectMapper.writeValueAsString(filterCondition);
        } catch (JsonProcessingException e) {
            throw new DevelopmentException(e.getMessage(), e);
        }
    }

    protected LogicalFilterCondition filterConditionFromJson(String json) {
        try {
            return objectMapper.readValue(json, LogicalFilterCondition.class);
        } catch (JsonProcessingException e) {
            throw new DevelopmentException(e.getMessage(), e);
        }
    }
}
