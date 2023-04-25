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

package io.jmix.reports.entity.table;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import jakarta.persistence.Id;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;


@JmixEntity(name = "report_TemplateTableBand")
public class TemplateTableBand {

    @Id
    @JmixGeneratedValue
    protected UUID id;

    @JmixProperty
    protected String bandName;

    @JmixProperty
    protected Integer position;

    @JmixProperty
    protected List<TemplateTableColumn> columns = new LinkedList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<TemplateTableColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<TemplateTableColumn> columns) {
        this.columns = columns;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getBandName() {
        return bandName;
    }

    public void setBandName(String bandName) {
        this.bandName = bandName;
    }
}
