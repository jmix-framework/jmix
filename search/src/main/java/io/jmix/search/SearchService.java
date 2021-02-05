/*
 * Copyright 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.search;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;

@Component
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    protected RestHighLevelClient esClient;

    public SearchResult search(String searchTerm) {
        //todo Currently it's a simple search over all fields of all search indices

        SearchRequest searchRequest = new SearchRequest("*_search_index");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(searchTerm, "*"));

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("*");
        searchSourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);

        try {
            SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
            if(searchResponse.getHits().getTotalHits().value > 0) {
                log.info("[IVGA] Search response: Status = {}, Hits (total = {}) = {}",
                        searchResponse.status(), searchResponse.getHits().getTotalHits(), Arrays.deepToString(searchResponse.getHits().getHits()));
            }
            return new SearchResult(); //todo
        } catch (IOException e) {
            throw new RuntimeException("Search failed", e);
        }
    }



    public static class SearchResult {

    }
}
