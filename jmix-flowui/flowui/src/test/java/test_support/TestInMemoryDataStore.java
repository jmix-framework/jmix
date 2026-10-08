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

package test_support;

import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TestInMemoryDataStore implements DataStore {

    private String name;

    private Map<String, Map<Object, Object>> entities = new ConcurrentHashMap<>();

    @Autowired
    private Metadata metadata;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    @Override
    public Object load(LoadContext<?> context) {
        Map<Object, Object> instances = entities.get(context.getEntityMetaClass().getName());
        if (instances == null)
            return null;
        else
            return instances.get(context.getId());
    }

    @Override
    public List<Object> loadList(LoadContext<?> context) {
        Map<Object, Object> instances = entities.get(context.getEntityMetaClass().getName());
        List<Object> result = instances == null ? new ArrayList<>() : new ArrayList<>(instances.values());

        LoadContext.Query query = context.getQuery();
        if (query == null) {
            return result;
        }

        Condition condition = query.getCondition();
        if (condition != null) {
            result = result.stream()
                    .filter(entity -> matches(entity, condition))
                    .collect(Collectors.toList());
        }

        Sort sort = query.getSort();
        if (sort != null && !sort.getOrders().isEmpty()) {
            result.sort(createComparator(sort));
        }

        return applyPaging(result, query.getFirstResult(), query.getMaxResults());
    }

    /**
     * Minimal query support for tests: filters by {@link PropertyCondition} with the
     * {@code CONTAINS} operation on string properties (case-insensitive substring match, with
     * backslash-escapes of the search value stripped), and by {@link LogicalCondition} AND/OR
     * nesting. Any other condition type matches everything (no filtering is applied for it),
     * which is sufficient for the current test suite.
     */
    protected boolean matches(Object entity, Condition condition) {
        if (condition instanceof PropertyCondition propertyCondition) {
            if (PropertyCondition.Operation.CONTAINS.equals(propertyCondition.getOperation())) {
                Object rawValue = EntityValues.getValue(entity, propertyCondition.getProperty());
                String value = rawValue == null ? "" : rawValue.toString();
                String search = unescapeForLike(String.valueOf(propertyCondition.getParameterValue()));
                return value.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT));
            }
            return true;
        }
        if (condition instanceof LogicalCondition logicalCondition) {
            List<Condition> nested = logicalCondition.getConditions();
            if (nested.isEmpty()) {
                return true;
            }
            return logicalCondition.getType() == LogicalCondition.Type.AND
                    ? nested.stream().allMatch(nestedCondition -> matches(entity, nestedCondition))
                    : nested.stream().anyMatch(nestedCondition -> matches(entity, nestedCondition));
        }
        return true;
    }

    protected String unescapeForLike(String value) {
        return value.replace("\\_", "_")
                .replace("\\%", "%")
                .replace("\\\\", "\\");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Comparator<Object> createComparator(Sort sort) {
        Comparator<Object> comparator = null;
        for (Sort.Order order : sort.getOrders()) {
            Comparator<Object> propertyComparator = Comparator.comparing(
                    entity -> (Comparable) EntityValues.getValue(entity, order.getProperty()),
                    Comparator.nullsFirst(Comparator.naturalOrder()));
            if (order.getDirection() == Sort.Direction.DESC) {
                propertyComparator = propertyComparator.reversed();
            }
            comparator = comparator == null ? propertyComparator : comparator.thenComparing(propertyComparator);
        }
        return comparator;
    }

    protected List<Object> applyPaging(List<Object> list, int firstResult, int maxResults) {
        int from = Math.min(Math.max(firstResult, 0), list.size());
        int to = maxResults > 0 ? Math.min(from + maxResults, list.size()) : list.size();
        return new ArrayList<>(list.subList(from, to));
    }

    @Override
    public long getCount(LoadContext<?> context) {
        Map<Object, Object> instances = entities.get(context.getEntityMetaClass().getName());
        if (instances == null)
            return 0;
        else
            return instances.size();
    }

    @Override
    public Set<Object> save(SaveContext context) {
        Set<Object> result = new HashSet<>();

        for (Object entity : context.getEntitiesToSave()) {
            String metaClassName = metadata.getClass(entity.getClass()).getName();
            Map<Object, Object> instances = entities.get(metaClassName);
            if (instances == null) {
                instances = new ConcurrentHashMap<>();
                entities.put(metaClassName, instances);
            }
            instances.put(EntityValues.getId(entity), entity);
            result.add(entity);
        }
        for (Object entity : context.getEntitiesToRemove()) {
            String metaClassName = metadata.getClass(entity.getClass()).getName();
            Map<Object, Object> instances = entities.get(metaClassName);
            if (instances != null) {
                instances.remove(EntityValues.getId(entity));
            }
            result.add(entity);
        }

        return result;
    }

    @Override
    public List<KeyValueEntity> loadValues(ValueLoadContext context) {
        return new ArrayList<>();
    }

    @Override
    public long getCount(ValueLoadContext context) {
        return 0;
    }

    public void clear() {
        entities.clear();
    }
}
