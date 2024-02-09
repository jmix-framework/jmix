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


import io.jmix.core.FetchPlan;
import io.jmix.core.annotation.Experimental;
import io.jmix.core.querycondition.Condition;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;



/**
 * @param fetchPlan {@link FetchPlan} to load entities with. Default value: {@link FetchPlan#BASE}
 * @param condition {@link Condition} to filter entities
 * @param hints query hints, e.g. from {@code io.jmix.data.PersistenceHints}
 */
@Experimental
public record JmixDataRepositoryContext(@Nullable FetchPlan fetchPlan,
                                        @Nullable Condition condition,
                                        @Nullable Map<String, Serializable> hints) implements Serializable {

    @Override
    public Map<String, Serializable> hints() {
        return hints != null ? hints : Collections.emptyMap();
    }

    /**
     * @return a new builder for {@link JmixDataRepositoryContext}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return a new builder for {@link JmixDataRepositoryContext} with specified {@code fetchPlan}.
     */
    public static Builder plan(@Nullable FetchPlan fetchPlan) {
        return builder().plan(fetchPlan);
    }

    /**
     * @return a new builder for {@link JmixDataRepositoryContext} with specified {@code condition}.
     */
    public static Builder condition(@Nullable Condition condition) {
        return builder().condition(condition);
    }

    /**
     * @return a new builder for {@link JmixDataRepositoryContext} with specified {@code hints}.
     */
    public static Builder hints(@Nullable Map<String, Serializable> hints) {
        return builder().hints(hints);
    }

    /**
     * @return a new {@link JmixDataRepositoryContext} with specified {@code fetchPlan}.
     */
    public static JmixDataRepositoryContext of(FetchPlan fetchPlan) {
        return new JmixDataRepositoryContext(fetchPlan, null, null);
    }

    /**
     * @return a new {@link JmixDataRepositoryContext} with specified {@code condition}.
     */
    public static JmixDataRepositoryContext of(Condition condition) {
        return new JmixDataRepositoryContext(null, condition, null);
    }

    /**
     * @return a new {@link JmixDataRepositoryContext} with specified {@code hints}.
     */
    public static JmixDataRepositoryContext of(Map<String, Serializable> hints) {
        return new JmixDataRepositoryContext(null, null, hints);
    }

    public static class Builder {
        protected FetchPlan fetchPlan;
        protected Condition condition;
        protected Map<String, Serializable> hints;

        public Builder plan(@Nullable FetchPlan plan) {
            this.fetchPlan = plan;
            return this;
        }

        public Builder condition(@Nullable Condition condition) {
            this.condition = condition;
            return this;
        }

        public Builder hints(@Nullable Map<String, Serializable> hints) {
            this.hints = hints;
            return this;
        }

        public JmixDataRepositoryContext build() {
            return new JmixDataRepositoryContext(fetchPlan, condition, hints);
        }
    }
}
