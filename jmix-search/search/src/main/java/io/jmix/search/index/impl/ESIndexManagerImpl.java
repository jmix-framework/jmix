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

package io.jmix.search.index.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.core.common.util.Preconditions;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.*;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.mapping.IndexMappingConfiguration;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.*;
import org.elasticsearch.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component("search_ESIndexManager")
public class ESIndexManagerImpl implements ESIndexManager {

    private static final Logger log = LoggerFactory.getLogger(ESIndexManagerImpl.class);

    @Autowired
    protected RestHighLevelClient esClient;
    @Autowired
    protected IndexConfigurationManager indexConfigurationManager;
    @Autowired
    protected SearchProperties searchProperties;
    @Autowired
    protected IndexStateRegistry indexStateRegistry;
    @Autowired
    private IndexConfigurationComparator indexConfigurationComparator;

    protected ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean createIndex(IndexConfiguration indexConfiguration) {
        Preconditions.checkNotNullArgument(indexConfiguration);

        CreateIndexRequest request = new CreateIndexRequest(indexConfiguration.getIndexName());
        String mappingBody;
        try {
            mappingBody = objectMapper.writeValueAsString(indexConfiguration.getMapping());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to create index '" + indexConfiguration.getIndexName() + "': Failed to parse index definition", e);
        }
        request.mapping(mappingBody, XContentType.JSON);
        request.settings(indexConfiguration.getSettings());
        log.info("Create index '{}' with mapping {}", indexConfiguration.getIndexName(), mappingBody);
        CreateIndexResponse response;
        try {
            response = esClient.indices().create(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create index '" + indexConfiguration.getIndexName() + "': Request failed", e);
        }
        log.info("Result of index '{}' creation: {}", indexConfiguration.getIndexName(), response.isAcknowledged() ? "Success" : "Failure");
        if (response.isAcknowledged()) {
            indexStateRegistry.markIndexAsAvailable(indexConfiguration.getEntityName());
        }
        return response.isAcknowledged();
    }

    @Override
    public boolean dropIndex(String indexName) {
        Preconditions.checkNotNullArgument(indexName);

        IndexConfiguration indexConfiguration = indexConfigurationManager.getIndexConfigurationByIndexName(indexName);
        DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        AcknowledgedResponse response;
        try {
            indexStateRegistry.markIndexAsUnavailable(indexConfiguration.getEntityName());
            response = esClient.indices().delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("Unable to delete index '" + indexName + "': Request failed", e);
        }
        log.info("Result of index '{}' deletion: {}", indexName, response.isAcknowledged() ? "Success" : "Failure");
        return response.isAcknowledged();
    }

    @Override
    public Map<IndexConfiguration, Boolean> recreateIndexes() {
        Collection<IndexConfiguration> indexConfigurations = indexConfigurationManager.getAllIndexConfigurations();
        return recreateIndexes(indexConfigurations);
    }

    @Override
    public Map<IndexConfiguration, Boolean> recreateIndexes(Collection<IndexConfiguration> indexConfigurations) {
        Preconditions.checkNotNullArgument(indexConfigurations);

        Map<IndexConfiguration, Boolean> result = new HashMap<>();
        indexConfigurations.forEach(config -> {
            boolean created = recreateIndex(config);
            result.put(config, created);
        });
        return result;
    }

    @Override
    public boolean recreateIndex(IndexConfiguration indexConfiguration) {
        Preconditions.checkNotNullArgument(indexConfiguration);

        String indexName = indexConfiguration.getIndexName();
        if (isIndexExist(indexName)) {
            boolean dropped = dropIndex(indexName);
            if (!dropped) {
                return false;
            }
        }
        return createIndex(indexConfiguration);
    }

    @Override
    public boolean isIndexExist(String indexName) {
        Preconditions.checkNotNullArgument(indexName);

        GetIndexRequest request = new GetIndexRequest(indexName);
        try {
            return esClient.indices().exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("Unable to check existence of index '" + indexName + "': Request failed", e);
        }
    }

    @Override
    public Map<IndexConfiguration, IndexValidationStatus> validateIndexes() {
        Collection<IndexConfiguration> indexConfigurations = indexConfigurationManager.getAllIndexConfigurations();
        return validateIndexes(indexConfigurations);
    }

    @Override
    public Map<IndexConfiguration, IndexValidationStatus> validateIndexes(Collection<IndexConfiguration> indexConfigurations) {
        Preconditions.checkNotNullArgument(indexConfigurations);

        Map<IndexConfiguration, IndexValidationStatus> result = new HashMap<>();
        indexConfigurations.forEach(config -> {
            IndexValidationStatus status = validateIndex(config);
            result.put(config, status);
        });
        return result;
    }

    @Override
    public IndexValidationStatus validateIndex(IndexConfiguration indexConfiguration) {
        Preconditions.checkNotNullArgument(indexConfiguration);

        IndexValidationStatus status;
        if (isIndexExist(indexConfiguration.getIndexName())) {
            IndexConfigurationComparator.ConfigurationComparingResult result = compareWithAppliedIndexConfiguration(indexConfiguration);
            if (result.isIndexRecreatingRequired()) {
                status = IndexValidationStatus.IRRELEVANT;
                indexStateRegistry.markIndexAsUnavailable(indexConfiguration.getEntityName());
            } else {
                status = IndexValidationStatus.ACTUAL;
                indexStateRegistry.markIndexAsAvailable(indexConfiguration.getEntityName());
            }
        } else {
            status = IndexValidationStatus.MISSING;
            indexStateRegistry.markIndexAsUnavailable(indexConfiguration.getEntityName());
        }

        log.info("Validation status of search index '{}' (entity '{}'): {}",
                indexConfiguration.getIndexName(), indexConfiguration.getEntityName(), status);
        return status;
    }

    @Override
    public GetIndexResponse getIndex(String indexName) {
        Preconditions.checkNotNullArgument(indexName);

        GetIndexRequest request = new GetIndexRequest(indexName);
        try {
            return esClient.indices().get(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("Unable to get info of index '" + indexName + "': Request failed", e);
        }
    }

    @Override
    public Map<IndexConfiguration, IndexSynchronizationStatus> synchronizeIndexSchemas() {
        Collection<IndexConfiguration> indexConfigurations = indexConfigurationManager.getAllIndexConfigurations();
        return synchronizeIndexSchemas(indexConfigurations);
    }

    @Override
    public Map<IndexConfiguration, IndexSynchronizationStatus> synchronizeIndexSchemas(Collection<IndexConfiguration> indexConfigurations) {
        Preconditions.checkNotNullArgument(indexConfigurations);

        Map<IndexConfiguration, IndexSynchronizationStatus> result = new HashMap<>();
        indexConfigurations.forEach(config -> {
            IndexSynchronizationStatus status = synchronizeIndexSchema(config);
            result.put(config, status);
        });
        return result;
    }

    @Override
    public IndexSynchronizationStatus synchronizeIndexSchema(IndexConfiguration indexConfiguration) {
        Preconditions.checkNotNullArgument(indexConfiguration);

        IndexSchemaManagementStrategy strategy = searchProperties.getIndexSchemaManagementStrategy();
        return synchronizeIndexSchema(indexConfiguration, strategy);
    }

    @Override
    public boolean putMapping(String indexName, IndexMappingConfiguration mapping) {
        PutMappingRequest request = new PutMappingRequest(indexName);

        String mappingBody;
        try {
            mappingBody = objectMapper.writeValueAsString(mapping);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to update index mapping'" + indexName + "': Failed to parse index mapping.", e);
        }

        request.source(mappingBody, XContentType.JSON);
        AcknowledgedResponse response;
        try {
            response = esClient.indices().putMapping(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("Problem with sending request to elastic search server.", e);
        }
        return response.isAcknowledged();
    }

    protected IndexSynchronizationStatus synchronizeIndexSchema(IndexConfiguration indexConfiguration, IndexSchemaManagementStrategy strategy) {
        log.info("Synchronize search index '{}' (entity '{}') according to strategy '{}'",
                indexConfiguration.getIndexName(), indexConfiguration.getEntityName(), strategy);
        IndexSynchronizationStatus status;
        boolean indexExist = isIndexExist(indexConfiguration.getIndexName());
        if (indexExist) {
            IndexConfigurationComparator.ConfigurationComparingResult result = compareWithAppliedIndexConfiguration(indexConfiguration);
            if (result.isIndexRecreatingRequired()) {
                status = handleIrrelevantIndex(indexConfiguration, strategy);
            } else {
                if (result.isConfigurationUpdateRequired()){
                    status = updateIndexConfiguration(indexConfiguration, strategy, result);
                }else {
                    status = IndexSynchronizationStatus.ACTUAL;
                    indexStateRegistry.markIndexAsAvailable(indexConfiguration.getEntityName());
                }

            }
        } else {
            status = handleMissingIndex(indexConfiguration, strategy);
        }
        log.info("Synchronization status of search index '{}' (entity '{}'): {}",
                indexConfiguration.getIndexName(), indexConfiguration.getEntityName(), status);
        return status;
    }

    protected IndexSynchronizationStatus updateIndexConfiguration(IndexConfiguration indexConfiguration, IndexSchemaManagementStrategy strategy, IndexConfigurationComparator.ConfigurationComparingResult result) {
        if(strategy.canUpdateConfiguration()) {
            if(result.isMappingUpdateRequired()){
                boolean mappingSavingResult = putMapping(indexConfiguration.getIndexName(), indexConfiguration.getMapping());
                if (mappingSavingResult) {
                    indexStateRegistry.markIndexAsAvailable(indexConfiguration.getEntityName());
                    return IndexSynchronizationStatus.UPDATED;
                }else{
                    log.error("Problem with index mapping saving.");
                    indexStateRegistry.markIndexAsUnavailable(indexConfiguration.getEntityName());
                    return IndexSynchronizationStatus.IRRELEVANT;
                }
            }
            throw new IllegalStateException("Only index mapping update is supported currently");
        } else {
            indexStateRegistry.markIndexAsUnavailable(indexConfiguration.getEntityName());
            return IndexSynchronizationStatus.IRRELEVANT;
        }
    }

    protected IndexSynchronizationStatus handleIrrelevantIndex(IndexConfiguration indexConfiguration, IndexSchemaManagementStrategy strategy) {
        IndexSynchronizationStatus status;
        if (strategy.canRecreateIndex()) {
            boolean created = recreateIndex(indexConfiguration);
            if (created) {
                status = IndexSynchronizationStatus.RECREATED;
                indexStateRegistry.markIndexAsAvailable(indexConfiguration.getEntityName());
            } else {
                status = IndexSynchronizationStatus.IRRELEVANT;
                indexStateRegistry.markIndexAsUnavailable(indexConfiguration.getEntityName());
            }
        } else {
            status = IndexSynchronizationStatus.IRRELEVANT;
            indexStateRegistry.markIndexAsUnavailable(indexConfiguration.getEntityName());
        }
        return status;
    }

    protected IndexSynchronizationStatus handleMissingIndex(IndexConfiguration indexConfiguration, IndexSchemaManagementStrategy strategy) {
        IndexSynchronizationStatus status;

        if (!strategy.canCreateIndex()) {
            status = IndexSynchronizationStatus.MISSING;
        } else {
            boolean created = createIndex(indexConfiguration);
            if (created) {
                status = IndexSynchronizationStatus.CREATED;
                indexStateRegistry.markIndexAsAvailable(indexConfiguration.getEntityName());
            } else {
                status = IndexSynchronizationStatus.MISSING;
                indexStateRegistry.markIndexAsUnavailable(indexConfiguration.getEntityName());
            }
        }
        return status;
    }

    protected IndexConfigurationComparator.ConfigurationComparingResult compareWithAppliedIndexConfiguration(IndexConfiguration indexConfiguration) {
        Preconditions.checkNotNullArgument(indexConfiguration);
        GetIndexResponse indexResponse = getIndex(indexConfiguration.getIndexName());
        return indexConfigurationComparator.compareConfigurations(indexConfiguration, indexResponse);
    }


}
