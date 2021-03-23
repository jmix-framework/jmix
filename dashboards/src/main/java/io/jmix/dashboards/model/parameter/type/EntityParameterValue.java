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

package io.jmix.dashboards.model.parameter.type;

import java.util.HashMap;
import java.util.Objects;

public class EntityParameterValue implements ParameterValue {
    protected String metaClassName;
    protected String entityId;
    protected String fetchPlan;

    public EntityParameterValue() {
    }

    public EntityParameterValue(String metaClassName, String entityId, String fetchPlan) {
        this.metaClassName = metaClassName;
        this.entityId = entityId;
        this.fetchPlan = fetchPlan;
    }

    @Override
    public Object getValue() {
        return new HashMap<String, String>() {
            {
                put("metaClassName", metaClassName);
                put("entityId", entityId);
                put("fetchPlan", fetchPlan);
            }
        };
    }

    public String getMetaClassName() {
        return metaClassName;
    }

    public void setMetaClassName(String metaClassName) {
        this.metaClassName = metaClassName;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getFetchPlan() {
        return fetchPlan;
    }

    public void setFetchPlan(String fetchPlan) {
        this.fetchPlan = fetchPlan;
    }

    @Override
    public String toString() {
        return String.format("type: entity; metaClassName=%s, entityId=%s, fetchPlan=%s", metaClassName, entityId, fetchPlan);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityParameterValue that = (EntityParameterValue) o;
        return Objects.equals(metaClassName, that.metaClassName) &&
                Objects.equals(entityId, that.entityId) &&
                Objects.equals(fetchPlan, that.fetchPlan);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metaClassName, entityId, fetchPlan);
    }
}
