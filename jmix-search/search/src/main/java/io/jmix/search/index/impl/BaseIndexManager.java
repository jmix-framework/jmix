package io.jmix.search.index.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.core.common.util.Preconditions;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.*;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains non-platform-specific operations.
 * Interaction with indexes is performed in platform-specific implementations.
 */
public abstract class BaseIndexManager<TState, TSettings, TJsonp> implements IndexManager {

    private static final Logger log = LoggerFactory.getLogger(BaseIndexManager.class);

    protected final IndexConfigurationManager indexConfigurationManager;
    protected final IndexStateRegistry indexStateRegistry;
    protected final SearchProperties searchProperties;

    protected final ObjectMapper objectMapper;

    protected final IndexConfigurationComparator<TState, TSettings, TJsonp> indexConfigurationComparator;
    protected final MetadataResolver<TState, TJsonp> metadataResolver;

    protected BaseIndexManager(IndexConfigurationManager indexConfigurationManager,
                               IndexStateRegistry indexStateRegistry,
                               SearchProperties searchProperties,
                               IndexConfigurationComparator<TState, TSettings, TJsonp> indexConfigurationComparator,
                               MetadataResolver<TState, TJsonp> metadataResolver) {
        this.indexConfigurationManager = indexConfigurationManager;
        this.indexStateRegistry = indexStateRegistry;
        this.searchProperties = searchProperties;
        this.indexConfigurationComparator = indexConfigurationComparator;
        this.metadataResolver = metadataResolver;
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
            IndexConfigurationComparator.ConfigurationComparingResult result = indexConfigurationComparator.compareConfigurations(indexConfiguration);
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

    protected IndexSynchronizationStatus synchronizeIndexSchema(IndexConfiguration indexConfiguration, IndexSchemaManagementStrategy strategy) {
        log.info("Synchronize search index '{}' (entity '{}') according to strategy '{}'",
                indexConfiguration.getIndexName(), indexConfiguration.getEntityName(), strategy);
        IndexSynchronizationStatus status;
        boolean indexExist = isIndexExist(indexConfiguration.getIndexName());
        if (indexExist) {
            IndexConfigurationComparator.ConfigurationComparingResult result = indexConfigurationComparator.compareConfigurations(indexConfiguration);
            if (result.isIndexRecreatingRequired()) {
                status = handleIrrelevantIndex(indexConfiguration, strategy);
            } else {
                if (result.isConfigurationUpdateRequired()) {
                    status = updateIndexConfiguration(indexConfiguration, strategy, result);
                } else {
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

    protected IndexSynchronizationStatus handleIrrelevantIndex(IndexConfiguration indexConfiguration, IndexSchemaManagementStrategy strategy) {
        IndexSynchronizationStatus status;
        if (strategy.isIndexRecreationSupported()) {
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

        if (strategy.isIndexCreationSupported()) {
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

    protected IndexSynchronizationStatus updateIndexConfiguration(IndexConfiguration indexConfiguration, IndexSchemaManagementStrategy strategy, IndexConfigurationComparator.ConfigurationComparingResult result) {
        if (strategy.isConfigurationUpdateSupported()) {
            if (result.isMappingUpdateRequired()) {
                boolean mappingSavingResult = putMapping(indexConfiguration.getIndexName(), indexConfiguration.getMapping());
                if (mappingSavingResult) {
                    indexStateRegistry.markIndexAsAvailable(indexConfiguration.getEntityName());
                    return IndexSynchronizationStatus.UPDATED;
                } else {
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
}
