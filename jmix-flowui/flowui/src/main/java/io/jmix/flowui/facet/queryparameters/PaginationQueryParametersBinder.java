/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.facet.queryparameters;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.router.QueryParameters;
import io.jmix.flowui.component.PaginationComponent;
import io.jmix.flowui.data.pagination.PaginationDataLoader;
import io.jmix.flowui.facet.QueryParametersFacet.QueryParametersChangeEvent;
import io.jmix.flowui.view.navigation.UrlParamSerializer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PaginationQueryParametersBinder extends AbstractQueryParametersBinder {

    public static final String NAME = "pagination";

    public static final String FIRST_RESULT_PARAM = "firstResult";
    public static final String MAX_RESULTS_PARAM = "maxResults";

    protected PaginationComponent<?> pagination;

    protected String firstResultParam;
    protected String maxResultsParam;

    protected UrlParamSerializer urlParamSerializer;

    public PaginationQueryParametersBinder(PaginationComponent<?> pagination,
                                           UrlParamSerializer urlParamSerializer) {
        this.pagination = pagination;
        this.urlParamSerializer = urlParamSerializer;

        initComponent(pagination);
    }

    protected void initComponent(PaginationComponent<?> pagination) {
        pagination.addAfterRefreshListener(this::onAfterRefresh);
    }

    protected void onAfterRefresh(PaginationComponent.AfterRefreshEvent<?> event) {
        getPaginationLoader().ifPresent(paginationLoader -> {
            QueryParameters queryParameters = QueryParameters.simple(ImmutableMap.of(
                    getFirstResultParam(), urlParamSerializer.serialize(paginationLoader.getFirstResult()),
                    getMaxResultsParam(), urlParamSerializer.serialize(paginationLoader.getMaxResults())
            ));

            fireQueryParametersChanged(new QueryParametersChangeEvent(this, queryParameters));
        });
    }

    @Override
    public void updateState(QueryParameters queryParameters) {
        getPaginationLoader().ifPresent(paginationLoader -> {
            Map<String, List<String>> parameters = queryParameters.getParameters();
            if (parameters.containsKey(getFirstResultParam())) {
                String serializedFirstResult = parameters.get(getFirstResultParam()).get(0);
                int firstResult = urlParamSerializer.deserialize(Integer.class, serializedFirstResult);
                paginationLoader.setFirstResult(firstResult);
            }

            if (parameters.containsKey(getMaxResultsParam())) {
                String serializedMaxResults = parameters.get(getMaxResultsParam()).get(0);
                int maxResults = urlParamSerializer.deserialize(Integer.class, serializedMaxResults);
                paginationLoader.setMaxResults(maxResults);
            }
        });
    }

    public String getFirstResultParam() {
        return Strings.isNullOrEmpty(firstResultParam) ? FIRST_RESULT_PARAM : firstResultParam;
    }

    public void setFirstResultParam(@Nullable String firstResultParam) {
        this.firstResultParam = firstResultParam;
    }

    public String getMaxResultsParam() {
        return Strings.isNullOrEmpty(maxResultsParam) ? MAX_RESULTS_PARAM : maxResultsParam;
    }

    public void setMaxResultsParam(@Nullable String maxResultsParam) {
        this.maxResultsParam = maxResultsParam;
    }

    protected Optional<PaginationDataLoader> getPaginationLoader() {
        return Optional.ofNullable(pagination.getPaginationLoader());
    }
}
