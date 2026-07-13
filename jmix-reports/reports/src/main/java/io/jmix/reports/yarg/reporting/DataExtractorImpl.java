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


import io.jmix.reports.yarg.loaders.factory.ReportLoaderFactory;
import io.jmix.reports.yarg.reporting.extraction.DefaultExtractionContextFactory;
import io.jmix.reports.yarg.reporting.extraction.DefaultExtractionControllerFactory;
import io.jmix.reports.yarg.reporting.extraction.ExtractionContextFactory;
import io.jmix.reports.yarg.reporting.extraction.ExtractionControllerFactory;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.Report;
import io.jmix.reports.yarg.structure.ReportBand;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NullMarked;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

@NullMarked
public class DataExtractorImpl implements DataExtractor {
    protected static final Map<String, Object> EMPTY_MAP = Collections.emptyMap();

    protected ReportLoaderFactory loaderFactory;
    protected ExtractionContextFactory contextFactory;
    protected ExtractionControllerFactory controllerFactory;

    protected boolean putEmptyRowIfNoDataSelected = true;

    public DataExtractorImpl(ReportLoaderFactory loaderFactory) {
        checkNotNull(loaderFactory, "\"loaderFactory\" parameter can not be null");

        this.loaderFactory = loaderFactory;
        this.contextFactory = createContextFactory();
        this.controllerFactory = createControllerFactory();
    }

    protected ExtractionContextFactory createContextFactory() {
        return new DefaultExtractionContextFactory(this);
    }

    protected ExtractionControllerFactory createControllerFactory() {
        return new DefaultExtractionControllerFactory(loaderFactory);
    }

    /**
     * Extracts all band data of the report into {@code rootBand}.
     *
     * <p>Note for subclasses: this method delegates to
     * {@link #extractData(Report, Map, BandData, Set)}, which is the canonical override point.
     * An override of only this 3-arg method is NOT invoked on the streaming report path (the engine
     * calls the 4-arg variant directly) — override the 4-arg method to customize both paths.
     */
    public void extractData(Report report, Map<String, Object> params, BandData rootBand) {
        extractData(report, params, rootBand, Collections.emptySet());
    }

    @Override
    public void extractData(Report report, Map<String, Object> params, BandData rootBand,
                            Set<String> excludedBandNames) {
        List<Map<String, Object>> rootBandData = controllerFactory.defaultController().extractData(
                contextFactory.context(report.getRootBand(), null, params)
        );
        if (CollectionUtils.isNotEmpty(rootBandData)) {
            rootBand.getData().putAll(rootBandData.get(0));
        }

        List<ReportBand> firstLevelBands = report.getRootBand().getChildren().stream()
                .sorted(Comparator.comparingInt(ReportBand::getPosition))
                .toList();

        for (ReportBand definition : firstLevelBands) {
            rootBand.getFirstLevelBandDefinitionNames().add(definition.getName());
            if (excludedBandNames.contains(definition.getName())) {
                continue;
            }
            List<BandData> bands = createBands(definition, rootBand, params);
            rootBand.addChildren(bands);
        }
    }

    public void setPutEmptyRowIfNoDataSelected(boolean putEmptyRowIfNoDataSelected) {
        this.putEmptyRowIfNoDataSelected = putEmptyRowIfNoDataSelected;
    }

    @Override
    public boolean getPutEmptyRowIfNoDataSelected() {
        return putEmptyRowIfNoDataSelected;
    }

    public ExtractionControllerFactory getExtractionControllerFactory() {
        return controllerFactory;
    }

    public void setExtractionControllerFactory(ExtractionControllerFactory controllerFactory) {
        checkNotNull(controllerFactory, "\"controllerFactory\" parameter can not be null");

        this.controllerFactory = controllerFactory;
    }

    protected List<BandData> createBands(ReportBand definition, BandData parentBandData, Map<String, Object> params) {
        return controllerFactory.controllerBy(definition.getBandOrientation())
                .extract(contextFactory.context(definition, parentBandData, params));
    }
}
