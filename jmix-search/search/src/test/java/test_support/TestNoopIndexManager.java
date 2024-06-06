package test_support;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.impl.BaseIndexManager;
import io.jmix.search.index.impl.IndexStateRegistry;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import org.springframework.lang.NonNull;

public class TestNoopIndexManager extends BaseIndexManager {

    protected TestNoopIndexManager(IndexConfigurationManager indexConfigurationManager,
                                   IndexStateRegistry indexStateRegistry,
                                   SearchProperties searchProperties) {
        super(indexConfigurationManager, indexStateRegistry, searchProperties);
    }

    @Override
    protected boolean isIndexActual(IndexConfiguration indexConfiguration) {
        return true;
    }

    @Override
    public boolean createIndex(IndexConfiguration indexConfiguration) {
        return true;
    }

    @Override
    public boolean dropIndex(String indexName) {
        return true;
    }

    @Override
    public boolean isIndexExist(String indexName) {
        return true;
    }

    @Override
    public ObjectNode getIndexMetadata(@NonNull String indexName) {
        return objectMapper.createObjectNode();
    }
}
