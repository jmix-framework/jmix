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

package io.jmix.eclipselink.impl.mapping;

import io.jmix.core.MetadataTools;
import io.jmix.eclipselink.persistence.AdditionalCriteriaProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * The implementation of additional criterion for soft delete.
 */
@Component("eclipselink_SoftDeleteAdditionalCriteriaProvider")
public class SoftDeleteAdditionalCriteriaProvider implements AdditionalCriteriaProvider {
    @Autowired
    protected MetadataTools metadataTools;

    private static final String DELETE_TS_IS_NULL = "this.%s is null";

    public boolean requiresAdditionalCriteria(Class<?> entityClass) {
        return metadataTools.isSoftDeletable(entityClass);
    }

    @Override
    public String getAdditionalCriteria(Class<?> entityClass) {
        return String.format(DELETE_TS_IS_NULL, metadataTools.findDeletedDateProperty(entityClass));
    }

    @Override
    public Map<String, Object> getCriteriaParameters() {
        return null;
    }
}