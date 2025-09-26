package io.jmix.searchopensearch.searching.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.searching.*;
import io.jmix.search.searching.impl.AbstractEntitySearcher;
import io.jmix.search.searching.impl.SearchResultImpl;
import io.jmix.search.utils.Constants;
import io.jmix.searchopensearch.searching.strategy.OpenSearchSearchStrategy;
import io.jmix.searchopensearch.searching.strategy.OpenSearchSearchStrategyProvider;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.apache.commons.lang3.StringUtils;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class OpenSearchEntitySearcher extends AbstractEntitySearcher implements EntitySearcher {

    private static final Logger log = LoggerFactory.getLogger(OpenSearchEntitySearcher.class);

    protected static final TypeReference<Map<String, Object>> GENERIC_MAP_TYPE_REF = new TypeReference<>() {
    };

    protected final OpenSearchClient client;
    protected final IndexConfigurationManager indexConfigurationManager;
    protected final Metadata metadata;
    protected final InstanceNameProvider instanceNameProvider;
    protected final SecureOperations secureOperations;
    protected final PolicyStore policyStore;
    protected final OpenSearchSearchStrategyProvider searchStrategyManager;
    protected final SearchUtils searchUtils;

    public OpenSearchEntitySearcher(OpenSearchClient client,
                                    IndexConfigurationManager indexConfigurationManager,
                                    Metadata metadata,
                                    MetadataTools metadataTools,
                                    DataManager secureDataManager,
                                    InstanceNameProvider instanceNameProvider,
                                    SearchProperties searchProperties,
                                    IdSerialization idSerialization,
                                    SecureOperations secureOperations,
                                    PolicyStore policyStore,
                                    OpenSearchSearchStrategyProvider searchStrategyManager,
                                    SearchUtils searchUtils) {
        super(metadataTools, searchProperties, secureDataManager, idSerialization);
        this.client = client;
        this.indexConfigurationManager = indexConfigurationManager;
        this.metadata = metadata;
        this.instanceNameProvider = instanceNameProvider;
        this.secureOperations = secureOperations;
        this.policyStore = policyStore;
        this.searchStrategyManager = searchStrategyManager;
        this.searchUtils = searchUtils;
    }


    @Override
    public SearchResult search(SearchContext searchContext) {
        return search(searchContext, searchStrategyManager.getDefaultSearchStrategy().getName());
    }

    @Override
    public SearchResult search(SearchContext searchContext, String searchStrategyName) {
        log.debug("Perform search by context '{}'", searchContext);

        OpenSearchSearchStrategy searchStrategy = resolveSearchStrategy(searchStrategyName);
        SearchResultImpl searchResult = initSearchResult(searchContext, searchStrategy);
        List<String> targetIndexes = searchUtils.resolveEffectiveTargetIndexes(searchContext.getEntities());
        if (targetIndexes.isEmpty()) {
            return searchResult;
        }

        boolean moreDataAvailable;
        do {
            SearchRequest searchRequest = createRequest(
                    searchContext, targetIndexes, searchStrategy, searchResult.getEffectiveOffset()
            );
            SearchResponse<ObjectNode> searchResponse;
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Search Request: {}", searchRequest.toJsonString());
                }
                searchResponse = client.search(searchRequest, ObjectNode.class);
            } catch (IOException e) {
                throw new RuntimeException("Search failed", e);
            }
            HitsMetadata<ObjectNode> hits = searchResponse.hits();

            Map<MetaClass, List<Hit<ObjectNode>>> hitsByEntityName = groupHitsByEntity(hits);
            fillSearchResult(searchResult, hitsByEntityName);

            long totalHits = hits.total() == null ? hits.hits().size() : hits.total().value();
            searchResult.setTotalHits(totalHits);
            moreDataAvailable = (totalHits - searchResult.getEffectiveOffset()) > 0;
        } while (moreDataAvailable && !isResultFull(searchResult, searchContext));
        searchResult.setMoreDataAvailable(moreDataAvailable);
        return searchResult;
    }

    @Override
    public SearchResult searchNextPage(SearchResult previousSearchResult) {
        return search(previousSearchResult.createNextPageSearchContext(), previousSearchResult.getSearchStrategy());
    }

    protected SearchResultImpl initSearchResult(SearchContext searchContext, OpenSearchSearchStrategy searchStrategy) {
        return new SearchResultImpl(searchContext, searchStrategy.getName());
    }

    protected OpenSearchSearchStrategy resolveSearchStrategy(String searchStrategyName) {
        return searchStrategyManager.getSearchStrategyByName(searchStrategyName);
    }

    protected SearchRequest createRequest(SearchContext searchContext,
                                          List<String> targetIndexes,
                                          OpenSearchSearchStrategy searchStrategy,
                                          int offset) {

        SearchRequest.Builder builder = new SearchRequest.Builder();
        initRequest(builder, targetIndexes);
        searchStrategy.configureRequest(builder, searchContext);
        applyPostStrategyRequestSettings(builder, searchContext, offset);
        return builder.build();
    }

    protected void initRequest(SearchRequest.Builder builder, List<String> targetIndexes) {
        builder.index(targetIndexes);
    }

    protected void applyPostStrategyRequestSettings(SearchRequest.Builder builder, SearchContext searchContext, int offset) {
        builder.size(searchContext.getSize()).from(offset);
        configureHighlight(builder);
        builder.trackTotalHits(b -> b.enabled(true));
    }

    protected void configureHighlight(SearchRequest.Builder requestBuilder) {
        requestBuilder.highlight(highlightBuilder ->
                highlightBuilder.requireFieldMatch(true)
                        .fields("*", highlightFieldBuilder ->
                                highlightFieldBuilder.preTags("<b>").postTags("</b>")
                        )
        );
    }

    protected Map<MetaClass, List<Hit<ObjectNode>>> groupHitsByEntity(HitsMetadata<ObjectNode> hits) {
        return hits.hits().stream()
                .filter(hit -> hit.id() != null)
                .collect(Collectors.groupingBy(hit -> {
                    Id<Object> entityId = idSerialization.stringToId(hit.id());
                    return metadata.getClass(entityId.getEntityClass());
                }));
    }

    protected void fillSearchResult(SearchResultImpl searchResultImpl, Map<MetaClass, List<Hit<ObjectNode>>> hitsByEntityName) {
        int sizeLimit = searchResultImpl.getSearchContext().getSize();
        for (Map.Entry<MetaClass, List<Hit<ObjectNode>>> entry : hitsByEntityName.entrySet()) {
            MetaClass metaClass = entry.getKey();

            boolean hasRowLevelPolicies = policyStore.getRowLevelPolicies(metaClass).findAny().isPresent();
            List<Hit<ObjectNode>> entityHits = entry.getValue();
            Set<String> effectiveIds;
            if (hasRowLevelPolicies) {
                List<Object> entityIds = entityHits.stream()
                        .map(Hit::id)
                        .filter(Objects::nonNull)
                        .map(idSerialization::stringToId)
                        .map(Id::getValue)
                        .collect(Collectors.toList());
                effectiveIds = reloadIds(metaClass, entityIds);
            } else {
                effectiveIds = entityHits.stream()
                        .map(Hit::id)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
            }

            for (Hit<ObjectNode> hit : entityHits) {
                if (searchResultImpl.getSize() >= sizeLimit) {
                    return;
                }

                String entityId = hit.id();
                if (entityId == null) {
                    continue;
                }

                if (effectiveIds.contains(entityId)) {
                    Map<String, Object> source = objectNodeToMap(hit.source());
                    String displayedName;
                    if (source == null) {
                        displayedName = entityId;
                    } else {
                        String instanceName = (String) source.get(Constants.INSTANCE_NAME_FIELD);
                        displayedName = StringUtils.isEmpty(instanceName) ? entityId : instanceName;
                    }
                    searchResultImpl.addEntry(createSearchResultEntry(entityId, displayedName, metaClass.getName(), hit.highlight()));
                }
                searchResultImpl.incrementOffset();
            }
        }
    }
}
