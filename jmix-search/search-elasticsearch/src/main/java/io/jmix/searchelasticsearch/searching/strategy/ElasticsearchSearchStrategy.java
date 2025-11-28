package io.jmix.searchelasticsearch.searching.strategy;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import io.jmix.search.searching.SearchStrategy;

/**
 * Represents a search strategy specifically designed for Elasticsearch integration.
 * An implementation of this interface configures an Elasticsearch {@link SearchRequest}
 * based on the provided search context.
 */
public interface ElasticsearchSearchStrategy extends SearchStrategy<SearchRequest.Builder> {
}
