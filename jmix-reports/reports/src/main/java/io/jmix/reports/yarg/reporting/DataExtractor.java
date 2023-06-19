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
package io.jmix.reports.yarg.reporting;

import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.Report;

import java.util.Map;

/**
 * This class should load data using ReportQuery objects, convert data onto BandData object and build BandData object tree (link children and parent bands)
 * The default implementation is io.jmix.reports.yarg.reporting.DataExtractorImpl
 * !Attention! Please make sure if you really need to change this behaviour against default implementation cause it might crash report generation logic
 */
@FunctionalInterface
public interface DataExtractor {

    void extractData(Report report, Map<String, Object> params, BandData rootBand);

    default boolean getPutEmptyRowIfNoDataSelected() { return true; }

}
