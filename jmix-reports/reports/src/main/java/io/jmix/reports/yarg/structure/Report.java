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
package io.jmix.reports.yarg.structure;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * This interface describes main report object. Contains all data about report: bands, parameters, formats.
 */
public interface Report extends Serializable {

    String getName();

    /**
     * @return map with report templates &lt;templateCode, template&gt;
     */
    Map<String, ReportTemplate> getReportTemplates();

    /**
     * @return root band which contains all others bands
     */
    ReportBand getRootBand();

    List<ReportParameter> getReportParameters();

    List<ReportFieldFormat> getReportFieldFormats();
}