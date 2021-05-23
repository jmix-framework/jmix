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

import com.haulmont.yarg.structure.BandOrientation;
import com.haulmont.yarg.structure.ReportBand;
import com.haulmont.yarg.structure.ReportQuery;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JmixEntity(name = "report_BandDefinition")
@SystemLevel
public class BandDefinition implements ReportBand {

    private static final long serialVersionUID = 8658220979738705511L;

    @JmixId
    @JmixGeneratedValue
    protected UUID id;

    @InstanceName
    @JmixProperty
    protected String name;

    @JmixProperty
    protected BandDefinition parentBandDefinition;

    @JmixProperty
    protected Report report;

    @JmixProperty
    protected List<BandDefinition> childrenBandDefinitions = new ArrayList<>();

    @JmixProperty
    @Composition
    protected List<DataSet> dataSets = new ArrayList<>();

    @JmixProperty
    protected Integer orientation;

    @JmixProperty
    protected Integer position;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BandDefinition getParentBandDefinition() {
        return parentBandDefinition;
    }

    public void setParentBandDefinition(BandDefinition parentBandDefinition) {
        this.parentBandDefinition = parentBandDefinition;
    }

    public List<BandDefinition> getChildrenBandDefinitions() {
        return childrenBandDefinitions;
    }

    public void setChildrenBandDefinitions(List<BandDefinition> childrenBandDefinitions) {
        this.childrenBandDefinitions = childrenBandDefinitions;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DataSet> getDataSets() {
        return dataSets;
    }

    public void setDataSets(List<DataSet> dataSets) {
        this.dataSets = dataSets;
    }

    public Orientation getOrientation() {
        return Orientation.fromId(orientation);
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation != null ? orientation.getId() : null;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position != null && position > 0 ? position : 0;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    @Override
    public ReportBand getParent() {
        return parentBandDefinition;
    }

    @Override
    public List<ReportBand> getChildren() {
        return (List) childrenBandDefinitions;
    }

    @Override
    public List<ReportQuery> getReportQueries() {
        return (List) dataSets;
    }

    @Override
    public BandOrientation getBandOrientation() {
        return BandOrientation.defaultIfNull(getOrientation() != null ? getOrientation().getBandOrientation() : null);
    }
}