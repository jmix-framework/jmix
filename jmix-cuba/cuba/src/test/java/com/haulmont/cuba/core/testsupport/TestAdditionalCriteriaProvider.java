/*
 * Copyright (c) 2008-2021 Haulmont.
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

package com.haulmont.cuba.core.testsupport;


import io.jmix.eclipselink.persistence.AdditionalCriteriaProvider;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

public class TestAdditionalCriteriaProvider implements AdditionalCriteriaProvider {

    private Object param;

    @Override
    public boolean requiresAdditionalCriteria(Class entityClass) {
        String className = entityClass.getName();
        return className.equals("com.haulmont.cuba.core.model.entity_cache.ParentCachedEntity")
                || className.equals("com.haulmont.cuba.core.model.entity_cache.ChildCachedEntity");
    }

    @Override
    public String getAdditionalCriteria(Class entityClass) {
        return " this.testAdditional = :testAdditional";
    }

    @Nullable
    @Override
    public Map<String, Object> getCriteriaParameters() {
        return Collections.singletonMap("testAdditional", param);
    }

    public void setParam(Object param) {
        this.param = param;
    }
}
