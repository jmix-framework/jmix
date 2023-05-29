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

/**
 * This interface describes Band abstraction. Band is description of some data.
 * Bands have tree structure - parent has several children, they also can have children, etc.
 */
public interface ReportBand extends Serializable {
    String getName();

    ReportBand getParent();

    List<ReportBand> getChildren();

    List<ReportQuery> getReportQueries();

    /**
     * @return band orientation. Relevant only for Xls and Xlsx templates.
     */
    BandOrientation getBandOrientation();
}