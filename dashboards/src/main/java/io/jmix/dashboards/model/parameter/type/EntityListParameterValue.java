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

import io.jmix.core.metamodel.annotation.JmixEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JmixEntity(name = "dshbrd_EntityListParameterValue")
public class EntityListParameterValue extends ParameterValue {
    protected List<EntityParameterValue> entityValues;

    public EntityListParameterValue() {
        entityValues = new ArrayList<>();
    }

    public EntityListParameterValue(List<EntityParameterValue> entityValues) {
        this.entityValues = entityValues;
    }

    public List<EntityParameterValue> getEntityValues() {
        return entityValues;
    }

    public void setEntityValues(List<EntityParameterValue> entityValues) {
        this.entityValues = entityValues;
    }

    @Override
    public String toString() {
        return "type: entity list";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityListParameterValue that = (EntityListParameterValue) o;
        if ((entityValues == null & that.entityValues != null) || (entityValues != null & that.entityValues == null)) {
            return false;
        }

        if (entityValues != null && that.entityValues != null) {
            if (entityValues.size() != that.entityValues.size()) {
                return false;
            }

            for (EntityParameterValue epv : entityValues) {
                Optional<EntityParameterValue> epvOpt = that.entityValues.stream().filter(v -> v.equals(epv)).findFirst();
                if (!epvOpt.isPresent()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {

        return Objects.hash(entityValues);
    }
}
