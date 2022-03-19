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

package io.jmix.reports.entity.wizard;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import io.jmix.core.metamodel.model.MetaClass;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JmixEntity(name = "report_WizardReportRegion")
@SystemLevel
public class ReportRegion implements OrderableEntity {

    private static final long serialVersionUID = -3122228074679382191L;
    public static final String HEADER_BAND_PREFIX = "header";

    @Id
    @JmixGeneratedValue
    protected UUID id;
    @JmixProperty
    @Transient
    protected ReportData reportData;
    @JmixProperty
    @Transient
    protected Boolean isTabulatedRegion;
    @JmixProperty
    @Composition
    @Transient
    protected List<RegionProperty> regionProperties = new ArrayList<>();
    @JmixProperty
    @Transient
    protected EntityTreeNode regionPropertiesRootNode;
    @JmixProperty
    @Transient
    protected Long orderNum = Long.MAX_VALUE;
    @JmixProperty
    @Transient
    protected String bandNameFromReport;

    public ReportRegion() {
    }

    public EntityTreeNode getRegionPropertiesRootNode() {
        return regionPropertiesRootNode;
    }

    public void setRegionPropertiesRootNode(EntityTreeNode regionPropertiesRootNode) {
        this.regionPropertiesRootNode = regionPropertiesRootNode;
    }

    @Override
    public Long getOrderNum() {
        return orderNum;
    }

    @Override
    public void setOrderNum(Long orderNum) {
        this.orderNum = orderNum;
    }

    public ReportData getReportData() {
        return reportData;
    }

    public void setReportData(ReportData reportData) {
        this.reportData = reportData;
    }

    public Boolean getIsTabulatedRegion() {
        return isTabulatedRegion;
    }

    public void setIsTabulatedRegion(Boolean isTabulatedRegion) {
        this.isTabulatedRegion = isTabulatedRegion;
    }

    public List<RegionProperty> getRegionProperties() {
        return regionProperties;
    }

    public void setRegionProperties(List<RegionProperty> regionProperties) {
        this.regionProperties = regionProperties;
    }

    @JmixProperty
    @Transient
    public String getNameForBand() {
        return StringUtils.isEmpty(bandNameFromReport) ? getRegionPropertiesRootNode().getEntityClassName() +
                (isTabulatedRegion() ? "s" : "") +
                (getReportData().getReportRegions().size() == 1 ? "" : getOrderNum().toString()) : bandNameFromReport;
    }

    @JmixProperty
    @Transient
    public String getNameForHeaderBand() {
        return HEADER_BAND_PREFIX + getNameForBand();
    }

    public boolean isTabulatedRegion() {
        return Boolean.TRUE.equals(isTabulatedRegion);
    }

    public void setBandNameFromReport(String bandNameFromReport) {
        this.bandNameFromReport = bandNameFromReport;
    }

    public String getBandNameFromReport() {
        return bandNameFromReport;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
