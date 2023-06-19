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
import io.jmix.reports.yarg.structure.BandOrientation;

import java.util.function.BiFunction;

/**
 * This interface implementation may holding relation between report band orientation and related controller logic
 * if relation not set, default controller should be returned
 * <p>The default controller implementation is <b>io.jmix.reports.yarg.reporting.extraction.controller.DefaultExtractionController</b></p>
 *
 * <p>The default implementation is <b>io.jmix.reports.yarg.reporting.extraction.DefaultExtractionControllerFactory</b></p>
 */
public interface ExtractionControllerFactory {
    /**
     * Method for runtime configuring data extraction logic by orientation
     *
     * @param orientation band orientation
     * @param controllerCreator specific creator function for extraction controller
     */
    void register(BandOrientation orientation, BiFunction<ExtractionControllerFactory, ReportLoaderFactory, ExtractionController> controllerCreator);

    /**
     * @param orientation band orientation
     * @return data extraction controller
     */
    ExtractionController controllerBy(BandOrientation orientation);

    /**
     * @return default data extraction controller
     */
    ExtractionController defaultController();
}
