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
package io.jmix.core;

import org.springframework.lang.Nullable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Defines a {@link FetchPlan} property. Each fetch plan property corresponds to a
 * {@link io.jmix.core.metamodel.model.MetaProperty} with the same name.
 */
public class FetchPlanProperty implements Serializable {

    private static final long serialVersionUID = 4098678639930287203L;

    private String name;

    private FetchPlan fetchPlan;

    private FetchMode fetchMode = FetchMode.AUTO;

    public FetchPlanProperty(String name, @Nullable FetchPlan fetchPlan) {
        this(name, fetchPlan, FetchMode.AUTO);
    }

    public FetchPlanProperty(String name, @Nullable FetchPlan fetchPlan, FetchMode fetchMode) {
        this.name = name;
        this.fetchPlan = fetchPlan;
        this.fetchMode = fetchMode;
    }

    /**
     * @return property name that is a metaclass attribute name
     */
    public String getName() {
        return name;
    }

    /**
     * @return fetch plan of the property if the corresponding meta class attribute is a reference
     */
    @Nullable
    public FetchPlan getFetchPlan() {
        return fetchPlan;
    }

    /**
     * @return fetch mode if the property is a reference
     */
    public FetchMode getFetchMode() {
        return fetchMode;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FetchPlanProperty)) return false;
        FetchPlanProperty that = (FetchPlanProperty) o;
        return name.equals(that.name) && Objects.equals(fetchPlan, that.fetchPlan) && fetchMode == that.fetchMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, fetchPlan, fetchMode);
    }
}
