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

package io.jmix.emailtemplates.entity;


import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.reports.entity.Report;

import javax.persistence.*;

@Entity(name = "emltmp_ReportEmailTemplate")
@JmixEntity
@DiscriminatorValue("R")
public class ReportEmailTemplate extends EmailTemplate {

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMAIL_BODY_REPORT_ID")
    protected TemplateReport emailBodyReport;

    @Override
    public void setEmailBodyReport(TemplateReport emailBodyReport) {
        this.emailBodyReport = emailBodyReport;
    }

    public TemplateReport getEmailBodyReport() {
        return emailBodyReport;
    }

    public ReportEmailTemplate() {
        setType(TemplateType.REPORT);
    }

    @Override
    public Report getReport() {
        return getEmailBodyReport() != null ? getEmailBodyReport().getReport() : null;
    }
}