package io.jmix.searchelasticsearch.searching.strategy;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import io.jmix.search.searching.SearchStrategy;
import org.jspecify.annotations.NullMarked;

/**
 * Elasticsearch-specific {@link SearchStrategy} that builds an Elasticsearch {@link SearchRequest}.
 */
@NullMarked
public interface ElasticsearchSearchStrategy extends SearchStrategy<SearchRequest.Builder> {
}
