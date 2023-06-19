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

package io.jmix.reports.entity;

import io.jmix.reports.yarg.structure.ReportOutputType;

import java.io.ObjectStreamException;

public class JmixReportOutputType extends ReportOutputType {

    public final static JmixReportOutputType chart = new JmixReportOutputType("chart");
    public final static JmixReportOutputType table = new JmixReportOutputType("table");
    public final static JmixReportOutputType pivot = new JmixReportOutputType("pivot");

    static {
       values.put(chart.getId(), chart);
       values.put(table.getId(), table);
       values.put(pivot.getId(), pivot);
    }

    public JmixReportOutputType(String id) {
        super(id);
    }

    private Object readResolve() throws ObjectStreamException {
        return getOutputTypeById(getId());
    }
}
