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

package io.jmix.graphql.datafetcher;

import io.jmix.graphql.loader.GraphQLEntityCountDataFetcher;
import io.jmix.graphql.loader.GraphQLEntityDataFetcher;
import io.jmix.graphql.loader.GraphQLEntityListDataFetcher;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component("gql_QueryDataFetcherLoader")
public class QueryDataFetcherLoader {

    @Autowired
    private ListableBeanFactory listableBeanFactory;

    private static Map<Class<?>, Object> entitiesDataFetchers;

    private static Map<Class<?>, Object> entityDataFetchers;

    private static Map<Class<?>, Object> countDataFetchers;

    @PostConstruct
    public void load() throws Exception {
        getCustomEntityCountFetchers();
        getCustomEntitiesFetchers();
        getCustomEntityFetchers();
    }

    public Object getCustomCountFetcher(Class<?> clazz) {
        return countDataFetchers.get(clazz);
    }

    public Object getCustomEntitiesFetcher(Class<?> clazz) {
        return entitiesDataFetchers.get(clazz);
    }

    public Object getCustomEntityFetcher(Class<?> clazz) {
        return entityDataFetchers.get(clazz);
    }

    protected Map<Class<?>, Object> getCustomEntityCountFetchers() throws Exception {
        if (countDataFetchers == null) {
            countDataFetchers = new HashMap<>();
            Map<String, ?> loaders = listableBeanFactory.getBeansOfType(GraphQLEntityCountDataFetcher.class);
            for (Object loader : loaders.values()) {
                ParameterizedType type = (ParameterizedType) Arrays.stream(loader.getClass().getGenericInterfaces())
                        .filter(t -> t.getTypeName().startsWith(GraphQLEntityCountDataFetcher.class.getName())).findFirst().get();
                if( !countDataFetchers.containsKey((Class<?>) type.getActualTypeArguments()[0])) {
                    countDataFetchers.put((Class<?>) type.getActualTypeArguments()[0], loader);
                } else {
                    throw new MoreThanOneQueryDataFetcher("More than one fetchers is added for an entity class: "
                            + ((Class<?>) type.getActualTypeArguments()[0]).getName());
                }
            }
        }
        return countDataFetchers;
    }

    protected Map<Class<?>, Object> getCustomEntitiesFetchers() throws Exception {
        if (entitiesDataFetchers == null) {
            entitiesDataFetchers = new HashMap<>();
            Map<String, ?> loaders = listableBeanFactory
                    .getBeansOfType(GraphQLEntityListDataFetcher.class);
            for (Object loader : loaders.values()) {
                ParameterizedType type = (ParameterizedType) Arrays.stream(loader.getClass().getGenericInterfaces())
                        .filter(t -> t.getTypeName().startsWith(GraphQLEntityListDataFetcher.class.getName())).findFirst().get();
                if (!entitiesDataFetchers.containsKey((Class<?>) type.getActualTypeArguments()[0])) {
                    entitiesDataFetchers.put((Class<?>) type.getActualTypeArguments()[0], loader);
                } else {
                    throw new MoreThanOneQueryDataFetcher("More than one fetchers is added for an entity class: "
                            + ((Class<?>) type.getActualTypeArguments()[0]).getName());
                }
            }
        }
        return entitiesDataFetchers;
    }

    protected Map<Class<?>, Object> getCustomEntityFetchers() throws Exception {
        if (entityDataFetchers == null) {
            entityDataFetchers = new HashMap<>();
            Map<String, ?> loaders = listableBeanFactory
                    .getBeansOfType(GraphQLEntityDataFetcher.class);
            for (Object loader : loaders.values()) {
                ParameterizedType type = (ParameterizedType) Arrays.stream(loader.getClass().getGenericInterfaces())
                        .filter(t -> t.getTypeName().startsWith(GraphQLEntityDataFetcher.class.getName())).findFirst().get();
                if (!entityDataFetchers.containsKey((Class<?>) type.getActualTypeArguments()[0])) {
                    entityDataFetchers.put((Class<?>) type.getActualTypeArguments()[0], loader);
                } else {
                    throw new MoreThanOneQueryDataFetcher("More than one fetchers is added for an entity class: "
                            + ((Class<?>) type.getActualTypeArguments()[0]).getName());
                }

            }
        }
        return entityDataFetchers;
    }

    public static class MoreThanOneQueryDataFetcher extends Exception {
        public MoreThanOneQueryDataFetcher(String message) {
            super(message);
        }
    }

}
