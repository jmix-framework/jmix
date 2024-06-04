/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.jmix.reports.yarg.structure;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class contains list of supported output types. It can be extended, new types can be added at runtime
 */
public class ReportOutputType implements Serializable {
    /*
     * Following constants are predefined output types containing in base reporting
     */
    public final static ReportOutputType xls = new ReportOutputType("xls");
    public final static ReportOutputType doc = new ReportOutputType("doc");
    public final static ReportOutputType docx = new ReportOutputType("docx");
    public final static ReportOutputType xlsx = new ReportOutputType("xlsx");
    public final static ReportOutputType html = new ReportOutputType("html");
    public final static ReportOutputType pdf = new ReportOutputType("pdf");
    public final static ReportOutputType csv = new ReportOutputType("csv");
    public final static ReportOutputType custom = new ReportOutputType("custom");

    protected static Map<String, ReportOutputType> values = new ConcurrentHashMap<String, ReportOutputType>();

    static {
        values.put(xls.id, xls);
        values.put(doc.id, doc);
        values.put(docx.id, docx);
        values.put(html.id, html);
        values.put(pdf.id, pdf);
        values.put(csv.id, csv);
        values.put(custom.id, custom);
        values.put(xlsx.id, xlsx);
    }

    private final String id;

    public ReportOutputType(String id) {
        if (id == null) {
            throw new NullPointerException("\"id\" field can not be null");
        }
        this.id = id;
    }

    protected static void registerOutputType(ReportOutputType outputType) {
        if (outputType == null) {
            throw new NullPointerException("\"outputType\" parameter can not be null");
        }
        values.put(outputType.id, outputType);
    }

    public static ReportOutputType getOutputTypeById(String id) {
        return values.get(id);
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "ReportOutputType{" +
                "id='" + id + '\'' +
                '}';
    }
}