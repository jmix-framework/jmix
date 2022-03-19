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

package io.jmix.emailtemplates.entity.listener;

import io.jmix.core.EntityStates;
import io.jmix.core.Metadata;
import io.jmix.data.listener.BeforeDetachEntityListener;
import io.jmix.emailtemplates.TemplateConverter;
import io.jmix.emailtemplates.entity.JsonEmailTemplate;
import io.jmix.emailtemplates.entity.TemplateReport;
import io.jmix.reports.entity.Report;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component("emltmp_JsonEmailTemplateDetachListener")
public class JsonEmailTemplateDetachListener implements BeforeDetachEntityListener<JsonEmailTemplate> {
    @Autowired
    protected TemplateConverter templateConverter;

    @Autowired
    protected EntityStates entityStates;

    @Autowired
    protected Metadata metadata;

    @Override
    public void onBeforeDetach(JsonEmailTemplate entity) {
        if (entityStates.isLoaded(entity, "reportJson") && StringUtils.isNotBlank(entity.getReportJson())) {
            Report reportFromXml = templateConverter.convertToReport(entity);
            entity.setReport(reportFromXml);

            TemplateReport templateReport = metadata.create(TemplateReport.class);
            templateReport.setReport(reportFromXml);
            templateReport.setEmailTemplate(entity);
            templateReport.setParameterValues(new ArrayList<>());

            entity.setEmailBodyReport(templateReport);
        }
    }
}
