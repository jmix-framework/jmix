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

package io.jmix.hibernate.impl;

import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.type.Type;

import java.util.HashMap;
import java.util.Map;

public class SoftDeletionFilterDefinition extends FilterDefinition {

    public static final String NAME = "SoftDeletionFilter";

    private static final long serialVersionUID = 2763953911243774699L;

    /**
     * Construct a default SoftDeletionFilterDefinition instance.
     */
    public SoftDeletionFilterDefinition() {
        super(NAME, "", new HashMap<>());
    }

    /**
     * Construct a new FilterDefinition instance.
     *
     * @param name             The name of the filter for which this configuration is in effect.
     * @param defaultCondition
     * @param parameterTypes
     */
    public SoftDeletionFilterDefinition(String name, String defaultCondition, Map<String, Type> parameterTypes) {
        super(name, defaultCondition, parameterTypes);
    }
}
