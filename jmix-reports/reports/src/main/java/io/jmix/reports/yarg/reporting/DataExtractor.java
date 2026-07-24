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
import org.jspecify.annotations.NullMarked;

import java.util.Map;
import java.util.Set;

/**
 * This class should load data using ReportQuery objects, convert data onto BandData object and build BandData object tree (link children and parent bands)
 * The default implementation is io.jmix.reports.yarg.reporting.DataExtractorImpl
 * !Attention! Please make sure if you really need to change this behaviour against default implementation cause it might crash report generation logic
 */
@FunctionalInterface
@NullMarked
public interface DataExtractor {

    void extractData(Report report, Map<String, Object> params, BandData rootBand);

    /**
     * Same as {@link #extractData(Report, Map, BandData)}, but bands whose names are in
     * {@code excludedBandNames} are not extracted (their data is supplied by other means, e.g. a
     * streaming cursor). Their names are still registered as first-level definitions.
     *
     * <p>The default implementation supports only an empty exclusion set and throws otherwise:
     * silently extracting an excluded band would load the streaming band's whole dataset into memory
     * and run its query twice. Implementations that should work with streaming reports must override
     * this method (see {@link DataExtractorImpl}).
     */
    default void extractData(Report report, Map<String, Object> params, BandData rootBand,
                             Set<String> excludedBandNames) {
        if (!excludedBandNames.isEmpty()) {
            throw new UnsupportedOperationException(String.format(
                    "DataExtractor [%s] does not support band exclusion required for streaming reports; "
                            + "override extractData(Report, Map, BandData, Set)",
                    getClass().getName()));
        }

        extractData(report, params, rootBand);
    }

    default boolean getPutEmptyRowIfNoDataSelected() { return true; }

}
