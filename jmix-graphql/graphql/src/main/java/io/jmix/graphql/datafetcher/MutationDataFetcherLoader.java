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

import io.jmix.graphql.modifier.GraphQLRemoveEntityDataFetcher;
import io.jmix.graphql.modifier.GraphQLUpsertEntityDataFetcher;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component("gql_MutationDataFetcherLoader")
public class MutationDataFetcherLoader {

    @Autowired
    private ListableBeanFactory listableBeanFactory;

    private static Map<Class<?>, Object> entityRemover;

    private static Map<Class<?>, Object> entityUpdater;

    @PostConstruct
    public void load() throws Exception {
        getCustomEntityRemovers();
        getCustomEntityUpdaters();
    }

    public Object getCustomEntityRemover(Class<?> clazz) {
        return entityRemover.get(clazz);
    }

    public Object getCustomEntityUpsert(Class<?> clazz) {
        return entityUpdater.get(clazz);
    }

    protected Map<Class<?>, Object> getCustomEntityRemovers() throws Exception {
        if (entityRemover == null) {
            entityRemover = new HashMap<>();
            Map<String, ?> removers = listableBeanFactory.getBeansOfType(GraphQLRemoveEntityDataFetcher.class);
            for (Object remover : removers.values()) {

                ParameterizedType type = (ParameterizedType) Arrays.stream(remover.getClass().getGenericInterfaces())
                        .filter(t -> t.getTypeName().startsWith(GraphQLRemoveEntityDataFetcher.class.getName())).findFirst().get();
                if ( !entityRemover.containsKey((Class<?>) type.getActualTypeArguments()[0]) ) {
                    entityRemover.put((Class<?>) type.getActualTypeArguments()[0], remover);
                } else {
                    throw new MoreThanOneMutationDataFetcher("More than one mutation fetchers is added for an entity class: "
                            + ((Class<?>) type.getActualTypeArguments()[0]).getName());
                }

            }
        }
        return entityRemover;
    }

    protected Map<Class<?>, Object> getCustomEntityUpdaters() throws Exception {
        if (entityUpdater == null) {
            entityUpdater = new HashMap<>();
            Map<String, ?> removers = listableBeanFactory
                    .getBeansOfType(GraphQLUpsertEntityDataFetcher.class);
            for (Object remover : removers.values()) {
                ParameterizedType type = (ParameterizedType) Arrays.stream(remover.getClass().getGenericInterfaces())
                        .filter(t -> t.getTypeName().startsWith(GraphQLUpsertEntityDataFetcher.class.getName())).findFirst().get();
                if ( !entityUpdater.containsKey((Class<?>) type.getActualTypeArguments()[0]) ) {
                    entityUpdater.put((Class<?>) type.getActualTypeArguments()[0], remover);
                } else {
                    throw new MoreThanOneMutationDataFetcher("More than one mutation fetchers is added for an entity class: "
                            + ((Class<?>) type.getActualTypeArguments()[0]).getName());
                }
            }
        }
        return entityUpdater;
    }

    public static class MoreThanOneMutationDataFetcher extends Exception {
        public MoreThanOneMutationDataFetcher(String message) {
            super(message);
        }
    }

}
