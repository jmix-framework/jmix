package io.jmix.search.index.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.core.common.util.Preconditions;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.*;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Contains non-platform-specific operations.
 * Interaction with indexes is performed in platform-specific implementations.
 */
public abstract class BaseIndexManager implements IndexManager {

    private static final Logger log = LoggerFactory.getLogger(BaseIndexManager.class);

    protected final IndexConfigurationManager indexConfigurationManager;
    protected final IndexStateRegistry indexStateRegistry;
    protected final SearchProperties searchProperties;

    protected final ObjectMapper objectMapper;

    protected final TypeReference<Map<String, Object>> MAP_TYPE_REF = new TypeReference<>() {
    };

    protected BaseIndexManager(IndexConfigurationManager indexConfigurationManager,
                               IndexStateRegistry indexStateRegistry,
                               SearchProperties searchProperties) {
        this.indexConfigurationManager = indexConfigurationManager;
        this.indexStateRegistry = indexStateRegistry;
        this.searchProperties = searchProperties;
        this.objectMapper = new ObjectMapper();
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
            if (isIndexActual(indexConfiguration)) {
                status = IndexValidationStatus.ACTUAL;
                indexStateRegistry.markIndexAsAvailable(indexConfiguration.getEntityName());
            } else {
                status = IndexValidationStatus.IRRELEVANT;
                indexStateRegistry.markIndexAsUnavailable(indexConfiguration.getEntityName());
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

    protected abstract boolean isIndexActual(IndexConfiguration indexConfiguration);

    protected IndexSynchronizationStatus synchronizeIndexSchema(IndexConfiguration indexConfiguration, IndexSchemaManagementStrategy strategy) {
        log.info("Synchronize search index '{}' (entity '{}') according to strategy '{}'",
                indexConfiguration.getIndexName(), indexConfiguration.getEntityName(), strategy);
        IndexSynchronizationStatus status;
        boolean indexExist = isIndexExist(indexConfiguration.getIndexName());
        if (indexExist) {
            boolean indexActual = isIndexActual(indexConfiguration);
            if (indexActual) {
                status = IndexSynchronizationStatus.ACTUAL;
                indexStateRegistry.markIndexAsAvailable(indexConfiguration.getEntityName());
            } else {
                status = handleIrrelevantIndex(indexConfiguration, strategy);
            }
        } else {
            status = handleMissingIndex(indexConfiguration, strategy);
        }
        log.info("Synchronization status of search index '{}' (entity '{}'): {}",
                indexConfiguration.getIndexName(), indexConfiguration.getEntityName(), status);
        return status;
    }

    protected IndexSynchronizationStatus handleIrrelevantIndex(IndexConfiguration indexConfiguration, IndexSchemaManagementStrategy strategy) {
        IndexSynchronizationStatus status;
        if (IndexSchemaManagementStrategy.CREATE_OR_RECREATE.equals(strategy)) {
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

        if (IndexSchemaManagementStrategy.NONE.equals(strategy)) {
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

    protected boolean nodeContains(ObjectNode containerNode, ObjectNode contentNode) {
        log.trace("Check if node {} contains {}", containerNode, contentNode);
        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = contentNode.fields();
        while (fieldsIterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = fieldsIterator.next();
            String fieldName = entry.getKey();
            log.trace("Check field '{}'", fieldName);
            JsonNode contentFieldValue = entry.getValue();
            JsonNode containerFieldValue;
            if (containerNode.has(fieldName)) {
                log.trace("Container has field '{}'", fieldName);
                containerFieldValue = containerNode.get(fieldName);
            } else {
                log.trace("Container doesn't have field '{}'. STOP - FALSE", fieldName);
                return false;
            }

            if (containerFieldValue == null) {
                log.trace("Container has NULL field '{}'. STOP - FALSE", fieldName);
                return false;
            }

            if (!contentFieldValue.getNodeType().equals(containerFieldValue.getNodeType())) {
                log.trace("Type of container field ({}) doesn't match the type of content field ({}). STOP - FALSE",
                        containerFieldValue.getNodeType(), contentFieldValue.getNodeType());
                return false;
            }

            if (contentFieldValue.isObject() && containerFieldValue.isObject()) {
                log.trace("Both container and content field is objects - check nested structure");
                boolean nestedResult = nodeContains((ObjectNode) containerFieldValue, (ObjectNode) contentFieldValue);
                if (!nestedResult) {
                    log.trace("Structures of the nested objects ({}) are different. STOP - FALSE", fieldName);
                    return false;
                }
            }

            if (!containerFieldValue.equals(contentFieldValue)) {
                return false;
            }
        }
        log.trace("Structures are the same. TRUE");
        return true;
    }
}
