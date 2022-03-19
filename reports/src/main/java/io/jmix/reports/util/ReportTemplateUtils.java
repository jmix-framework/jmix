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

package io.jmix.reports.util;

import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import org.apache.commons.lang3.BooleanUtils;

public class ReportTemplateUtils {

    public static boolean inputParametersRequiredByTemplates(Report report) {
        return report.getTemplates() != null && report.getTemplates().size() > 1 || containsAlterableTemplate(report);
    }

    public static boolean containsAlterableTemplate(Report report) {
        if (report.getTemplates() == null) {
            return false;
        }
        return report.getTemplates()
                .stream()
                .anyMatch(ReportTemplateUtils::supportAlterableForTemplate);
    }

    public static boolean supportAlterableForTemplate(ReportTemplate template) {
        if (BooleanUtils.isTrue(template.getCustom())) {
            return false;
        }
        if (template.getReportOutputType() == ReportOutputType.CHART || template.getReportOutputType() == ReportOutputType.TABLE) {
            return false;
        }
        return BooleanUtils.isTrue(template.getAlterable());
    }
}
