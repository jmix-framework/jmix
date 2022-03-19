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

package io.jmix.emailtemplates.dto;


import io.jmix.reports.entity.Report;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ReportWithParams implements Serializable {

    protected Report report;
    protected Map<String, Object> params;

    public ReportWithParams(Report report) {
        this.report = report;
        params = new HashMap<>();
    }

    public Report getReport() {
        return report;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Object put(String key, Object value) {
        return params.put(key, value);
    }

    public boolean remove(String key, Object value) {
        return params.remove(key, value);
    }

    public boolean isEmptyParams() {
        return params.isEmpty();
    }

}
