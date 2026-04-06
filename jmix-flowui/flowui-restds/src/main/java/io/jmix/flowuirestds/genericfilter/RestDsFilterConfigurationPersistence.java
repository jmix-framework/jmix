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

package io.jmix.flowuirestds.genericfilter;

import io.jmix.core.DataManager;
import io.jmix.core.EntityStates;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.flowui.component.genericfilter.FilterConfigurationPersistence;
import io.jmix.flowui.component.genericfilter.model.FilterConfigurationModel;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("flowui_RestDsFilterConfigurationPersistence")
public class RestDsFilterConfigurationPersistence implements FilterConfigurationPersistence {

    private final DataManager dataManager;
    private final EntityStates entityStates;

    public RestDsFilterConfigurationPersistence(DataManager dataManager, EntityStates entityStates) {
        this.dataManager = dataManager;
        this.entityStates = entityStates;
    }

    @Override
    public void remove(FilterConfigurationModel configurationModel) {
        dataManager.remove(modelToEntity(configurationModel));
    }

    @Override
    public void save(FilterConfigurationModel configurationModel) {
        dataManager.save(modelToEntity(configurationModel));
    }

    @Override
    @Nullable
    public FilterConfigurationModel load(String configurationId, String componentId, String username) {
        FilterConfiguration entity = dataManager.load(FilterConfiguration.class)
                .condition(LogicalCondition.and()
                        .add(PropertyCondition.equal("configurationId", configurationId).skipNullOrEmpty())
                        .add(PropertyCondition.equal("componentId", componentId).skipNullOrEmpty())
                        .add(LogicalCondition.or()
                                .add(PropertyCondition.isSet("username", false).skipNullOrEmpty())
                                .add(PropertyCondition.equal("username", username).skipNullOrEmpty())))
// TODO KK: replace when https://github.com/jmix-framework/jmix/issues/3975 is fixed
//                .optional()
//                .orElse(null);
                .list()
                .stream()
                .findFirst()
                .orElse(null);

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

    private FilterConfiguration modelToEntity(FilterConfigurationModel model) {
        FilterConfiguration entity = dataManager.create(FilterConfiguration.class);

        entity.setId(model.getId());
        entity.setComponentId(model.getComponentId());
        entity.setConfigurationId(model.getConfigurationId());
        entity.setName(model.getName());
        entity.setUsername(model.getUsername());
        entity.setDefaultForAll(model.getDefaultForAll());
        entity.setDefaultForMe(model.getDefaultForMe());
        entity.setRootCondition(model.getRootCondition());
        entity.setSysTenantId(model.getSysTenantId());

        entityStates.setNew(entity, entityStates.isNew(model));
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
