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

package io.jmix.searchelasticsearch.searching.strategy.impl;

import io.jmix.search.searching.impl.AbstractSearchStrategy;
import io.jmix.search.searching.impl.SearchFieldsResolver;

public abstract class AbstractElasticSearchStrategy extends AbstractSearchStrategy<ElasticSearchQueryConfigurator> {
    protected AbstractElasticSearchStrategy(SearchFieldsResolver searchFieldsResolver, ElasticSearchQueryConfigurator queryConfigurator) {
        super(searchFieldsResolver, queryConfigurator);
    }
}
