package io.jmix.searchopensearch.searching.strategy;

import io.jmix.search.searching.SearchStrategy;
import org.opensearch.client.opensearch.core.SearchRequest;

/**
 * A OpenSearch-specific extension of the common {link @SearchStrategy} interface
 */
public interface OpenSearchSearchStrategy extends SearchStrategy<SearchRequest.Builder> {
}
