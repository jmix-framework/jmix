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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.parser.PartTree;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class JmixListQuery extends JmixStructuredQuery {
    protected Sort staticSort;

    private static final Logger log = LoggerFactory.getLogger(JmixStructuredQuery.class);

    public JmixListQuery(DataManager dataManager, Metadata jmixMetadata, Method method, RepositoryMetadata metadata, ProjectionFactory factory, PartTree qryTree) {
        super(dataManager, jmixMetadata, method, metadata, factory, qryTree);
        staticSort = LoaderHelper.springToJmixSort(qryTree.getSort());
    }

    @Override
    public Object execute(Object[] parameters) {
        FluentLoader.ByCondition<?> loader = dataManager.load(metadata.getDomainType())
                .condition(conditions)
                .fetchPlan(fetchPlan)
                .hints(queryHints)
                .parameters(buildNamedParametersMap(parameters));

        if (maxResults != null) {
            loader.maxResults(maxResults);
        }

        considerSorting(loader, parameters);
        return processAccordingToReturnType(loader, parameters);
    }

    protected void considerSorting(FluentLoader.ByCondition<?> loader, Object[] parameters) {
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
        loader.sort(Sort.by(orders));
    }

    @Nullable
    protected Object processAccordingToReturnType(FluentLoader.ByCondition<?> loader, Object[] parameters) {
        Class<?> returnType = method.getReturnType();
        if (Slice.class.isAssignableFrom(returnType)) {
            if (pageableIndex == -1) {
                throw new DevelopmentException(String.format("Pageable parameter should be provided for method returns instance of Slice: %s", formatMethod(method)));
            }

            Pageable pageable = (Pageable) parameters[pageableIndex];

            LoaderHelper.applyPageableForConditionLoader(loader, pageable);

            if (Page.class.isAssignableFrom(returnType)) {
                String entityName = jmixMetadata.getClass(metadata.getDomainType()).getName();

                String queryString = String.format("select e from %s e", entityName);

                LoadContext<?> context = new LoadContext<>(jmixMetadata.getClass(metadata.getDomainType()))
                        .setQuery(new LoadContext.Query(queryString)
                                .setCondition(conditions)
                                .setParameters(buildNamedParametersMap(parameters)))
                        .setHints(queryHints);

                long count = dataManager.getCount(context);

                return new PageImpl(loader.list(), pageable, count);
            } else {

                if (pageable.isPaged())
                    loader.maxResults(pageable.getPageSize() + 1);

                List<?> results = loader.list();
                boolean hasNext = pageable.isPaged() && results.size() > pageable.getPageSize();

                return new SliceImpl(hasNext ? results.subList(0, pageable.getPageSize()) : results, pageable, hasNext);
            }

        }

        List<?> result = loader.list();

        if (Iterator.class.isAssignableFrom(returnType)) {
            return result.iterator();
        }
        if (Objects.equals(maxResults, 1) && !isMultipleReturnType(returnType)) {
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
