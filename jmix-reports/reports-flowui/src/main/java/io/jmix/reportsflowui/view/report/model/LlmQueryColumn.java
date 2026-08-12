/*
 * Copyright 2026 Haulmont.
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

package io.jmix.reportsflowui.view.report.model;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.reportsflowui.view.report.ReportDetailView;
import jakarta.persistence.Id;

import java.util.UUID;

/**
 * One column of the query stored in an LLM data set, as the designer shows and edits it: the columns are a list
 * of names, positional against the select clause, and a row per name is what {@link ReportDetailView} binds its
 * column list to.
 */
@JmixEntity(name = "report_LlmQueryColumn")
@SystemLevel
public class LlmQueryColumn {

    @JmixGeneratedValue
    @Id
    protected UUID id;

    protected String name;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
