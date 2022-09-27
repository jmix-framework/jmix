/*
 * Copyright 2019 Haulmont.
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

package io.jmix.eclipselink.impl.entitycache;

import com.google.common.base.MoreObjects;
import io.jmix.core.UuidProvider;

import javax.persistence.Parameter;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QueryKey implements Serializable {

    protected final String queryString;
    protected final int firstRow;
    protected final int maxRows;
    protected final boolean softDeletion;
    protected final boolean singleResult;
    /**
     * Named parameters with names replaced by {@code normalized_param_{i}} string, where i - order in query string
     */
    protected final Object[] normalizedParameters;
    protected final Object[] positionalParameters;
    protected final Object[] additionalCriteriaParameters;
    protected final int hashCode;

    //transient attributes
    /**
     * !!!WARNING!!!
     * DO NOT USE COLLECTIONS IN KEYS WITH HAZELCAST.
     * At least HashMap serialized/deserialized by hazelcast in undetermined way which leads to inability to get value by valid key.
     */
    protected final transient String originalQueryString;
    protected final transient Map<String, Object> originalNamedParameters;
    protected final transient UUID id;

    protected static final Pattern PARAMETER_TEMPLATE_PATTERN = Pattern.compile("(:[\\w_$]+)");

    public static QueryKey create(String queryString, boolean softDeletion, boolean singleResult, Query jpaQuery, Map<String, Object> additionalCriteriaParameters) {
        return new QueryKey(queryString, jpaQuery.getFirstResult(), jpaQuery.getMaxResults(), softDeletion, singleResult,
                getNamedParameters(jpaQuery), getPositionalParameters(jpaQuery), additionalCriteriaParameters);
    }

    private static Map<String, Object> getNamedParameters(Query jpaQuery) {
        if (jpaQuery.getParameters() == null) return null;

        List<String> names = jpaQuery.getParameters().stream()
                .map(Parameter::getName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (names.isEmpty()) return null;

        return names.stream()
                .sorted()
                .collect(Collectors.toMap(Function.identity(),
                        jpaQuery::getParameterValue,
                        (o, o2) -> o,
                        LinkedHashMap::new));
    }

    private static Object[] getPositionalParameters(Query jpaQuery) {
        if (jpaQuery.getParameters() == null) return null;

        int max = 0;
        List<Integer> positions = new ArrayList<>();
        for (Parameter parameter : jpaQuery.getParameters()) {
            if (parameter.getPosition() != null) {
                positions.add(parameter.getPosition());
                if (parameter.getPosition() > max) {
                    max = parameter.getPosition();
                }
            }
        }

        if (positions.isEmpty()) return null;

        Object[] positionalParameters = new Object[max];
        positions.forEach(position -> positionalParameters[position - 1] = jpaQuery.getParameterValue(position));
        return positionalParameters;
    }

    protected QueryKey(String queryString, int firstRow, int maxRows,
                       boolean softDeletion, boolean singleResult,
                       Map<String, Object> namedParameters,
                       Object[] positionalParameters,
                       Map<String, Object> additionalCriteriaParameters) {
        this.id = UuidProvider.createUuid();
        this.originalQueryString = queryString;
        this.firstRow = firstRow;
        this.maxRows = maxRows;
        this.softDeletion = softDeletion;
        this.singleResult = singleResult;

        this.originalNamedParameters = namedParameters;
        if (this.originalNamedParameters != null) {
            this.normalizedParameters = new Object[originalNamedParameters.size()];
            StringBuffer queryBuilder = new StringBuffer();
            int i = 0;
            Matcher m = PARAMETER_TEMPLATE_PATTERN.matcher(originalQueryString);
            while (m.find()) {
                String parameterName = m.group().substring(1);
                String newParameterName = "normalized_param_" + i;

                this.normalizedParameters[i] = originalNamedParameters.get(parameterName);
                m.appendReplacement(queryBuilder, String.format(":%s", newParameterName));
                i++;
            }
            m.appendTail(queryBuilder);
            this.queryString = queryBuilder.toString();
        } else {
            this.queryString = this.originalQueryString;
            this.normalizedParameters = null;
        }

        if (additionalCriteriaParameters.size() > 0) {
            this.additionalCriteriaParameters = new Object[additionalCriteriaParameters.size() * 2];

            List<String> sortedParams = additionalCriteriaParameters.keySet().stream().sorted().collect(Collectors.toList());

            for (int i = 0; i < sortedParams.size(); i++) {
                this.additionalCriteriaParameters[i * 2] = sortedParams.get(i);
                this.additionalCriteriaParameters[i * 2 + 1] = additionalCriteriaParameters.get(sortedParams.get(i));
            }
        } else {
            this.additionalCriteriaParameters = null;
        }

        this.positionalParameters = positionalParameters;

        this.hashCode = generateHashCode();
    }

    public UUID getId() {
        return id;
    }

    public String printDescription() {
        return MoreObjects.toStringHelper("Query")
                .addValue("\"" + queryString.trim() + "\"")
                .add("id", id)
                .add("firstRow", firstRow)
                .add("maxRows", maxRows)
                .add("softDeletion", softDeletion)
                .add("positionalParameters", Arrays.deepToString(positionalParameters))
                .add("normalizedParameters", Arrays.deepToString(normalizedParameters))
                .add("additionalCriteriaParameters", Arrays.deepToString(additionalCriteriaParameters))
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueryKey queryKey = (QueryKey) o;

        return hashCode == queryKey.hashCode && equalsFields(queryKey) && equalsParams(queryKey);
    }

    protected boolean equalsFields(QueryKey queryKey) {
        return Objects.equals(queryString, queryKey.queryString)
                && firstRow == queryKey.firstRow
                && maxRows == queryKey.maxRows
                && softDeletion == queryKey.softDeletion
                && singleResult == queryKey.singleResult;
    }

    protected boolean equalsParams(QueryKey queryKey) {
        return Arrays.deepEquals(positionalParameters, queryKey.positionalParameters)
                && Arrays.deepEquals(normalizedParameters, queryKey.normalizedParameters)
                && Arrays.deepEquals(additionalCriteriaParameters, queryKey.additionalCriteriaParameters);
    }


    @Override
    public int hashCode() {
        return this.hashCode;
    }

    protected int generateHashCode() {
        int result = 1;
        result = 31 * result + queryString.hashCode();
        result = 31 * result + Integer.hashCode(firstRow);
        result = 31 * result + Integer.hashCode(maxRows);
        result = 31 * result + Boolean.hashCode(softDeletion);
        result = 31 * result + Boolean.hashCode(singleResult);
        //generates hashCode for value in same way as org.eclipse.persistence.internal.identitymaps.CacheId.computeArrayHashCode()
        result = 31 * result + (positionalParameters == null ? 0 : Arrays.deepHashCode(positionalParameters));

        result = 31 * result + (normalizedParameters == null ? 0 : Arrays.deepHashCode(normalizedParameters));
        result = 31 * result + (additionalCriteriaParameters == null ? 0 : Arrays.deepHashCode(additionalCriteriaParameters));
        return result;
    }

    protected int generateMapHashCode(Map<String, Object> map) {
        int result = 0;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value != null && value.getClass().isArray()) {
                //generates hashCode for value in same way as org.eclipse.persistence.internal.identitymaps.CacheId.computeArrayHashCode()
                result += Objects.hashCode(entry.getKey()) ^ generateArrayHashCode(value);
            } else {
                result += Objects.hashCode(entry.getKey()) ^ Objects.hashCode(entry.getValue());
            }
        }

        return result;
    }

    protected boolean mapEquals(Map<String, Object> a, Map<String, Object> b) {
        if (a == b) return true;

        if (a == null || b == null) return false;

        for (Map.Entry<String, Object> entry : a.entrySet()) {
            Object aValue = entry.getValue();
            Object bValue = b.get(entry.getKey());

            if (aValue == bValue) continue;

            if (aValue == null || bValue == null) return false;

            if (aValue.getClass() != bValue.getClass()) return false;

            if (aValue.getClass().isArray()) {
                if (!Arrays.deepEquals((Object[]) aValue, (Object[]) bValue)) return false;
            } else {
                if (!aValue.equals(bValue)) return false;
            }
        }
        return true;
    }

    protected int generateArrayHashCode(Object array) {
        if (array instanceof Object[])
            return Arrays.deepHashCode((Object[]) array);
        else if (array instanceof byte[])
            return Arrays.hashCode((byte[]) array);
        else if (array instanceof short[])
            return Arrays.hashCode((short[]) array);
        else if (array instanceof int[])
            return Arrays.hashCode((int[]) array);
        else if (array instanceof long[])
            return Arrays.hashCode((long[]) array);
        else if (array instanceof char[])
            return Arrays.hashCode((char[]) array);
        else if (array instanceof float[])
            return Arrays.hashCode((float[]) array);
        else if (array instanceof double[])
            return Arrays.hashCode((double[]) array);
        else if (array instanceof boolean[])
            return Arrays.hashCode((boolean[]) array);
        else {
            return Objects.hashCode(array);
        }
    }
}
