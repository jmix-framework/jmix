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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.core.common.util.Preconditions;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.IndexManagementService;
import io.jmix.search.index.mapping.IndexConfigurationProvider;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

@Service(IndexManagementService.NAME)
public class IndexManagementServiceImpl implements IndexManagementService {

    private static final Logger log = LoggerFactory.getLogger(IndexManagementServiceImpl.class);

    @Autowired
    protected RestHighLevelClient esClient;
    @Autowired
    protected IndexConfigurationProvider indexDefinitionsProvider;

    protected ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean createIndex(@Nullable IndexConfiguration indexConfiguration) throws IOException {
        Preconditions.checkNotNullArgument(indexConfiguration);

        CreateIndexRequest request = new CreateIndexRequest(indexConfiguration.getIndexName());
        String mappingBody;
        try {
            mappingBody = objectMapper.writeValueAsString(indexConfiguration.getMapping());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to create index '" + indexConfiguration.getIndexName() + "': Failed to parse index definition", e);
        }
        request.mapping(mappingBody, XContentType.JSON);
        log.info("Create index '{}' with mapping {}", indexConfiguration.getIndexName(), mappingBody);
        CreateIndexResponse response = esClient.indices().create(request, RequestOptions.DEFAULT);
        log.info("Result of index '{}' creation: {}", indexConfiguration.getIndexName(), response.isAcknowledged() ? "Success" : "Failure");
        return response.isAcknowledged();
    }

    @Override
    public boolean dropIndex(@Nullable String indexName) throws IOException {
        Preconditions.checkNotNullArgument(indexName);

        DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        AcknowledgedResponse response = esClient.indices().delete(request, RequestOptions.DEFAULT);
        log.info("Result of index '{}' deletion: {}", indexName, response.isAcknowledged() ? "Success" : "Failure");
        return response.isAcknowledged();
    }

    @Override
    public boolean recreateIndex(@Nullable IndexConfiguration indexConfiguration) throws IOException {
        Preconditions.checkNotNullArgument(indexConfiguration);

        String indexName = indexConfiguration.getIndexName();
        if (isIndexExist(indexName)) {
            dropIndex(indexName);
        }
        return createIndex(indexConfiguration);
    }

    @Override
    public boolean isIndexExist(@Nullable String indexName) throws IOException {
        Preconditions.checkNotNullArgument(indexName);

        GetIndexRequest request = new GetIndexRequest(indexName);
        return esClient.indices().exists(request, RequestOptions.DEFAULT);
    }

    @Override
    public boolean isIndexActual(@Nullable IndexConfiguration indexConfiguration) throws IOException {
        Preconditions.checkNotNullArgument(indexConfiguration);

        GetIndexResponse index = getIndex(indexConfiguration.getIndexName());
        Map<String, MappingMetadata> mappings = index.getMappings();
        MappingMetadata mappingMetadata = mappings.get(indexConfiguration.getIndexName());
        Map<String, Object> currentMapping = mappingMetadata.getSourceAsMap();
        log.debug("Current mapping of index '{}': {}", indexConfiguration.getIndexName(), currentMapping);

        Map<String, Object> actualMapping = objectMapper.convertValue(indexConfiguration.getMapping(), new TypeReference<Map<String, Object>>() {
        });
        log.debug("Actual mapping of index '{}': {}", indexConfiguration.getIndexName(), actualMapping);

        return actualMapping.equals(currentMapping);
    }

    @Override
    public GetIndexResponse getIndex(@Nullable String indexName) throws IOException {
        Preconditions.checkNotNullArgument(indexName);

        GetIndexRequest request = new GetIndexRequest(indexName);
        return esClient.indices().get(request, RequestOptions.DEFAULT);
    }

    @Override
    public void prepareIndexes() {
        log.info("Prepare search indexes");
        Collection<IndexConfiguration> indexConfigurations = indexDefinitionsProvider.getIndexConfigurations();
        indexConfigurations.forEach(this::prepareIndex);
    }

    @Override
    public void prepareIndex(@Nullable IndexConfiguration indexConfiguration) {
        Preconditions.checkNotNullArgument(indexConfiguration);

        log.info("Prepare search index '{}'", indexConfiguration.getIndexName());
        try {
            boolean indexExist = isIndexExist(indexConfiguration.getIndexName());
            if (indexExist) {
                log.info("Index '{}' already exists", indexConfiguration.getIndexName());
                //todo compare mapping & settings
                if (isIndexActual(indexConfiguration)) {
                    log.info("Index '{}' has actual configuration", indexConfiguration.getIndexName());
                } else {
                    log.info("Index '{}' has irrelevant configuration", indexConfiguration.getIndexName());
                    dropIndex(indexConfiguration.getIndexName());
                    createIndex(indexConfiguration);
                }
            } else {
                log.info("Index '{}' does not exists. Create", indexConfiguration.getIndexName());
                createIndex(indexConfiguration);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to prepare index '" + indexConfiguration.getIndexName() + "'", e);
        }
    }
}
