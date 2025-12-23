/*
 * Copyright 2025 Haulmont.
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

import io.jmix.core.*;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.impl.repository.query.utils.LoaderHelper;
import io.jmix.core.impl.repository.query.utils.QueryParameterUtils;
import io.jmix.core.metamodel.datatype.EnumClass;
import io.jmix.core.metamodel.datatype.impl.EnumUtils;
import io.jmix.core.repository.JmixDataRepositoryContext;
import io.jmix.core.repository.Query;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.*;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.lang.Nullable;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Stream;

import static io.jmix.core.Sort.by;

public class JmixScalarQuery extends JmixAbstractQuery<ValueLoadContext> {

    protected List<String> resultPropertyNames;
    protected String query;

    public JmixScalarQuery(DataManager dataManager,
                           Metadata jmixMetadata,
                           Method method,
                           RepositoryMetadata metadata,
                           ProjectionFactory factory,
                           String query,
                           List<String> resultPropertyNames) {
        super(dataManager, jmixMetadata, method, metadata, factory);
        this.query = QueryParameterUtils.replaceQueryParameters(queryMethod, method, query, namedParametersBindings);
        this.resultPropertyNames = resultPropertyNames;
    }

    /**
     * Builds {@link ValueLoadContext} based on
     * <ul>
     *     <li>{@link Query}</li>
     *     <li>{@link JmixDataRepositoryContext#condition()},</li>
     *     <li>{@link io.jmix.core.repository.QueryHints},</li>
     *     <li>{@link JmixDataRepositoryContext#hints()}.</li>
     * </ul>
     * <p>
     * Suitable as is for count query.
     *
     * @param parameters query method parameters
     * @return {@link ValueLoadContext} with {@link ValueLoadContext#getQuery()} not null
     */
    protected ValueLoadContext prepareQueryContext(Object[] parameters) {
        ValueLoadContext.Query lcQuery = new ValueLoadContext.Query(this.query);

        if (jmixContextIndex != -1 && parameters[jmixContextIndex] != null) {
            JmixDataRepositoryContext jmixDataRepositoryContext = (JmixDataRepositoryContext) parameters[jmixContextIndex];
            if (jmixDataRepositoryContext.condition() != null) {
                lcQuery.setCondition(jmixDataRepositoryContext.condition());
            }
        }

        lcQuery.setParameters(buildNamedParametersMap(parameters));

        return new ValueLoadContext()
                .setQuery(lcQuery)
                .setHints(collectHints(parameters))
                .setProperties(resultPropertyNames);
    }

    @Override
    @Nullable
    public Object execute(Object[] parameters) {
        ValueLoadContext loadContext = prepareQueryContext(parameters);
        loadContext.getQuery().setSort(by(getSortFromParams(parameters)));

        return processAccordingToReturnType(loadContext, parameters);
    }

    @Nullable
    protected Object processAccordingToReturnType(ValueLoadContext loadContext, Object[] parameters) {
        Pageable pageable = null;

        if (pageableIndex != -1) {
            pageable = (Pageable) parameters[pageableIndex];
            LoaderHelper.applyPageableForValueLoadContext(loadContext, pageable);
        }

        Class<?> returnClass = method.getReturnType();
        Type genericReturnType = method.getGenericReturnType();

        if (Slice.class.isAssignableFrom(returnClass)) {
            if (pageable == null) {
                throw new DevelopmentException(
                        String.format("Pageable parameter should be provided for method returns instance of Slice: %s",
                                formatMethod(method)));
            }

            if (Page.class.isAssignableFrom(returnClass)) {
                long count = dataManager.getCount(loadContext.copy());
                List<?> results = (List<?>) processMultipleValuesAccordingToReturnType(dataManager.loadValues(loadContext),
                        genericReturnType,
                        List.class);
                return new PageImpl<>(results, pageable, count);
            } else {
                if (pageable.isPaged())
                    loadContext.getQuery().setMaxResults(pageable.getPageSize() + 1);// have to load additional one to know whether next results present
                List<?> results = (List<?>) processMultipleValuesAccordingToReturnType(dataManager.loadValues(loadContext),
                        genericReturnType,
                        List.class);
                boolean hasNext = pageable.isPaged() && results.size() > pageable.getPageSize();

                return new SliceImpl(hasNext ? results.subList(0, pageable.getPageSize()) : results, pageable, hasNext);
            }
        }

        List<KeyValueEntity> keyValueEntities = dataManager.loadValues(loadContext);

        Object result;

        if (Collection.class.isAssignableFrom(returnClass)
                || Iterable.class.isAssignableFrom(returnClass)
                || Stream.class.isAssignableFrom(returnClass)) {
            result = processMultipleValuesAccordingToReturnType(keyValueEntities, genericReturnType);
        } else if (Optional.class.isAssignableFrom(returnClass)) {
            if (keyValueEntities.isEmpty()) {
                result = Optional.empty();
            } else {//let Spring make conversion if needed
                if (genericReturnType instanceof ParameterizedType prt
                        && prt.getActualTypeArguments().length == 1
                        && prt.getActualTypeArguments()[0] instanceof Class<?> clazz) {
                    result = processAsSingleValue(keyValueEntities, clazz);
                } else {//return KeyValueEntity in case of the raw type or type parametrized with not a Class
                    result = processAsSingleValue(keyValueEntities, KeyValueEntity.class);
                }
            }
        } else {
            result = processAsSingleValue(keyValueEntities, returnClass);
        }

        return result;
    }

    @Nullable
    protected Object processAsSingleValue(List<KeyValueEntity> keyValueEntities, Class<?> returnClass) {
        if (keyValueEntities.isEmpty())
            throw new NoResultException("No results");

        if (keyValueEntities.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, keyValueEntities.size());
        }

        return processSingleValueAccordingToReturnType(keyValueEntities.get(0), returnClass);
    }

    @Nullable
    protected Object processSingleValueAccordingToReturnType(KeyValueEntity keyValueEntity, Class<?> returnClass) {
        if (KeyValueEntity.class.isAssignableFrom(returnClass)) {
            return keyValueEntity;
        } else if (EnumClass.class.isAssignableFrom(returnClass)) {
            Object value = keyValueEntity.getValue(resultPropertyNames.get(0));
            if (value == null) return null;
            for (Object o : returnClass.getEnumConstants()) {
                EnumClass<?> enumValue = (EnumClass<?>) o;
                if (value.equals(enumValue.getId())) {
                    return enumValue;
                }
            }
            throw new DevelopmentException("Unable to find EnumClass value for id '" + value + "'");
        } else {
            return keyValueEntity.getValue(resultPropertyNames.get(0));
        }
    }

    protected Object processMultipleValuesAccordingToReturnType(List<KeyValueEntity> keyValueEntities, Type methodGenericReturnType) {
        return processMultipleValuesAccordingToReturnType(keyValueEntities, methodGenericReturnType, null);
    }

    @SuppressWarnings("unchecked")
    protected Object processMultipleValuesAccordingToReturnType(List<KeyValueEntity> keyValueEntities,
                                                                Type methodGenericReturnType,
                                                                @Nullable Class<? extends Collection> collectionClass) {

        if (methodGenericReturnType instanceof ParameterizedType pt) {

            Type[] actualTypeArguments = pt.getActualTypeArguments();

            if (actualTypeArguments.length > 1) {
                throw new DevelopmentException("Complex collections is unsupported");
            }

            Type rawType = pt.getRawType();

            if (collectionClass == null) {
                if (rawType instanceof Class<?> returnClass && Collection.class.isAssignableFrom(returnClass)) {
                    collectionClass = (Class<? extends Collection<?>>) rawType;
                } else {
                    collectionClass = List.class;
                }
            }

            boolean isKeyValueTypeArgument = actualTypeArguments.length == 1
                    && actualTypeArguments[0] instanceof Class<?> clazz
                    && KeyValueEntity.class.isAssignableFrom(clazz);

            if (List.class.equals(collectionClass) && isKeyValueTypeArgument) {
                return keyValueEntities;
            }

            if (resultPropertyNames.size() != 1 && !isKeyValueTypeArgument) {
                throw new DevelopmentException("Conversion to Collection of complex types is unsupported");
            }

            Class<?> argumentClass;
            if (actualTypeArguments.length == 0) {
                argumentClass = KeyValueEntity.class;
            } else if (actualTypeArguments[0] instanceof Class<?>) {
                argumentClass = (Class<?>) actualTypeArguments[0];
            } else {
                throw new DevelopmentException("Unsupported generic type:" + actualTypeArguments[0]);
            }

            //noinspection rawtypes
            Collection result = createCollectionOfClass(collectionClass, keyValueEntities.size());

            for (KeyValueEntity keyValueEntity : keyValueEntities) {
                result.add(processSingleValueAccordingToReturnType(keyValueEntity, argumentClass));
            }

            if (rawType instanceof Class<?> returnClass && Stream.class.isAssignableFrom(returnClass)) {
                return result.stream();
            } else {
                return result;
            }
        }

        return keyValueEntities;
    }

    protected Collection<?> createCollectionOfClass(Class<? extends Collection> collectionClass, int size) {
        if (collectionClass.equals(List.class)) {
            return new ArrayList<>(size);
        } else if (collectionClass.equals(Set.class)) {
            return new HashSet<>(size);
        }

        try {
            if (!collectionClass.isInterface()) {
                Constructor<? extends Collection> constructor = ConstructorUtils.getAccessibleConstructor(collectionClass);
                return constructor.newInstance();
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            //do nothing, a default collection will be returned
        }

        return new ArrayList<>(size);
    }

    @Override
    public String toString() {
        return String.format("%s:{%s}", this.getClass().getSimpleName(), getQueryDescription());
    }

    @Override
    protected String getQueryDescription() {
        return String.format("%s; query:%s", super.getQueryDescription(), query);
    }
}
