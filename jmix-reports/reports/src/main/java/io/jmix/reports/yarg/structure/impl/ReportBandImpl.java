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
import io.jmix.reports.yarg.structure.ReportQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ReportBandImpl implements ReportBand {
    protected String name;
    protected ReportBand parentBandDefinition;
    protected List<ReportBand> childrenBandDefinitions;
    protected List<ReportQuery> reportQueries;
    protected BandOrientation orientation;

    protected ReportBandImpl() {
        this.childrenBandDefinitions = new ArrayList<ReportBand>();
        this.reportQueries = new ArrayList<ReportQuery>();
        this.orientation = BandOrientation.HORIZONTAL;
    }

    public ReportBandImpl(String name, ReportBand parentBandDefinition, Collection<ReportBand> childrenBandDefinitions, Collection<ReportQuery> reportQueries, BandOrientation orientation) {
        this.name = name;
        this.parentBandDefinition = parentBandDefinition;
        this.childrenBandDefinitions = new ArrayList<ReportBand>(childrenBandDefinitions);
        this.reportQueries = new ArrayList<ReportQuery>(reportQueries);
        this.orientation = orientation;

        validate();
    }

    public ReportBandImpl(String name, ReportBand parentBandDefinition, BandOrientation orientation) {
        this(name, parentBandDefinition, new ArrayList<ReportBand>(), new ArrayList<ReportQuery>(), orientation);
    }

    public ReportBandImpl(String name, ReportBand parentBandDefinition) {
        this(name, parentBandDefinition, Collections.<ReportBand>emptyList(), Collections.<ReportQuery>emptyList(), BandOrientation.HORIZONTAL);
    }

    public ReportBandImpl(ReportBand instanceToCopy) {
        this(instanceToCopy.getName(), instanceToCopy.getParent(), instanceToCopy.getChildren(), instanceToCopy.getReportQueries(), instanceToCopy.getBandOrientation());
    }

    protected void validate() {
        Preconditions.checkNotNull(this.name, "\"name\" parameter can not be null");
        Preconditions.checkNotNull(this.orientation, "\"orientation\" parameter can not be null");
    }

    public String getName() {
        return name;
    }

    public ReportBand getParent() {
        return parentBandDefinition;
    }

    public List<ReportBand> getChildren() {
        return Collections.unmodifiableList(childrenBandDefinitions);
    }

    public List<ReportQuery> getReportQueries() {
        return Collections.unmodifiableList(reportQueries);
    }

    public BandOrientation getBandOrientation() {
        return orientation;
    }
}