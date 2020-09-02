/*
 * Copyright (c) 2008-2019 Haulmont.
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

import io.jmix.core.metamodel.annotation.ModelObject;
import io.jmix.core.metamodel.annotation.ModelProperty;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import io.jmix.core.entity.annotation.SystemLevel;
import com.haulmont.yarg.structure.BandOrientation;
import com.haulmont.yarg.structure.ReportBand;
import com.haulmont.yarg.structure.ReportQuery;

import java.util.ArrayList;
import java.util.List;

@ModelObject(name = "report$BandDefinition")
@NamePattern("%s|name")
@SystemLevel
public class BandDefinition extends BaseUuidEntity implements ReportBand {

    private static final long serialVersionUID = 8658220979738705511L;

    @ModelProperty
    protected String name;

    @ModelProperty
    protected BandDefinition parentBandDefinition;

    @ModelProperty
    protected Report report;

    @ModelProperty
    protected List<BandDefinition> childrenBandDefinitions = new ArrayList<>();

    @ModelProperty
    protected List<DataSet> dataSets = new ArrayList<>();

    @ModelProperty
    protected Integer orientation;

    @ModelProperty
    protected Integer position;

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
    public BandOrientation  getBandOrientation() {
        return BandOrientation.defaultIfNull(getOrientation() != null ? getOrientation().getBandOrientation(): null);
    }
}