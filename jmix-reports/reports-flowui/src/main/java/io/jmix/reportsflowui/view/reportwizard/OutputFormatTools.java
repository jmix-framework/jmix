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

package io.jmix.reportsflowui.view.reportwizard;

import com.google.common.collect.ImmutableMap;
import io.jmix.core.Messages;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.wizard.TemplateFileType;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("report_OutputFormatTools")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class OutputFormatTools {

    protected Map<TemplateFileType, Map<String, ReportOutputType>> availableOutputFormats;

    @Autowired
    protected Messages messages;

    @PostConstruct
    protected void init() {
        availableOutputFormats = new ImmutableMap.Builder<TemplateFileType, Map<String, ReportOutputType>>()
                .put(TemplateFileType.DOCX, new ImmutableMap.Builder<String, ReportOutputType>()
                        .put(messages.getMessage(ReportOutputType.DOCX), ReportOutputType.DOCX)
                        .put(messages.getMessage(ReportOutputType.HTML), ReportOutputType.HTML)
                        .put(messages.getMessage(ReportOutputType.PDF), ReportOutputType.PDF)
                        .build())
                .put(TemplateFileType.XLSX, new ImmutableMap.Builder<String, ReportOutputType>()
                        .put(messages.getMessage(ReportOutputType.XLSX), ReportOutputType.XLSX)
                        .put(messages.getMessage(ReportOutputType.HTML), ReportOutputType.HTML)
                        .put(messages.getMessage(ReportOutputType.PDF), ReportOutputType.PDF)
                        .put(messages.getMessage(ReportOutputType.CSV), ReportOutputType.CSV)
                        .build())
                .put(TemplateFileType.HTML, new ImmutableMap.Builder<String, ReportOutputType>()
                        .put(messages.getMessage(ReportOutputType.HTML), ReportOutputType.HTML)
                        .put(messages.getMessage(ReportOutputType.PDF), ReportOutputType.PDF)
                        .build())
                .put(TemplateFileType.CHART, new ImmutableMap.Builder<String, ReportOutputType>()
                        .put(messages.getMessage(ReportOutputType.CHART), ReportOutputType.CHART)
                        .build())
                .put(TemplateFileType.CSV, new ImmutableMap.Builder<String, ReportOutputType>()
                        .put(messages.getMessage(ReportOutputType.CSV), ReportOutputType.CSV)
                        .build())
                .put(TemplateFileType.TABLE, new ImmutableMap.Builder<String, ReportOutputType>()
                        .put(messages.getMessage(ReportOutputType.TABLE), ReportOutputType.TABLE)
                        .build())
                .build();
    }

    public Map<String, ReportOutputType> getOutputAvailableFormats(TemplateFileType templateFileType) {
        return availableOutputFormats.get(templateFileType);
    }

}
