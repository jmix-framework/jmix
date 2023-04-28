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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.reports.converter.MetadataFieldsIgnoringGson;

import jakarta.persistence.Id;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@JmixEntity(name = "report_TemplateTableDescription")
public class TemplateTableDescription {

    protected final static Gson gson;

    static {
        gson =  MetadataFieldsIgnoringGson.create()
                .addIgnoringStrategy()
                .build();
    }

    @Id
    @JmixGeneratedValue
    protected UUID id;

    @JmixProperty
    protected List<TemplateTableBand> templateTableBands = new LinkedList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<TemplateTableBand> getTemplateTableBands() {
        return templateTableBands;
    }

    public void setTemplateTableBands(List<TemplateTableBand> templateTableBands) {
        this.templateTableBands = templateTableBands;
    }

    public static String toJsonString(TemplateTableDescription description) {
        return gson.toJson(description);
    }

    public static TemplateTableDescription fromJsonString(String json) {
        try {
            return gson.fromJson(json, TemplateTableDescription.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }
}
