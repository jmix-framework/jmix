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

import com.haulmont.yarg.structure.ReportFieldFormat;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import jakarta.persistence.Id;
import java.util.UUID;

@JmixEntity(name = "report_ReportValueFormat")
@SystemLevel
public class ReportValueFormat implements ReportFieldFormat {

    private static final long serialVersionUID = 680180375698449946L;

    @Id
    @JmixGeneratedValue
    protected UUID id;

    @JmixProperty
    protected String valueName;

    @JmixProperty
    protected String formatString;

    @JmixProperty
    protected Report report;

    @JmixProperty
    protected Boolean groovyScript = false;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }

    public String getFormatString() {
        return formatString;
    }

    public void setFormatString(String formatString) {
        this.formatString = formatString;
    }

    public Boolean getGroovyScript() {
        return groovyScript;
    }

    public void setGroovyScript(Boolean groovyScript) {
        this.groovyScript = groovyScript;
    }

    @Override
    public String getName() {
        return valueName;
    }

    @Override
    public String getFormat() {
        return formatString;
    }

    @Override
    public Boolean isGroovyScript() {
        return groovyScript;
    }
}
