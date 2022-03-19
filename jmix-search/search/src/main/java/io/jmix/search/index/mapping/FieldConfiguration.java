/*
 * Copyright 2020 Haulmont.
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

package io.jmix.search.index.mapping;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jmix.core.common.util.Preconditions;

/**
 * Contains configuration of index field.
 */
public class FieldConfiguration {

    protected ObjectNode config;

    protected FieldConfiguration(ObjectNode config) {
        this.config = config;
    }

    /**
     * Provides field configuration as Elasticsearch-native json.
     *
     * @return json with field configuration
     */
    public ObjectNode asJson() {
        return config.deepCopy();
    }

    public static FieldConfiguration create(ObjectNode config) {
        Preconditions.checkNotNullArgument(config);

        return new FieldConfiguration(config);
    }
}
