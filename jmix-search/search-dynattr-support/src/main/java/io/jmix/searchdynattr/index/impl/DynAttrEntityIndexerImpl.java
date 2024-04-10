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

package io.jmix.searchdynattr.index.impl;

import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanBuilder;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.DynAttrQueryHints;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.impl.EntityIndexerImpl;
import io.jmix.search.index.mapping.MappingFieldDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.jmix.searchdynattr.SearchDynAttrSupportConstants.DYN_ATTR_PREFIX;

@Primary
@Component("search_dynattr_support_DynAttrEntityIndexerImpl")
public class DynAttrEntityIndexerImpl extends EntityIndexerImpl {
    private static final Logger log = LoggerFactory.getLogger(DynAttrEntityIndexerImpl.class);
    @Override
    protected List<Object> reloadEntityListWithPrimaryKeyByIds(List<Object> entityIds, MetaClass metaClass, FetchPlan fetchPlan) {
        return entityIds.stream()
                .map(id -> dataManager
                        .load(metaClass.getJavaClass())
                        .id(id)
                        .fetchPlan(fetchPlan)
                        .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
                        .optional())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    protected List<Object> reloadEntityListByIds(List<Object> entityIds, MetaClass metaClass, FetchPlan fetchPlan) {
        String primaryKeyName = metadataTools.getPrimaryKeyName(metaClass);
        String discriminatorCondition = metaClass.getDescendants().isEmpty() ? "" : " and TYPE(e) = " + metaClass.getName();
        String queryString = "select e from " + metaClass.getName() + " e where e." + primaryKeyName + " in :ids" + discriminatorCondition;
        return dataManager
                .load(metaClass.getJavaClass())
                .query(queryString)
                .parameter("ids", entityIds)
                .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
                .fetchPlan(fetchPlan)
                .list();
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    protected FetchPlan createFetchPlan(IndexConfiguration indexConfiguration) {
        FetchPlanBuilder fetchPlanBuilder = fetchPlans.builder(indexConfiguration.getEntityClass());
        indexConfiguration.getMapping().getFields().values().forEach(field -> {
            if (isDynAttrField(field)) {
                return;
            }
            log.trace("Add property to fetch plan: {}", field.getEntityPropertyFullName());
            fetchPlanBuilder.add(field.getEntityPropertyFullName());
            field.getInstanceNameRelatedProperties().forEach(instanceNameRelatedProperty -> {
                log.trace("Add instance name related property to fetch plan: {}", instanceNameRelatedProperty.toPathString());
                if (instanceNameRelatedProperty.getRange().isClass()) {
                    fetchPlanBuilder.add(instanceNameRelatedProperty.toPathString(), FetchPlan.INSTANCE_NAME);
                } else {
                    fetchPlanBuilder.add(instanceNameRelatedProperty.toPathString());
                }
            });
        });

        indexConfiguration.getMapping()
                .getDisplayedNameDescriptor()
                .getInstanceNameRelatedProperties()
                .forEach(instanceNameRelatedProperty -> {
                    log.trace("Add instance name related property (displayed name) to fetch plan: {}", instanceNameRelatedProperty.toPathString());
                    if (instanceNameRelatedProperty.getRange().isClass()) {
                        fetchPlanBuilder.add(instanceNameRelatedProperty.toPathString(), FetchPlan.INSTANCE_NAME);
                    } else {
                        fetchPlanBuilder.add(instanceNameRelatedProperty.toPathString());
                    }
                });

        return fetchPlanBuilder.build();
    }

    private boolean isDynAttrField(MappingFieldDescriptor field) {
        return field.getEntityPropertyFullName().startsWith(DYN_ATTR_PREFIX);
    }
}
