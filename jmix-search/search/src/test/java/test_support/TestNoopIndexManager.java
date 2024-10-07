package test_support;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.impl.BaseIndexManager;
import io.jmix.search.index.impl.IndexStateRegistry;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.index.mapping.IndexMappingConfiguration;
import org.springframework.lang.NonNull;

public class TestNoopIndexManager extends BaseIndexManager {

    protected TestNoopIndexManager(IndexConfigurationManager indexConfigurationManager,
                                   IndexStateRegistry indexStateRegistry,
                                   SearchProperties searchProperties) {
        super(indexConfigurationManager, null, null, null, null);
    }

    @Override
    public boolean createIndex(@NonNull IndexConfiguration indexConfiguration) {
        return true;
    }

    @Override
    public boolean dropIndex(@NonNull String indexName) {
        return true;
    }

    @Override
    public boolean isIndexExist(@NonNull String indexName) {
        return true;
    }

    @Override
    public ObjectNode getIndexMetadata(@NonNull String indexName) {
        return objectMapper.createObjectNode();
    }

    @Override
    public boolean putMapping(String indexName, IndexMappingConfiguration mapping) {
        return false;
    }
}
