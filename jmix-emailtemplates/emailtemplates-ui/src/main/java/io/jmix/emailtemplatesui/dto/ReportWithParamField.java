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

package io.jmix.emailtemplatesui.dto;


import io.jmix.reports.entity.Report;
import io.jmix.ui.component.Field;

import java.io.Serializable;
import java.util.Map;

public class ReportWithParamField implements Serializable {
    protected Report report;
    protected Map<String, Field> fields;

    public ReportWithParamField(Report report, Map<String, Field> fields) {
        this.report = report;
        this.fields = fields;
    }

    public Report getReport() {
        return report;
    }

    public Map<String, Field> getFields() {
        return fields;
    }

    public void setFields(Map<String, Field> fields) {
        this.fields = fields;
    }

    public Field put(String key, Field value) {
        return fields.put(key, value);
    }

    public boolean remove(String key, Field value) {
        return fields.remove(key, value);
    }

    public boolean isEmptyParams() {
        return fields.isEmpty();
    }

}
