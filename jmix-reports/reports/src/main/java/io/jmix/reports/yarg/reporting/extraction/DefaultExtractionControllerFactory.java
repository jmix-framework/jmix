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

package io.jmix.reports.yarg.reporting.extraction;

import io.jmix.reports.yarg.loaders.factory.ReportLoaderFactory;
import io.jmix.reports.yarg.reporting.extraction.controller.DefaultExtractionController;
import io.jmix.reports.yarg.structure.BandOrientation;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default controller factory implementation
 */
public class DefaultExtractionControllerFactory implements ExtractionControllerFactory {
    protected ReportLoaderFactory loaderFactory;
    protected ExtractionController defaultExtractionController;
    protected Map<BandOrientation, ExtractionController> extractionControllerMap = new ConcurrentHashMap<>();

    public DefaultExtractionControllerFactory(ReportLoaderFactory loaderFactory) {
        this(loaderFactory, DefaultExtractionController::new);
    }

    public DefaultExtractionControllerFactory(ReportLoaderFactory loaderFactory,
                                              BiFunction<ExtractionControllerFactory, ReportLoaderFactory, ExtractionController> controllerCreator) {
        this.loaderFactory = loaderFactory;
        this.defaultExtractionController = controllerCreator.apply(this, loaderFactory);
    }

    @Override
    public void register(BandOrientation orientation, BiFunction<ExtractionControllerFactory, ReportLoaderFactory, ExtractionController> controllerCreator) {
        checkNotNull(orientation);
        checkNotNull(controllerCreator);

        extractionControllerMap.put(orientation, controllerCreator.apply(this, loaderFactory));
    }

    @Override
    public ExtractionController controllerBy(BandOrientation orientation) {
        return extractionControllerMap.getOrDefault(BandOrientation.defaultIfNull(orientation), defaultExtractionController);
    }

    @Override
    public ExtractionController defaultController() {
        return defaultExtractionController;
    }

    public Map<BandOrientation, ExtractionController> getExtractionControllers() {
        return Collections.unmodifiableMap(extractionControllerMap);
    }

    public void setExtractionControllers(Map<BandOrientation, ExtractionController> extractionControllers) {
        checkNotNull(extractionControllers);

        extractionControllerMap = extractionControllers;
    }
}
