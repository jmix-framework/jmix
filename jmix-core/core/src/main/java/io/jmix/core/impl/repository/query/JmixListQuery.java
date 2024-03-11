/*
 * Copyright 2021 Haulmont.
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

package io.jmix.core.impl.repository.query;

import io.jmix.core.Sort;
import io.jmix.core.*;
import io.jmix.core.impl.repository.query.utils.LoaderHelper;
import io.jmix.core.repository.JmixDataRepositoryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.*;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

public class JmixListQuery extends JmixStructuredQuery {
    protected FetchPlanRepository fetchPlanRepository;

    protected final Sort staticSort;

    public JmixListQuery(DataManager dataManager,
                         Metadata jmixMetadata,
                         FetchPlanRepository fetchPlanRepository,
                         Method method,
                         RepositoryMetadata metadata,
                         ProjectionFactory factory,
                         PartTree qryTree) {
        super(dataManager, jmixMetadata, method, metadata, factory, qryTree);

        this.staticSort = LoaderHelper.springToJmixSort(qryTree.getSort());
        this.fetchPlanRepository = fetchPlanRepository;
    }

    @Override
    public Object execute(Object[] parameters) {
        JmixDataRepositoryContext jmixDataRepositoryContext = jmixContextIndex == -1 ? null :
                (JmixDataRepositoryContext) parameters[jmixContextIndex];

        LoadContext<?> loadContext = prepareStructuredQueryContext(parameters);

        if (fetchPlanIndex != -1 && parameters[fetchPlanIndex] != null) {
            loadContext.setFetchPlan((FetchPlan) parameters[fetchPlanIndex]);
        } else if (jmixDataRepositoryContext != null && jmixDataRepositoryContext.fetchPlan() != null) {
            loadContext.setFetchPlan(jmixDataRepositoryContext.fetchPlan());
        } else {
            loadContext.setFetchPlan(fetchPlanRepository.getFetchPlan(metadata.getDomainType(), fetchPlanByAnnotation));
        }

        if (maxResults != null) {
            //noinspection DataFlowIssue
            loadContext.getQuery().setMaxResults(maxResults);
        }

        considerSorting(loadContext, parameters);
        return processAccordingToReturnType(loadContext, parameters);
    }

    protected void considerSorting(LoadContext<?> loadContext, Object[] parameters) {
        List<Sort.Order> orders = new LinkedList<>();

        if (staticSort != null) {
            orders.addAll(staticSort.getOrders());
        }
        if (sortIndex != -1) {
            orders.addAll(LoaderHelper.springToJmixSort((org.springframework.data.domain.Sort) parameters[sortIndex])
                    .getOrders());
        }
        if (pageableIndex != -1) {
            orders.addAll(LoaderHelper.springToJmixSort(((Pageable) parameters[pageableIndex]).getSort()).getOrders());
        }
        //noinspection DataFlowIssue
        loadContext.getQuery().setSort(Sort.by(orders));
    }

    @Nullable
    protected Object processAccordingToReturnType(LoadContext<?> loadContext, Object[] parameters) {
        Class<?> returnType = method.getReturnType();
        if (Slice.class.isAssignableFrom(returnType)) {
            if (pageableIndex == -1) {
                throw new DevelopmentException(String.format("Pageable parameter should be provided for method returns instance of Slice: %s", formatMethod(method)));
            }

            Pageable pageable = (Pageable) parameters[pageableIndex];

            LoaderHelper.applyPageableForLoadContext(loadContext, pageable);

            if (Page.class.isAssignableFrom(returnType)) {
                long count = dataManager.getCount(loadContext);
                return new PageImpl<>(dataManager.loadList(loadContext), pageable, count);
            } else {

                if (pageable.isPaged())
                    loadContext.getQuery().setMaxResults(pageable.getPageSize() + 1);// have to load additional one to know whether next results present

                List<?> results = dataManager.loadList(loadContext);
                boolean hasNext = pageable.isPaged() && results.size() > pageable.getPageSize();

                return new SliceImpl(hasNext ? results.subList(0, pageable.getPageSize()) : results, pageable, hasNext);
            }
        }

        List<?> result = dataManager.loadList(loadContext);

        if (returnType.isAssignableFrom(metadata.getDomainType())
                || Optional.class.isAssignableFrom(returnType)) {
            if (result.size() > 1) {
                throw new IncorrectResultSizeDataAccessException(1, result.size());
            }
            return result.size() == 1 ? result.iterator().next() : null;
        }

        if (Iterator.class.isAssignableFrom(returnType)) {
            return result.iterator();
        }
        if (Objects.equals(maxResults, 1) && !isMultipleReturnType(returnType)) {//just in case of unconsidered return type
            return result.isEmpty() ? null : result.iterator().next();
        }
        return result;
    }

    protected boolean isMultipleReturnType(Class<?> returnType) {
        return Iterable.class.isAssignableFrom(returnType) || Stream.class.isAssignableFrom(returnType);
    }

    @Override
    protected String getQueryDescription() {
        return String.format("%s; static sort: '%s'", super.getQueryDescription(), staticSort);
    }
}
