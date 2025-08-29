package io.jmix.searchelasticsearch.searching.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterables;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.search.SearchProperties;
import io.jmix.search.index.IndexConfiguration;
import io.jmix.search.index.mapping.IndexConfigurationManager;
import io.jmix.search.searching.*;
import io.jmix.search.searching.impl.AbstractEntitySearcher;
import io.jmix.search.searching.impl.SearchResultImpl;
import io.jmix.search.utils.Constants;
import io.jmix.searchelasticsearch.searching.strategy.ElasticsearchSearchStrategy;
import io.jmix.searchelasticsearch.searching.strategy.ElasticsearchSearchStrategyProvider;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation for Elasticsearch
 */
public class ElasticsearchEntitySearcher extends AbstractEntitySearcher implements EntitySearcher {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchEntitySearcher.class);

    protected final ElasticsearchClient client;
    protected final IndexConfigurationManager indexConfigurationManager;
    protected final Metadata metadata;
    protected final InstanceNameProvider instanceNameProvider;
    protected final SecureOperations secureOperations;
    protected final PolicyStore policyStore;
    protected final ElasticsearchSearchStrategyProvider searchStrategyManager;
    protected final SearchUtils searchUtils;

    public ElasticsearchEntitySearcher(ElasticsearchClient client,
                                       IndexConfigurationManager indexConfigurationManager,
                                       Metadata metadata,
                                       MetadataTools metadataTools,
                                       DataManager secureDataManager,
                                       InstanceNameProvider instanceNameProvider,
                                       SearchProperties searchProperties,
                                       IdSerialization idSerialization,
                                       SecureOperations secureOperations,
                                       PolicyStore policyStore,
                                       ElasticsearchSearchStrategyProvider searchStrategyManager,
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

        ElasticsearchSearchStrategy searchStrategy = resolveSearchStrategy(searchStrategyName);
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
                log.debug("Search Request: {}", searchRequest);
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

    protected SearchResultImpl initSearchResult(SearchContext searchContext, ElasticsearchSearchStrategy searchStrategy) {
        return new SearchResultImpl(searchContext, searchStrategy.getName());
    }

    protected ElasticsearchSearchStrategy resolveSearchStrategy(String searchStrategyName) {
        return searchStrategyManager.getSearchStrategyByName(searchStrategyName);
    }

    protected SearchRequest createRequest(SearchContext searchContext,
                                          List<String> targetIndexes,
                                          ElasticsearchSearchStrategy searchStrategy,
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
