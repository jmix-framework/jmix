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
package io.jmix.reports.listener;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.listener.BeforeDetachEntityListener;
import io.jmix.reports.ReportingApi;
import io.jmix.reports.entity.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component("report_ReportDetachListener")
public class ReportDetachListener implements BeforeDetachEntityListener<Report> {

    @Autowired
    protected ReportingApi reportingApi;

    @Autowired
    protected Persistence persistence;

    @Override
    public void onBeforeDetach(Report entity, EntityManager entityManager) {
        if (persistence.getTools().isLoaded(entity, "xml") && StringUtils.isNotBlank(entity.getXml())) {
            Report reportFromXml = reportingApi.convertToReport(entity.getXml());
            entity.setBands(reportFromXml.getBands());
            entity.setInputParameters(reportFromXml.getInputParameters());
            entity.setReportScreens(reportFromXml.getReportScreens());
            entity.setRoles(reportFromXml.getRoles());
            entity.setValuesFormats(reportFromXml.getValuesFormats());
            entity.setValidationOn(reportFromXml.getValidationOn());
            entity.setValidationScript(reportFromXml.getValidationScript());

            setRelevantReferencesToReport(entity);
            sortRootChildrenBands(entity);
        }
    }

    protected void sortRootChildrenBands(Report entity) {
        if (entity.getRootBandDefinition() != null
                && CollectionUtils.isNotEmpty(entity.getRootBandDefinition().getChildrenBandDefinitions())) {
            List<BandDefinition> bandDefinitions = new ArrayList<>(entity.getRootBandDefinition().getChildrenBandDefinitions());
            Collections.sort(bandDefinitions, new Comparator<BandDefinition>() {
                @Override
                public int compare(BandDefinition o1, BandDefinition o2) {
                    return o1.getPosition().compareTo(o2.getPosition());
                }
            });
            entity.getRootBandDefinition().setChildrenBandDefinitions(bandDefinitions);
        }
    }

    protected void setRelevantReferencesToReport(Report entity) {
        for (ReportValueFormat reportValueFormat : entity.getValuesFormats()) {
            reportValueFormat.setReport(entity);
        }

        for (BandDefinition bandDefinition : entity.getBands()) {
            bandDefinition.setReport(entity);
        }

        for (ReportInputParameter reportInputParameter : entity.getInputParameters()) {
            reportInputParameter.setReport(entity);
        }

        for (ReportScreen reportScreen : entity.getReportScreens()) {
            reportScreen.setReport(entity);
        }
    }
}