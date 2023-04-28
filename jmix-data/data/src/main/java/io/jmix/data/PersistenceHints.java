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

package io.jmix.data;

import io.jmix.core.FetchPlan;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import java.util.*;

public class PersistenceHints {

    public static final String SOFT_DELETION = "jmix.softDeletion";

    public static final String FETCH_PLAN = "jmix.fetchPlan";

    public static final String CACHEABLE = "jmix.cacheable";

    /**
     * Defines a Jmix query hint which enable customization of generated SQL statements.
     * Adds an SQL hint string after the SQL statement.
     * <p>The SQL hint can be used on certain database platforms to define how the query uses indexes
     * and other such low level usages. It should be the full hint string including the comment delimiters.
     * <p>Corresponds to {@code org.eclipse.persistence.config.QueryHints#HINT}
     * <p>Usage examples:
     * <pre>
     *    query.setHint(PersistenceHints.SQL_HINT, "OPTION(RECOMPILE)");
     * </pre>
     */
    public static final String SQL_HINT = "jmix.sql.hint";

    /**
     * Defines a Jmix query hint which enable customization of generated SQL statements.
     * Adds <code>OPTION(RECOMPILE)</code> SQL hint for MSSQL database. Hint value is ignored.
     * <p>Usage examples:
     * <pre>
     *    query.setHint(PersistenceHints.MSSQL_RECOMPILE_HINT, true);
     * </pre>
     */
    public static final String MSSQL_RECOMPILE_HINT = "jmix.mssql.recompile";

    public static boolean isSoftDeletion(EntityManager entityManager) {
        Boolean softDeletion = (Boolean) entityManager.getProperties().get(SOFT_DELETION);
        return softDeletion == null || softDeletion;
    }

    @SuppressWarnings("unchecked")
    public static Collection<FetchPlan> getFetchPlans(@Nullable Map<String, Object> properties) {
        if (properties == null)
            return Collections.emptyList();
        Object value = properties.get(FETCH_PLAN);
        if (value == null) {
            return Collections.emptyList();
        } else if (value instanceof Collection) {
            return (Collection) value;
        } else {
            return (Collection) Collections.singletonList(value);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Map<String, Object> properties;

        public Builder() {
            this(new HashMap<>());
        }

        public Builder(Map<String, Object> properties) {
            this.properties = properties;
        }

        @SuppressWarnings("unchecked")
        public Builder withFetchPlan(FetchPlan fetchPlan) {
            properties.compute(FETCH_PLAN, (s, o) -> {
                if (o == null) {
                    ArrayList<FetchPlan> list = new ArrayList<>(2);
                    list.add(fetchPlan);
                    return list;
                } else {
                    if (o instanceof Collection) {
                        ((Collection) o).add(fetchPlan);
                        return o;
                    } else {
                        ArrayList list = new ArrayList(2);
                        list.add(o);
                        list.add(fetchPlan);
                        return list;
                    }
                }
            });
            return this;
        }

        public Builder withFetchPlans(FetchPlan... fetchPlans) {
            for (FetchPlan fetchPlan : fetchPlans) {
                withFetchPlan(fetchPlan);
            }
            return this;
        }

        public Builder withFetchPlans(Collection<FetchPlan> fetchPlans) {
            for (FetchPlan fetchPlan : fetchPlans) {
                withFetchPlan(fetchPlan);
            }
            return this;
        }

        public Map<String, Object> build() {
            return properties;
        }
    }
}
