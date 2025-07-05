package io.jmix.search.index.impl.stub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.IndexManager;
import io.jmix.search.index.IndexSynchronizationStatus;
import io.jmix.search.index.IndexValidationStatus;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.Map;

public class IndexManagerStub implements IndexManager {

    protected ObjectMapper objectMapper;

    public IndexManagerStub() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean createIndex(IndexConfiguration indexConfiguration) {
        return false;
    }

    @Override
    public boolean dropIndex(String indexName) {
        return false;
    }

    @Override
    public Map<IndexConfiguration, Boolean> recreateIndexes() {
        return Map.of();
    }

    @Override
    public Map<IndexConfiguration, Boolean> recreateIndexes(Collection<IndexConfiguration> indexConfigurations) {
        return Map.of();
    }

    @Override
    public boolean recreateIndex(IndexConfiguration indexConfiguration) {
        return false;
    }

    @Override
    public boolean isIndexExist(String indexName) {
        return false;
    }

    @Override
    public Map<IndexConfiguration, IndexValidationStatus> validateIndexes() {
        return Map.of();
    }

    @Override
    public Map<IndexConfiguration, IndexValidationStatus> validateIndexes(Collection<IndexConfiguration> indexConfigurations) {
        return Map.of();
    }

    @Override
    public IndexValidationStatus validateIndex(IndexConfiguration indexConfiguration) {
        return IndexValidationStatus.ACTUAL;
    }

    @Override
    public ObjectNode getIndexMetadata(@NonNull String indexName) {
        return objectMapper.createObjectNode();
    }

    @Override
    public Map<IndexConfiguration, IndexSynchronizationStatus> synchronizeIndexSchemas() {
        return Map.of();
    }

    @Override
    public Map<IndexConfiguration, IndexSynchronizationStatus> synchronizeIndexSchemas(Collection<IndexConfiguration> indexConfigurations) {
        return Map.of();
    }

    @Override
    public IndexSynchronizationStatus synchronizeIndexSchema(IndexConfiguration indexConfiguration) {
        return IndexSynchronizationStatus.ACTUAL;
    }
}
