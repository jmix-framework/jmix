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
import io.jmix.reports.yarg.structure.BandOrientation;
import io.jmix.reports.yarg.structure.ReportBand;

public class BandBuilder {
    protected ReportBandImpl bandDefinition = createBand();

    public BandBuilder child(ReportBand bandDefinition) {
        Preconditions.checkNotNull(bandDefinition, "\"bandDefinition\" parameter can not be null");
        ReportBandImpl copyBand = copyBand(bandDefinition);
        copyBand.parentBandDefinition = this.bandDefinition;
        this.bandDefinition.childrenBandDefinitions.add(copyBand);
        return this;
    }

    public BandBuilder query(String name, String script, String loaderType) {
        bandDefinition.reportQueries.add(createReportQuery(name, script, loaderType, null));
        return this;
    }

    public BandBuilder query(String name, String script, String loaderType, String linkParameterName) {
        bandDefinition.reportQueries.add(createReportQuery(name, script, loaderType, linkParameterName));
        return this;
    }

    public BandBuilder name(String name) {
        Preconditions.checkNotNull(name, "\"name\" parameter can not be null");
        bandDefinition.name = name;
        return this;

    }

    public BandBuilder orientation(BandOrientation orientation) {
        Preconditions.checkNotNull(orientation, "\"orientation\" parameter can not be null");
        bandDefinition.orientation = orientation;
        return this;

    }

    public ReportBand build() {
        bandDefinition.validate();
        ReportBandImpl result = bandDefinition;
        bandDefinition = new ReportBandImpl();
        return result;
    }

    protected ReportBandImpl createBand() {
        return new ReportBandImpl();
    }

    protected ReportBandImpl copyBand(ReportBand bandDefinition) {
        return new ReportBandImpl(bandDefinition);
    }

    protected ReportQueryImpl createReportQuery(String name, String script, String loaderType, String linkParameterName) {
        return new ReportQueryImpl(name, script, loaderType, linkParameterName, null);
    }
}
