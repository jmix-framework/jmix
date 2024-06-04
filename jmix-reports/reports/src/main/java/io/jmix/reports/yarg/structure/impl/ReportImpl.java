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
package io.jmix.reports.yarg.structure.impl;

import com.google.common.base.Preconditions;
import io.jmix.reports.yarg.structure.*;

import java.util.*;

public class ReportImpl implements Report {
    protected String name;
    protected Map<String, ReportTemplate> reportTemplates;
    protected ReportBand rootBand;
    protected List<ReportParameter> reportParameters;
    protected List<ReportFieldFormat> reportFieldFormats;

    public ReportImpl(String name, Map<String, ReportTemplate> reportTemplates, ReportBand rootBand,
                      List<ReportParameter> reportParameters, List<ReportFieldFormat> reportFieldFormats) {
        this.name = name;
        this.reportTemplates = reportTemplates;
        this.rootBand = rootBand;
        this.reportParameters = reportParameters;
        this.reportFieldFormats = reportFieldFormats;

        validate();
    }

    protected ReportImpl() {
        this.name = "";
        this.rootBand = null;
        this.reportTemplates = new HashMap<String, ReportTemplate>();
        this.reportParameters = new ArrayList<ReportParameter>();
        this.reportFieldFormats = new ArrayList<ReportFieldFormat>();
    }

    protected void validate() {
        Preconditions.checkNotNull(this.name, "\"name\" parameter can not be null");
        Preconditions.checkNotNull(this.reportTemplates, "\"reportTemplates\" parameter can not be null");
        Preconditions.checkNotNull(this.reportParameters, "\"reportParameters\" parameter can not be null");
        Preconditions.checkNotNull(this.reportFieldFormats, "\"reportFieldFormats\" parameter can not be null");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, ReportTemplate> getReportTemplates() {
        return Collections.unmodifiableMap(reportTemplates);
    }

    @Override
    public ReportBand getRootBand() {
        return rootBand;
    }

    @Override
    public List<ReportParameter> getReportParameters() {
        return Collections.unmodifiableList(reportParameters);
    }

    @Override
    public List<ReportFieldFormat> getReportFieldFormats() {
        return Collections.unmodifiableList(reportFieldFormats);
    }
}