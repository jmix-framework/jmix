package io.jmix.searchopensearch.searching.strategy;

import io.jmix.search.searching.SearchStrategy;
import org.jspecify.annotations.NullMarked;
import org.opensearch.client.opensearch.core.SearchRequest;

/**
 * OpenSearch-specific {@link SearchStrategy} that builds an OpenSearch {@link SearchRequest}.
 */
@NullMarked
public interface OpenSearchSearchStrategy extends SearchStrategy<SearchRequest.Builder> {
}
