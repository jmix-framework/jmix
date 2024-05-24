/*
 * Copyright 2024 Haulmont.
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

package io.jmix.core.repository;

import io.jmix.core.LoadContext;
import io.jmix.core.annotation.Experimental;
import io.jmix.core.impl.repository.query.utils.LoaderHelper;
import org.springframework.data.domain.PageRequest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for {@link JmixDataRepository}. Provides methods to build {@link JmixDataRepositoryContext} by {@link LoadContext}.
 */
@Experimental
public class JmixDataRepositoryUtils {
    /**
     * @return {@link JmixDataRepositoryContext} with Jmix-specific parameters extracted from {@code context}
     * @throws RuntimeException if {@link LoadContext#getQuery()}} is null
     */
    public static JmixDataRepositoryContext buildRepositoryContext(LoadContext<?> context) {
        LoadContext.Query query = getQuery(context);
        Map<String, Serializable> hints = new HashMap<>();
        context.getHints().forEach((s, o) -> hints.put(s, (Serializable) o));
        hints.put("jmix.cacheable", query.isCacheable());
        return JmixDataRepositoryContext.condition(query.getCondition()).plan(context.getFetchPlan()).hints(hints).build();
    }

    /**
     * @return {@link PageRequest} with {@code firstResult}, {@code maxResults} and {@code sort} extracted from {@code context}
     * @throws RuntimeException if {@link LoadContext#getQuery()}} is null
     */
    public static PageRequest buildPageRequest(LoadContext<?> context) {
        LoadContext.Query query = getQuery(context);
        return PageRequest.of(
                query.getFirstResult() / query.getMaxResults(),
                query.getMaxResults(),
                LoaderHelper.jmixToSpringSort(query.getSort())
        );
    }

    /**
     * @param context to extract id from
     * @return typed id
     * @throws RuntimeException if {@code id} absent in {@code context}
     */
    public static <T> T extractEntityId(LoadContext<?> context) {
        if (context.getId() == null) {
            throw new RuntimeException("Id is not specified for LoadContext");
        }
        //noinspection unchecked
        return (T) context.getId();
    }

    /**
     * @return {@code Query} for {@code context}
     * @throws RuntimeException if context has no {@code Query}
     */
    private static LoadContext.Query getQuery(LoadContext<?> context) {
        if (context.getQuery() == null) {
            //query should always exist in collection loader
            throw new RuntimeException("No query in CollectionLoader");
        }
        return context.getQuery();
    }
}
