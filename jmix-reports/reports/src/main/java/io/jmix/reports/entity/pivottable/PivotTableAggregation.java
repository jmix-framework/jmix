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

package io.jmix.reports.entity.pivottable;

import io.jmix.core.UuidProvider;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import jakarta.persistence.Id;
import jakarta.persistence.Lob;

import java.util.UUID;

@JmixEntity(name = "report_PivotTableAggregation")
public class PivotTableAggregation {

    @Id
    @JmixGeneratedValue
    protected UUID id;

    @JmixProperty
    protected AggregationMode mode;

    @JmixProperty
    protected String caption;

    @Lob
    @JmixProperty
    protected String function;

    public PivotTableAggregation() {
        id = UuidProvider.createUuid();
    }

    public PivotTableAggregation(AggregationMode mode) {
        id = UuidProvider.createUuid();
        setMode(mode);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public AggregationMode getMode() {
        return mode;
    }

    public void setMode(AggregationMode mode) {
        this.mode = mode;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }
}
