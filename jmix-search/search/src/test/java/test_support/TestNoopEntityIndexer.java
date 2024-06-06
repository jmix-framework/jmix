package test_support;

import io.jmix.core.Id;
import io.jmix.search.index.EntityIndexer;
import io.jmix.search.index.IndexResult;

import java.util.Collection;
import java.util.Collections;

public class TestNoopEntityIndexer implements EntityIndexer {
    @Override
    public IndexResult index(Object entityInstance) {
        return new IndexResult(0, Collections.emptyList());
    }

    @Override
    public IndexResult indexCollection(Collection<Object> entityInstances) {
        return new IndexResult(0, Collections.emptyList());
    }

    @Override
    public IndexResult indexByEntityId(Id<?> entityId) {
        return new IndexResult(0, Collections.emptyList());
    }

    @Override
    public IndexResult indexCollectionByEntityIds(Collection<Id<?>> entityIds) {
        return new IndexResult(0, Collections.emptyList());
    }

    @Override
    public IndexResult delete(Object entityInstance) {
        return new IndexResult(0, Collections.emptyList());
    }

    @Override
    public IndexResult deleteCollection(Collection<Object> entityInstances) {
        return new IndexResult(0, Collections.emptyList());
    }

    @Override
    public IndexResult deleteByEntityId(Id<?> entityId) {
        return new IndexResult(0, Collections.emptyList());
    }

    @Override
    public IndexResult deleteCollectionByEntityIds(Collection<Id<?>> entityIds) {
        return new IndexResult(0, Collections.emptyList());
    }
}
