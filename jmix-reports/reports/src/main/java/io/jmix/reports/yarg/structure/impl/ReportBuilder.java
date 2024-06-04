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

public class ReportBuilder {
    protected ReportImpl report;
    protected ReportBandImpl rootBandDefinition;

    public ReportBuilder() {
        rootBandDefinition = createRootBand();
        report = createReport();
        report.rootBand = rootBandDefinition;
    }

    public ReportBuilder band(ReportBand band) {
        Preconditions.checkNotNull(band, "\"band\" parameter can not be null");
        ReportBandImpl wrapperBandDefinition = copyBand(band);
        rootBandDefinition.childrenBandDefinitions.add(wrapperBandDefinition);
        wrapperBandDefinition.parentBandDefinition = rootBandDefinition;
        return this;
    }

    public ReportBuilder template(ReportTemplate reportTemplate) {
        Preconditions.checkNotNull(reportTemplate, "\"reportTemplate\" parameter can not be null");
        report.reportTemplates.put(reportTemplate.getCode(), reportTemplate);
        return this;
    }

    public ReportBuilder parameter(ReportParameter reportParameter) {
        Preconditions.checkNotNull(reportParameter, "\"reportParameter\" parameter can not be null");
        report.reportParameters.add(reportParameter);
        return this;
    }

    public ReportBuilder format(ReportFieldFormat reportFieldFormat) {
        Preconditions.checkNotNull(reportFieldFormat, "\"reportFieldFormat\" parameter can not be null");
        report.reportFieldFormats.add(reportFieldFormat);
        return this;
    }

    public ReportBuilder name(String name) {
        Preconditions.checkNotNull(name, "\"name\" parameter can not be null");
        report.name = name;
        return this;
    }

    public Report build() {
        report.validate();
        ReportImpl result = report;
        report = new ReportImpl();
        return result;
    }

    protected ReportImpl createReport() {
        return new ReportImpl();
    }

    protected ReportBandImpl createRootBand() {
        return new ReportBandImpl(BandData.ROOT_BAND_NAME, null);
    }

    protected ReportBandImpl copyBand(ReportBand band) {
        return new ReportBandImpl(band);
    }
}
