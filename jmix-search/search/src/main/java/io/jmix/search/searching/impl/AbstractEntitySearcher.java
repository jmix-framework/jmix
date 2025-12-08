/*
 * Copyright 2025 Haulmont.
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

package io.jmix.search.searching.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterables;
import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.core.IdSerialization;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.search.SearchProperties;
import io.jmix.search.searching.EntitySearcher;
import io.jmix.search.searching.FieldHit;
import io.jmix.search.searching.SearchContext;
import io.jmix.search.searching.SearchResultEntry;
import io.jmix.search.utils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractEntitySearcher implements EntitySearcher {

    private static final Logger log = LoggerFactory.getLogger(AbstractEntitySearcher.class);

    protected static final TypeReference<Map<String, Object>> GENERIC_MAP_TYPE_REF = new TypeReference<>() {
    };

    protected final MetadataTools metadataTools;
    protected final SearchProperties searchProperties;
    protected final DataManager secureDataManager;
    protected final IdSerialization idSerialization;
    protected final ObjectMapper objectMapper;

    public AbstractEntitySearcher(MetadataTools metadataTools,
                                  SearchProperties searchProperties,
                                  DataManager secureDataManager,
                                  IdSerialization idSerialization) {
        this.metadataTools = metadataTools;
        this.searchProperties = searchProperties;
        this.secureDataManager = secureDataManager;
        this.idSerialization = idSerialization;
        this.objectMapper = new ObjectMapper();
    }

    protected boolean isResultFull(SearchResultImpl searchResultImpl, SearchContext searchContext) {
        return searchResultImpl.getSize() >= searchContext.getSize();
    }

    @Nullable
    protected Map<String, Object> objectNodeToMap(@Nullable ObjectNode node) {
        if (node == null) {
            return null;
        }
        return objectMapper.convertValue(node, GENERIC_MAP_TYPE_REF);
    }

    protected SearchResultEntry createSearchResultEntry(String entityId, String instanceName, String entityName, Map<String, List<String>> highlight) {
        Map<String, List<String>> highlightFields = highlight;
        List<FieldHit> fieldHits = new ArrayList<>();
        highlightFields.forEach((f, h) -> {
            if (isDisplayedField(f)) {
                String highlights = String.join("...", h);
                fieldHits.add(new FieldHit(formatFieldName(f), highlights));
            }
        });
        return new SearchResultEntry(entityId, instanceName, entityName, fieldHits);
    }

    protected boolean isDisplayedField(String fieldName) {
        return !Constants.INSTANCE_NAME_FIELD.equals(fieldName);
    }

    protected String formatFieldName(String fieldName) {
        return StringUtils.removeEnd(fieldName, "." + Constants.INSTANCE_NAME_FIELD);
    }

    protected Set<String> reloadIds(MetaClass metaClass, Collection<Object> entityIds) {
        Set<String> result = new HashSet<>();
        String primaryKeyName = metadataTools.getPrimaryKeyName(metaClass);
        for (Collection<Object> idsPartition : Iterables.partition(entityIds, searchProperties.getSearchReloadEntitiesBatchSize())) {
            log.debug("Load instance names for ids: {}", idsPartition);

            List<Object> partitionResult;
            if (metadataTools.hasCompositePrimaryKey(metaClass)) {
                partitionResult = idsPartition.stream()
                        .map(id -> secureDataManager
                                .load(metaClass.getJavaClass())
                                .id(id)
                                .fetchPlanProperties(primaryKeyName)
                                .optional())
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
            } else {
                partitionResult = secureDataManager
                        .load(metaClass.getJavaClass())
                        .query("select e from " + metaClass.getName() + " e where e." + primaryKeyName + " in :ids")
                        .parameter("ids", idsPartition)
                        .fetchPlanProperties(primaryKeyName)
                        .list();
            }

            partitionResult.stream()
                    .map(instance -> idSerialization.idToString(Id.of(instance)))
                    .forEach(result::add);
        }
        return result;
    }
}
