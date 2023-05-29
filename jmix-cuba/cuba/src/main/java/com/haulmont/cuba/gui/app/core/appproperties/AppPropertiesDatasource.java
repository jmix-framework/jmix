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

package com.haulmont.cuba.gui.app.core.appproperties;

import com.haulmont.cuba.core.app.ConfigStorageService;
import com.haulmont.cuba.core.config.AppPropertiesLocator;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.config.AppPropertyEntity;
import com.haulmont.cuba.gui.data.impl.CustomHierarchicalDatasource;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Custom datasource used in the {@code appproperties-browse.xml} screen
 */
public class AppPropertiesDatasource extends CustomHierarchicalDatasource<AppPropertyEntity, UUID> {
    @Override
    protected Collection<AppPropertyEntity> getEntities(Map<String, Object> params) {
        List<AppPropertyEntity> entities = loadAppPropertyEntities();

        String name = (String) params.get("name");
        if (StringUtils.isNotEmpty(name)) {
            entities = entities.stream()
                    .filter(it -> it.getName().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList());
        }

        return createEntitiesTree(entities);
    }

    public List<AppPropertyEntity> loadAppPropertyEntities() {
        ConfigStorageService configStorageService = AppBeans.get(ConfigStorageService.class);
        List<AppPropertyEntity> entities = configStorageService.getAppProperties();

        AppPropertiesLocator appPropertiesLocator = AppBeans.get(AppPropertiesLocator.class);
        entities.addAll(appPropertiesLocator.getAppProperties());
        return entities;
    }

    List<AppPropertyEntity> createEntitiesTree(List<AppPropertyEntity> entities) {
        List<AppPropertyEntity> resultList = new ArrayList<>();
        for (AppPropertyEntity entity : entities) {
            String[] parts = entity.getName().split("\\.");
            AppPropertyEntity parent = null;
            for (int i = 0; i < parts.length; i++) {
                String[] currParts = Arrays.copyOfRange(parts, 0, i + 1);
                String part = parts[i];
                if (i < parts.length - 1) {
                    Optional<AppPropertyEntity> parentOpt = resultList.stream()
                            .filter(e -> {
                                return e.getCategory() && nameEquals(currParts, e);
                            })
                            .findFirst();
                    if (parentOpt.isPresent()) {
                        parent = parentOpt.get();
                    } else {
                        AppPropertyEntity categoryEntity = new AppPropertyEntity();
                        categoryEntity.setParent(parent);
                        categoryEntity.setName(part);
                        resultList.add(categoryEntity);
                        parent = categoryEntity;
                    }

                } else {
                    entity.setParent(parent);
                    entity.setCategory(false);
                    resultList.add(entity);
                }
            }
        }
        // remove duplicates from global configs
        for (Iterator<AppPropertyEntity> iter = resultList.iterator(); iter.hasNext();) {
            AppPropertyEntity entity = iter.next();
            resultList.stream()
                    .filter(e -> e != entity && nameParts(e).equals(nameParts(entity)))
                    .findFirst()
                    .ifPresent(e -> iter.remove());
        }

        return resultList;
    }

    private boolean nameEquals(String[] nameParts, AppPropertyEntity entity) {
        AppPropertyEntity e = entity;
        for (int i = nameParts.length - 1; i >= 0; i--) {
            String name = nameParts[i];
            if (!e.getName().equals(name))
                return false;
            if (i > 0) {
                e = e.getParent();
                if (e == null)
                    return false;
            }
        }
        return true;
    }

    private List<String> nameParts(AppPropertyEntity entity) {
        List<String> list = new ArrayList<>();
        AppPropertyEntity e = entity;
        while (e != null) {
            list.add(e.getName());
            e = e.getParent();
        }
        Collections.reverse(list);
        return list;
    }
}
