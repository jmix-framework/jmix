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

package io.jmix.reports;

import io.jmix.reports.converter.GsonConverter;
import io.jmix.reports.converter.XStreamConverter;
import io.jmix.reports.entity.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class to serialize and deserialize reports
 */
@Component("report_ReportsSerialization")
public class ReportsSerialization {

    @Autowired
    protected GsonConverter gsonConverter;

    protected XStreamConverter xStreamConverter = new XStreamConverter();

    /**
     * Serializes specified report to JSON string.
     *
     * @param report report
     * @return report that serialized to JSON
     */
    public String convertToString(Report report) {
        return gsonConverter.convertToString(report);
    }

    /**
     * Deserializes a report from a specified string
     *
     * @param serializedReport serialized report in XML or JSON string
     * @return report entity
     */
    public Report convertToReport(String serializedReport) {
        if (!serializedReport.startsWith("<")) {//for old xml reports
            return gsonConverter.convertToReport(serializedReport);
        } else {
            return xStreamConverter.convertToReport(serializedReport);
        }
    }
}
